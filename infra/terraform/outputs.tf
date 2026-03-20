output "artifact_registry_repository" {
  value = google_artifact_registry_repository.images.name
}

output "cloud_run_services" {
  value = {
    for key, service in google_cloud_run_v2_service.service :
    key => service.uri
  }
}

output "cloud_sql_instances" {
  value = {
    for key, instance in google_sql_database_instance.db :
    key => instance.connection_name
  }
}
