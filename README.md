# Inventario con microservicios

Backend para inventario de productos con un maximo de 4 microservicios:

1. `auth-service`: autenticacion JWT y administracion de usuarios.
2. `catalog-service`: categorias y productos.
3. `inventory-service`: entradas, salidas y stock.
4. `reporting-service`: reportes de stock y movimientos.

## Reglas de negocio implementadas

- El stock vive en `inventory-service` y parte en `0`.
- Solo usuarios con rol `ADMIN` pueden crear categorias, productos y usuarios.
- Usuarios con rol `OPERATOR` pueden registrar entradas y salidas.
- Cada microservicio usa su propia base de datos PostgreSQL.
- La configuracion usa `application.yml`.
- Todo el entorno esta dockerizado.

## Credenciales iniciales

- Admin:
  - usuario: `admin`
  - clave: `Admin123!`
- Operador:
  - usuario: `operator`
  - clave: `Operator123!`

## Ejecutar con Docker

```bash
docker compose up --build
```

## Despliegue en Cloud Run

El repositorio quedo preparado para desplegar cada microservicio directamente desde Cloud Run usando Dockerfile en la raiz del repo:

- `Dockerfile.auth-service`
- `Dockerfile.catalog-service`
- `Dockerfile.inventory-service`
- `Dockerfile.reporting-service`

Cambios aplicados para Cloud Run:

- Cada servicio escucha automaticamente en `PORT`.
- Los clientes internos descubren por defecto las URLs de Cloud Run usando el patron `https://SERVICE-PROJECT_NUMBER.REGION.run.app`.
- Las llamadas internas entre servicios agregan automaticamente `X-Serverless-Authorization` con un ID token cuando corren en Cloud Run.
- La configuracion de base de datos acepta `SPRING_DATASOURCE_*`, `DATABASE_URL`, `DATABASE_PUBLIC_URL`, `PG*` y `DB_*`.

Variables recomendadas por servicio:

- `SPRING_DATASOURCE_URL` o `DATABASE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_SECURITY_JWT_SECRET`
- `APP_SECURITY_INTERNAL_API_KEY`

Si dejas vacias `APP_CLIENTS_*_BASE_URL`, los servicios se resuelven solos en Cloud Run. Solo necesitas definirlas si quieres sobrescribir el destino por defecto.

## Servicios

- Auth: `http://localhost:8081`
- Catalog: `http://localhost:8082`
- Inventory: `http://localhost:8083`
- Reporting: `http://localhost:8084`

## Flujo sugerido

1. Iniciar sesion en `auth-service`.
2. Crear categorias y productos con el usuario `admin`.
3. Registrar entradas y salidas con `admin` o `operator`.
4. Consultar reportes con `admin`.

## Endpoints principales

### Auth

- `POST /api/auth/login`
- `GET /api/users`
- `POST /api/users`

### Catalog

- `GET /api/categories`
- `POST /api/categories`
- `GET /api/products`
- `POST /api/products`

### Inventory

- `POST /api/movements/entries`
- `POST /api/movements/exits`
- `GET /api/stocks/{productId}`
- `GET /api/movements`

### Reporting

- `GET /api/reports/stock-summary`
- `GET /api/reports/movements-summary`
- `GET /api/reports/history`
