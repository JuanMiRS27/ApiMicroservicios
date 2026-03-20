locals {
  services = {
    auth = {
      run_name      = "auth-service"
      db_instance   = "auth-db"
      db_name       = "auth_db"
      db_user       = "auth_user"
      image_name    = "auth-service"
      service_acct  = "auth-service-sa"
    }
    catalog = {
      run_name      = "catalog-service"
      db_instance   = "catalog-db"
      db_name       = "catalog_db"
      db_user       = "catalog_user"
      image_name    = "catalog-service"
      service_acct  = "catalog-service-sa"
    }
    inventory = {
      run_name      = "inventory-service"
      db_instance   = "inventory-db"
      db_name       = "inventory_db"
      db_user       = "inventory_user"
      image_name    = "inventory-service"
      service_acct  = "inventory-service-sa"
    }
    reporting = {
      run_name      = "reporting-service"
      db_instance   = "reporting-db"
      db_name       = "reporting_db"
      db_user       = "reporting_user"
      image_name    = "reporting-service"
      service_acct  = "reporting-service-sa"
    }
  }

  artifact_registry_base = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.images.repository_id}"
}

resource "google_project_service" "required" {
  for_each = toset([
    "artifactregistry.googleapis.com",
    "cloudbuild.googleapis.com",
    "run.googleapis.com",
    "secretmanager.googleapis.com",
    "sqladmin.googleapis.com"
  ])

  project            = var.project_id
  service            = each.value
  disable_on_destroy = false
}

resource "google_artifact_registry_repository" "images" {
  location      = var.region
  repository_id = var.artifact_registry_repository_id
  description   = "Imagenes Docker de inventory-microservices."
  format        = "DOCKER"

  depends_on = [google_project_service.required]
}

resource "random_password" "jwt_secret" {
  length  = 64
  special = false
}

resource "random_password" "internal_api_key" {
  length  = 48
  special = false
}

resource "random_password" "db_password" {
  for_each = local.services

  length  = 32
  special = false
}

resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "inventory-jwt-secret"

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret_version" "jwt_secret" {
  secret      = google_secret_manager_secret.jwt_secret.id
  secret_data = random_password.jwt_secret.result
}

resource "google_secret_manager_secret" "internal_api_key" {
  secret_id = "inventory-internal-api-key"

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret_version" "internal_api_key" {
  secret      = google_secret_manager_secret.internal_api_key.id
  secret_data = random_password.internal_api_key.result
}

resource "google_secret_manager_secret" "db_password" {
  for_each = local.services

  secret_id = "${each.value.run_name}-db-password"

  replication {
    auto {}
  }

  depends_on = [google_project_service.required]
}

resource "google_secret_manager_secret_version" "db_password" {
  for_each = local.services

  secret      = google_secret_manager_secret.db_password[each.key].id
  secret_data = random_password.db_password[each.key].result
}

resource "google_service_account" "service" {
  for_each = local.services

  account_id   = each.value.service_acct
  display_name = "${each.value.run_name} runtime"
}

resource "google_project_iam_member" "cloudsql_client" {
  for_each = local.services

  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.service[each.key].email}"
}

resource "google_secret_manager_secret_iam_member" "jwt_secret_accessor" {
  for_each = local.services

  secret_id = google_secret_manager_secret.jwt_secret.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.service[each.key].email}"
}

resource "google_secret_manager_secret_iam_member" "internal_api_key_accessor" {
  for_each = local.services

  secret_id = google_secret_manager_secret.internal_api_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.service[each.key].email}"
}

resource "google_secret_manager_secret_iam_member" "db_password_accessor" {
  for_each = local.services

  secret_id = google_secret_manager_secret.db_password[each.key].id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.service[each.key].email}"
}

resource "google_sql_database_instance" "db" {
  for_each = local.services

  name             = each.value.db_instance
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier = var.cloud_sql_tier

    ip_configuration {
      ipv4_enabled = true
    }

    backup_configuration {
      enabled = true
    }
  }

  deletion_protection = false

  depends_on = [google_project_service.required]
}

resource "google_sql_database" "db" {
  for_each = local.services

  name     = each.value.db_name
  instance = google_sql_database_instance.db[each.key].name
}

resource "google_sql_user" "db_user" {
  for_each = local.services

  instance = google_sql_database_instance.db[each.key].name
  name     = each.value.db_user
  password = random_password.db_password[each.key].result
}

resource "google_cloud_run_v2_service" "service" {
  for_each = local.services

  name     = each.value.run_name
  location = var.region

  deletion_protection = false

  template {
    service_account = google_service_account.service[each.key].email

    scaling {
      min_instance_count = 0
      max_instance_count = var.max_instance_count
    }

    containers {
      image = "${local.artifact_registry_base}/${each.value.image_name}:latest"

      ports {
        container_port = 8080
      }

      env {
        name  = "DB_NAME"
        value = each.value.db_name
      }

      env {
        name  = "DB_USER"
        value = each.value.db_user
      }

      env {
        name  = "INSTANCE_CONNECTION_NAME"
        value = google_sql_database_instance.db[each.key].connection_name
      }

      env {
        name = "DB_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password[each.key].secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "APP_SECURITY_JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.jwt_secret.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "APP_SECURITY_INTERNAL_API_KEY"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.internal_api_key.secret_id
            version = "latest"
          }
        }
      }
    }
  }

  depends_on = [
    google_project_iam_member.cloudsql_client,
    google_secret_manager_secret_iam_member.jwt_secret_accessor,
    google_secret_manager_secret_iam_member.internal_api_key_accessor,
    google_secret_manager_secret_iam_member.db_password_accessor,
    google_sql_database.db,
    google_sql_user.db_user
  ]
}

resource "google_cloud_run_service_iam_member" "public_invoker" {
  for_each = local.services

  location = var.region
  service  = google_cloud_run_v2_service.service[each.key].name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
