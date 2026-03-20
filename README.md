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
