#!/usr/bin/env bash
set -euo pipefail

REGION="${1:-us-central1}"
REPOSITORY="${2:-inventory-microservices}"
PROJECT_ID="${PROJECT_ID:-$(gcloud config get-value project 2>/dev/null)}"

if [[ -z "${PROJECT_ID}" || "${PROJECT_ID}" == "(unset)" ]]; then
  echo "No hay proyecto activo en gcloud."
  echo "Ejecuta: gcloud config set project TU_PROJECT_ID"
  exit 1
fi

echo "Proyecto: ${PROJECT_ID}"
echo "Region: ${REGION}"
echo "Repositorio Docker: ${REPOSITORY}"

gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  --project "${PROJECT_ID}"

if ! gcloud artifacts repositories describe "${REPOSITORY}" \
  --location "${REGION}" \
  --project "${PROJECT_ID}" >/dev/null 2>&1; then
  gcloud artifacts repositories create "${REPOSITORY}" \
    --repository-format=docker \
    --location="${REGION}" \
    --description="Microservicios inventory para Cloud Run" \
    --project "${PROJECT_ID}"
fi

gcloud builds submit \
  --project "${PROJECT_ID}" \
  --region "${REGION}" \
  --config cloudbuild.yaml \
  --substitutions "_REGION=${REGION},_REPOSITORY=${REPOSITORY}"

echo "Despliegue completado."
