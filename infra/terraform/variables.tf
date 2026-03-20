variable "project_id" {
  description = "ID del proyecto de Google Cloud."
  type        = string
}

variable "region" {
  description = "Region principal para Cloud Run y Cloud SQL."
  type        = string
  default     = "us-central1"
}

variable "artifact_registry_repository_id" {
  description = "Nombre del repositorio de Artifact Registry."
  type        = string
  default     = "inventory-microservices"
}

variable "cloud_sql_tier" {
  description = "Tier de Cloud SQL para cada microservicio."
  type        = string
  default     = "db-custom-1-3840"
}

variable "max_instance_count" {
  description = "Maximo de instancias por servicio de Cloud Run."
  type        = number
  default     = 3
}
