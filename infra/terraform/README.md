# Terraform para Google Cloud

Este directorio define la infraestructura completa necesaria para ejecutar el sistema en Google Cloud:

- 4 servicios de Cloud Run
- 4 instancias de Cloud SQL PostgreSQL
- 4 bases de datos y 4 usuarios
- 4 service accounts
- Secret Manager para JWT, internal API key y passwords de base de datos
- Artifact Registry para las imagenes

Limitacion importante:

- Terraform define y crea la infraestructura, pero Cloud Run no ejecuta Terraform automaticamente al "conectar un repo".
- Para que exista la plataforma por primera vez, necesitas un pipeline o una ejecucion de `terraform apply` con permisos sobre el proyecto.

Despues de creada la infraestructura, el codigo de la aplicacion ya quedo preparado para que los servicios:

- escuchen en `PORT`
- se autentiquen entre si dentro de Cloud Run
- descubran las URLs internas automaticamente
- se conecten a Cloud SQL usando `INSTANCE_CONNECTION_NAME`
