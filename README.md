# Enterprise CRUD API (Spring Boot)

Backend corporativo para portafolio: CRUD evolucionado con seguridad JWT, RBAC, auditoria, observabilidad, paginacion/filtros, pruebas y pipeline CI.

## Stack

- Java 17+ (compila con JDK 21)
- Spring Boot 3.3.x
- Spring Security + JWT access/refresh
- PostgreSQL + Flyway
- OpenAPI/Swagger
- Actuator + correlation-id + rate limit login
- JUnit 5 + Testcontainers
- Spotless + JaCoCo (70% en capa service)

## Milestones implementados

- M1 Seguridad base: register/login, JWT access+refresh, RBAC (ADMIN/MANAGER/USER), logout con invalidacion.
- M2 CRUD mejorado: versionado (`/api/v1`), validaciones, paginacion/sort/search, Problem Details (RFC7807).
- M3 Auditoria: `createdAt/updatedAt/createdBy/updatedBy`, soft delete + restore, `audit_log`.
- M4 Operacion: Actuator (`health/info/metrics`), correlation-id, rate limit en login.
- M5 Calidad: tests unitarios e integracion con Testcontainers, Spotless, JaCoCo, GitHub Actions.
- M6 Deploy gratis: guia Render + Neon (y alternativa Render Postgres).

## Estructura

```text
.
├── docker-compose.yml
├── Dockerfile
├── docs/
│   ├── architecture.md
│   └── adr-001-auth.md
├── requests/
│   └── api.http
├── src/
│   ├── main/
│   │   ├── java/com/empresaccutsb/apirest/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/V1__init_schema.sql
│   └── test/
│       └── java/com/empresaccutsb/apirest/
├── .github/workflows/ci.yml
└── pom.xml
```

## Ejecutar local

1) Copia variables:

```bash
cp .env.template .env
```

2) Levanta DB + API:

```bash
docker compose --env-file .env up --build
```

3) Swagger y health:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/actuator/health`

## Ejecutar con Maven

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## Endpoints principales

- Auth
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
- Productos
  - `GET /api/v1/productos?page=0&size=20&sort=createdAt,desc&search=abc`
  - `GET /api/v1/productos/{id}`
  - `POST /api/v1/productos` (ADMIN/MANAGER)
  - `PUT /api/v1/productos/{id}` (ADMIN/MANAGER)
  - `DELETE /api/v1/productos/{id}` (ADMIN soft delete)
  - `POST /api/v1/productos/{id}/restore` (ADMIN)

Compatibilidad del CRUD original:

- `GET /productos`
- `GET /productos/{id}`
- `POST /productos`
- `PUT /productos/{id}`
- `DELETE /productos/{id}`

## Testing y calidad

```bash
./mvnw spotless:check test verify
```

- Unit tests + integration tests con PostgreSQL Testcontainers.
- JaCoCo exige cobertura minima de 70% para `service`.

## Seguridad antes de publicar en GitHub

1. Crea tu archivo local de secretos sin versionarlo:

```bash
cp .env.template .env
```

2. Reemplaza en `.env` estos valores obligatorios:

- `JWT_SECRET` (usa una cadena aleatoria de 64+ caracteres)
- `BOOTSTRAP_ADMIN_PASSWORD` (fuerte y unico)

3. Verifica que `.env` no se suba:

```bash
git status --short
```

4. Escanea secretos antes del push (recomendado):

```bash
docker run --rm -v "$PWD:/repo" zricethezav/gitleaks:latest detect --source=/repo --no-git
```

5. Nunca subas credenciales reales a:

- `README.md`
- `.env.template`
- `requests/api.http`

6. El workflow `Security` ejecuta gitleaks en cada PR/push (`.github/workflows/security.yml`).

## Deploy gratis (Render + Neon) paso a paso

### 0) Requisitos de cuentas

- Cuenta GitHub
- Cuenta Render
- Cuenta Neon

### 1) Publica el repo en GitHub (sin secretos)

1. Asegurate que `.env` exista solo localmente y no este trackeado.
2. Sube el repo.
3. Activa en GitHub: `Settings -> Security -> Secret scanning` (si esta disponible en tu plan).

### 2) Crea la base de datos en Neon

1. En Neon: `Create Project`.
2. Elige region cercana a Render.
3. Crea una base (ejemplo: `apirest`).
4. En `Connection Details` copia:
   - host
   - database
   - user
   - password
   - port

No necesitas crear perfiles extra en Spring: ya existe `application-prod.yml` para produccion.

### 3) Crea el Web Service en Render

1. Render -> `New +` -> `Web Service`.
2. Conecta tu repo GitHub.
3. Configura:
   - Runtime: `Docker` (usa `Dockerfile` del proyecto)
   - Branch: `main`
   - Plan: `Free`

### 4) Variables de entorno en Render

En `Environment` agrega estas variables (todas):

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://<NEON_HOST>:<NEON_PORT>/<NEON_DB>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME=<NEON_USER>`
- `SPRING_DATASOURCE_PASSWORD=<NEON_PASSWORD>`
- `JWT_SECRET=<random_64+_chars>`
- `JWT_ISSUER=enterprise-crud-api`
- `JWT_ACCESS_TOKEN_MINUTES=15`
- `JWT_REFRESH_TOKEN_DAYS=7`
- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_USERNAME=admin`
- `BOOTSTRAP_ADMIN_EMAIL=<tu-email>`
- `BOOTSTRAP_ADMIN_PASSWORD=<password-fuerte>`

Genera `JWT_SECRET` con:

```bash
openssl rand -base64 64
```

### 5) Primer deploy y hardening inmediato

1. Haz deploy en Render.
2. Verifica:
   - `GET /actuator/health` responde `UP`
   - `GET /swagger-ui.html` abre correctamente
3. Inicia sesion con el admin bootstrap.
4. Crea un usuario administrador permanente via flujo normal si lo deseas.
5. Vuelve a Render y cambia:
   - `BOOTSTRAP_ADMIN_ENABLED=false`
6. Redeploy para desactivar bootstrap en produccion.

### 6) Checklist de seguridad post-deploy

- No exponer `/actuator/metrics` sin auth (ya protegido por rol ADMIN)
- Rotar `JWT_SECRET` si sospechas fuga
- Usar password largo para admin bootstrap y luego deshabilitarlo
- Mantener Neon y Render con 2FA
- No pegar tokens reales en capturas para portafolio

### 7) Alternativa Render + Render Postgres

Si usas Render Postgres, reemplaza solo variables `SPRING_DATASOURCE_*` con credenciales de Render Postgres. El resto queda igual.

## Coleccion de requests

Usa `requests/api.http` (VS Code REST Client/IntelliJ HTTP Client) o adapta los curls desde ese archivo.
