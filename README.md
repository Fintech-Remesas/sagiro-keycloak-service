# IAM Service — `sagiro-keycloak-service`

Base técnica del microservicio de Identidad y Acceso (IAM) para la plataforma de remesas internacionales **Sagiro**. Adopta Clean Architecture como estructura interna y opera como microservicio REST stateless protegido con JWT emitido por Keycloak.

---

## Tabla de Contenidos

1. [Arquitectura](#arquitectura)
2. [¿Necesito levantar una base de datos?](#necesito-levantar-una-base-de-datos)
3. [Variables de entorno](#variables-de-entorno)
4. [Levantar el entorno con Docker](#levantar-el-entorno-con-docker)
5. [Compilar y ejecutar localmente con Maven](#compilar-y-ejecutar-localmente-con-maven)
6. [Configuración de Keycloak](#configuración-de-keycloak)
7. [Endpoints REST](#endpoints-rest)
   - [POST /register](#post-apiv1usersregister)
   - [POST /login](#post-apiv1userslogin)
   - [GET /me](#get-apiv1usersme)
   - [PATCH /me/profile](#patch-apiv1usersmeperfil)
   - [GET /me/access-context](#get-apiv1usersmeperfil)
   - [GET /{id}/status](#get-apiv1usersidstatus)
   - [POST /me/simulate-kyc](#post-apiv1usersme-simulate-kyc)
   - [POST /password-recovery/request](#post-apiv1userspassword-recoveryrequest)
   - [POST /password-recovery/reset](#post-apiv1userspassword-recoveryreset)
   - [Endpoints Financieros (Tarjetas y Cuentas Bancarias)](#endpoints-financieros-tarjetas-y-cuentas-bancarias)
   - [Búsqueda de Usuarios](#búsqueda-de-usuarios)
   - [Endpoints internos](#endpoints-internos)
   - [Endpoints de prueba](#endpoints-de-prueba)
8. [Recuperación de contraseña – Flujo completo](#recuperación-de-contraseña--flujo-completo)
9. [Kafka](#kafka)
10. [Migraciones de base de datos](#migraciones-de-base-de-datos)
11. [Pruebas](#pruebas)
12. [Nota arquitectónica](#nota-arquitectónica)

---

## Arquitectura

El servicio separa responsabilidades en cuatro zonas:

- **`domain`** — Modelo puro del negocio IAM, sin dependencias de Spring ni JPA.
- **`application`** — Casos de uso, DTOs de aplicación y puertos de entrada/salida.
- **`infrastructure`** — Adaptadores web, seguridad, persistencia, OpenAPI, Keycloak y Kafka.
- **`src/main/resources`** — Configuración `application.yml` y migraciones Flyway estructurales.

### Estructura de carpetas

```text
iam-service
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/sagiro/iamservice
    │   │   ├── IamServiceApplication.java
    │   │   ├── domain
    │   │   │   ├── enums
    │   │   │   ├── model
    │   │   │   ├── service
    │   │   │   └── valueobject
    │   │   ├── application
    │   │   │   ├── dto
    │   │   │   ├── event
    │   │   │   ├── exception
    │   │   │   ├── mapper
    │   │   │   ├── port/input
    │   │   │   ├── port/output
    │   │   │   └── service
    │   │   └── infrastructure
    │   │       ├── config
    │   │       ├── exception
    │   │       ├── keycloak
    │   │       │   ├── adapter
    │   │       │   ├── client
    │   │       │   └── dto
    │   │       ├── messaging/kafka
    │   │       │   ├── config
    │   │       │   ├── event
    │   │       │   ├── listener
    │   │       │   └── producer
    │   │       ├── openapi
    │   │       ├── persistence
    │   │       │   ├── adapter
    │   │       │   ├── entity
    │   │       │   ├── mapper
    │   │       │   └── repository
    │   │       ├── security
    │   │       ├── shared
    │   │       └── web
    │   │           ├── controller
    │   │           ├── mapper
    │   │           ├── request
    │   │           └── response
    │   └── resources
    │       ├── application.yml
    │       └── db/migration
    │           ├── V1__create_iam_schema.sql
    │           └── V2__add_password_reset_token.sql
    └── test
        └── java/com/sagiro/iamservice/application/service
```

---

## ¿Necesito levantar una base de datos?

**Sí.** El servicio utiliza **PostgreSQL** como base de datos local para:

| Tabla | Propósito |
|---|---|
| `iam_users` | Identidad, estado de cuenta, estado de verificación y token de recuperación de contraseña |
| `user_profiles` | Preferencias del usuario (país, idioma, modo oscuro, visibilidad blockchain) |

> **Importante:** El IAM **nunca almacena contraseñas** en PostgreSQL. Las credenciales viven exclusivamente en Keycloak. La base de datos local solo guarda el token temporal de recuperación de contraseña (con TTL de 15 minutos) hasta que el usuario completa el flujo.

La base de datos se provisiona automáticamente via Flyway con dos migraciones:
- `V1__create_iam_schema.sql` — tablas `iam_users` y `user_profiles`
- `V2__add_password_reset_token.sql` — columnas `password_reset_token` y `password_reset_token_expires_at`

---

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | `8082` |
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5433/iam_db` |
| `DB_USERNAME` | Usuario PostgreSQL | `iam_user` |
| `DB_PASSWORD` | Password PostgreSQL | `iam_password` |
| `KEYCLOAK_BASE_URL` | URL base de Keycloak | `http://localhost:8081` |
| `KEYCLOAK_REALM` | Realm de Sagiro | `sagiro` |
| `KEYCLOAK_ISSUER_URI` | Issuer URI para validar JWT | `http://localhost:8081/realms/sagiro` |
| `KEYCLOAK_ADMIN_CLIENT_ID` | Cliente técnico para admin APIs | `iam-admin-client` |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | Secreto del cliente técnico admin | `change-me` |
| `KEYCLOAK_BACKEND_CLIENT_ID` | Cliente backend (resource server) | `iam-backend` |
| `KEYCLOAK_BACKEND_CLIENT_SECRET` | Secreto del cliente backend (para login) | `change-me` |
| `KAFKA_BOOTSTRAP_SERVERS` | Bootstrap servers Kafka | `localhost:9092` |
| `KAFKA_TOPIC_CUSTOMER_CREATED` | Tópico de cliente creado | `customer.created` |
| `KAFKA_TOPIC_CUSTOMER_ACTIVATED` | Tópico de cliente activado | `customer.activated` |
| `KAFKA_TOPIC_CUSTOMER_DISABLED` | Tópico de cliente desactivado | `customer.disabled` |
| `KAFKA_TOPIC_PASSWORD_RECOVERY_REQUESTED` | Tópico de recuperación de contraseña | `password-recovery.requested` |
| `PASSWORD_RESET_TOKEN_TTL_MINUTES` | TTL en minutos para el token de recuperación | `15` |
| `AES_ENCRYPTION_KEY` | Llave secreta para cifrado AES-256-GCM (32 caracteres min) | `V2hhdGV2ZXJTdWperFNlY3JldEtleUJhczY0==` |

---

## Levantar el entorno con Docker

**Opción ideal para probar el proyecto rápidamente sin instalar Java ni Maven.**

1. **Asegúrate de estar en la raíz del proyecto** (`sagiro/sagiro-keycloak-service`).
2. **Configura tu llave de encriptación AES-256** antes de levantar el contenedor. Puedes configurar una variable de entorno en tu terminal:
   - En Linux/Mac/Git Bash: `export AES_ENCRYPTION_KEY=12345678901234567890123456789012`
   - En Windows CMD: `set AES_ENCRYPTION_KEY=12345678901234567890123456789012`
   *(Alternativamente, esta variable ya está definida por defecto en tu `application.yml` local para entornos de desarrollo).*
3. **Levanta todos los contenedores** (Base de datos, Keycloak, Kafka, Zookeeper y el propio microservicio IAM):
   ```bash
   docker compose up --build -d
   ```
4. **Inicializa la configuración de Keycloak** (Esto creará el Realm, los clientes y roles requeridos para que funcione):
   ```bash
   ./scripts/setup-keycloak.sh
   ```
   *(En Windows puedes ejecutar este script usando Git Bash o WSL)*

**Servicios disponibles una vez levantado:**

| Servicio | URL |
|---|---|
| IAM Service | `http://localhost:8082` |
| Swagger UI | `http://localhost:8082/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8082/v3/api-docs` |
| Health | `http://localhost:8082/actuator/health` |
| Keycloak Admin | `http://localhost:8081` (admin / admin) |
| Kafka broker | `localhost:9092` |

---

## Compilar y ejecutar localmente con Maven

**Opción ideal para desarrollo.** Requiere Java 21 (según el `pom.xml`) y Maven instalados en tu máquina.

### Compilar y empaquetar el proyecto (Sin requerir Docker)

Para compilar el proyecto o generar el archivo JAR ejecutable, **no es necesario** tener los contenedores de Docker activos, ya que los tests actuales de la aplicación son unitarios (mocks) y no dependen de servicios externos durante el build.

* **Solo compilar (verificar errores de compilación):**
  ```bash
  mvn clean compile
  ```
  *(O con el Maven Wrapper: `./mvnw clean compile` o `.\mvnw.cmd clean compile`)*

* **Compilar y construir el archivo JAR ejecutable (ejecutando las pruebas unitarias):**
  ```bash
  mvn clean package
  ```
  *(El archivo `.jar` generado se guardará en el directorio `target/`)*

* **Compilar y construir el archivo JAR saltándose las pruebas:**
  ```bash
  mvn clean package -DskipTests
  ```

> ⚠️ **Nota para usuarios de Windows:** Si la ruta de tu proyecto contiene espacios (como `La Techi`), el Maven Wrapper (`mvnw` / `mvnw.cmd`) puede fallar al descargar/resolver dependencias debido a un bug conocido con rutas que contienen espacios en PowerShell/CMD (ej. `El sistema no puede encontrar el archivo...`). Si te enfrentas a este error, la solución recomendada es utilizar el comando global **`mvn`** directamente (asegurándote de tener Maven instalado en tu sistema y configurado en tu PATH).

### Ejecutar el microservicio localmente (Requiere Docker)

Para **ejecutar** la aplicación (a través de `mvn spring-boot:run` o iniciando el `.jar`), **sí es obligatorio** tener corriendo los contenedores de Docker con las dependencias externas (Base de datos PostgreSQL, Keycloak y Kafka). Si no están activos, el microservicio fallará al arrancar debido a errores de conexión rechazada (por ejemplo, `Connection to localhost:5433 refused`).

1. **Levantar dependencias externas:**
   ```bash
   docker compose up -d postgres keycloak kafka zookeeper
   ```
2. **Inicializar configuración de Keycloak** (solo es necesario la primera vez):
   ```bash
   ./scripts/setup-keycloak.sh
   ```
   *(En Windows puedes ejecutar este script usando Git Bash o WSL)*
3. **Ejecutar el microservicio:**
   ```bash
   mvn spring-boot:run
   ```
   *(O con el Wrapper si no tienes espacios en tu ruta: `./mvnw spring-boot:run` o `.\mvnw.cmd spring-boot:run`)*

---

## Configuración de Keycloak

### Realm

- Nombre: `sagiro`

### Clientes requeridos

| Cliente | Tipo | Service Account | Uso |
|---|---|---|---|
| `iam-admin-client` | confidential | ✅ habilitado | Token técnico `client_credentials` para Keycloak Admin API |
| `iam-backend` | confidential | ❌ | Resource Server. También usado en el flujo de login con `password` grant |

> Para el endpoint de login, el cliente `iam-backend` debe tener habilitado el **Direct Access Grants** (Resource Owner Password Credentials grant) en Keycloak.

### Roles de realm requeridos

- `ROLE_CUSTOMER`
- `ROLE_ADMIN`
- `ROLE_COMPLIANCE_AGENT`
- `ROLE_SERVICE`

### Claims mínimos en el JWT

| Claim | Uso |
|---|---|
| `sub` | Identificador del usuario en Keycloak (keycloakUserId) |
| `preferred_username` | Username del usuario |
| `email` | Email del usuario |
| `realm_access.roles` | Roles del realm para autorización |

---

## Endpoints REST

> Accede al Swagger interactivo en: `http://localhost:8082/swagger-ui.html`

### `POST /api/v1/users/register`

Registra un usuario en el IAM y provisiona la identidad en Keycloak. No requiere Bearer token.

**Request body:**
```json
{
  "email": "ana.customer@techi.test",
  "username": "ana.customer",
  "phone": "+51999999999",
  "firstName": "Ana",
  "lastName": "Torres",
  "country": "PE",
  "preferredLanguage": "es",
  "initialPassword": "TempPass#2026"
}
```

| Campo | Tipo | Requerido | Validación | Descripción |
|---|---|---|---|---|
| `email` | string | ✅ | `@Email`, max 120 | Email único del usuario |
| `username` | string | ✅ | max 50 | Username único para login |
| `phone` | string | ❌ | max 30 | Teléfono en formato E.164 |
| `firstName` | string | ✅ | max 80 | Nombre |
| `lastName` | string | ✅ | max 80 | Apellido |
| `country` | string | ✅ | max 3 | Código ISO 3166-1 alpha-2 (ej: `PE`, `MX`, `US`) |
| `preferredLanguage` | string | ✅ | max 10 | Código ISO 639-1 (ej: `es`, `en`) |
| `initialPassword` | string | ❌ | max 120 | Contraseña temporal. Si se omite, Keycloak exigirá que el usuario la defina en el primer login |

**Response `201 Created`:**
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "keycloakUserId": "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f",
    "email": "ana.customer@techi.test",
    "username": "ana.customer",
    "phone": "+51999999999",
    "firstName": "Ana",
    "lastName": "Torres",
    "accountStatus": "REGISTERED",
    "verificationStatus": "NOT_STARTED",
    "verificationLevel": "NONE",
    "verificationUpdatedAt": null,
    "canOperate": false,
    "enabled": true,
    "createdAt": "2026-06-06T19:00:00Z",
    "updatedAt": "2026-06-06T19:00:00Z",
    "profile": {
      "id": "7d2b4e8a-1234-4bcd-9876-abcdef012345",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "country": "PE",
      "preferredLanguage": "es",
      "blockchainVisibilityEnabled": false,
      "darkModeEnabled": false
    }
  }
}
```

**Errores:**
- `400` — Campos inválidos o faltantes
- `409` — Email o username ya existe en IAM o Keycloak

---

### `POST /api/v1/users/login`

Autentica al usuario vía Keycloak (Resource Owner Password Credentials grant). No requiere Bearer token.

> **Nota de Keycloak:** El cliente `iam-backend` debe tener **Direct Access Grants** habilitado.

**Request body:**
```json
{
  "usernameOrEmail": "ana.customer@techi.test",
  "password": "TempPass#2026"
}
```

| Campo | Tipo | Requerido | Validación | Descripción |
|---|---|---|---|---|
| `usernameOrEmail` | string | ✅ | max 120 | Username o email registrado |
| `password` | string | ✅ | max 120 | Contraseña del usuario en Keycloak |

**Response `200 OK`:**
```json
{
  "status": "SUCCESS",
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6...",
    "expiresIn": 300,
    "refreshExpiresIn": 1800,
    "tokenType": "Bearer"
  }
}
```

| Campo respuesta | Descripción |
|---|---|
| `accessToken` | JWT de acceso. Usar en el header `Authorization: Bearer <token>` |
| `refreshToken` | Token para renovar el acceso cuando expire |
| `expiresIn` | Segundos hasta que expire el access token |
| `refreshExpiresIn` | Segundos hasta que expire el refresh token |
| `tokenType` | Siempre `Bearer` |

**Errores:**
- `400` — Campos inválidos
- `401` — Credenciales incorrectas

---

### `GET /api/v1/users/me`

Retorna el usuario IAM autenticado. Requiere Bearer token.

**Headers:** `Authorization: Bearer <access_token>`

**Response `200 OK`:**
```json
{
  "status": "SUCCESS",
  "message": "Authenticated user resolved",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "keycloakUserId": "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f",
    "email": "ana.customer@techi.test",
    "username": "ana.customer",
    "phone": "+51999999999",
    "firstName": "Ana",
    "lastName": "Torres",
    "accountStatus": "ACTIVE",
    "verificationStatus": "NOT_STARTED",
    "verificationLevel": "NONE",
    "verificationUpdatedAt": null,
    "canOperate": false,
    "enabled": true,
    "createdAt": "2026-06-06T19:00:00Z",
    "updatedAt": "2026-06-06T19:00:00Z",
    "profile": {
      "id": "7d2b4e8a-1234-4bcd-9876-abcdef012345",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "country": "PE",
      "preferredLanguage": "es",
      "blockchainVisibilityEnabled": false,
      "darkModeEnabled": true
    }
  }
}
```

**Errores:** `401`, `404`

---

### `PATCH /api/v1/users/me/profile`

Actualiza el perfil del usuario autenticado. Requiere Bearer token.

**Headers:** `Authorization: Bearer <access_token>`

**Request body:**
```json
{
  "phone": "+51911111111",
  "firstName": "Ana Maria",
  "lastName": "Torres Vega",
  "country": "PE",
  "preferredLanguage": "es",
  "blockchainVisibilityEnabled": false,
  "darkModeEnabled": true
}
```

| Campo | Tipo | Requerido | Validación | Descripción |
|---|---|---|---|---|
| `phone` | string | ❌ | max 30 | Nuevo número de teléfono |
| `firstName` | string | ✅ | max 80 | Nombre actualizado |
| `lastName` | string | ✅ | max 80 | Apellido actualizado |
| `country` | string | ✅ | max 3 | Código ISO país de residencia |
| `preferredLanguage` | string | ✅ | max 10 | Código ISO idioma preferido |
| `blockchainVisibilityEnabled` | boolean | ✅ | — | Mostrar/ocultar transacciones blockchain en el frontend |
| `darkModeEnabled` | boolean | ✅ | — | Preferencia de modo oscuro |

**Response `200 OK`:** mismo esquema que `GET /me`

**Errores:** `400`, `401`, `404`

---

### `GET /api/v1/users/me/access-context`

Retorna el contexto de acceso completo del usuario autenticado. Requiere Bearer token. Ideal para uso del API Gateway.

**Headers:** `Authorization: Bearer <access_token>`

**Response `200 OK`:**
```json
{
  "status": "SUCCESS",
  "message": "Access context resolved",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "keycloakUserId": "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f",
    "subject": "kc-9b2e3f4a-12bc-4def-8765-1a2b3c4d5e6f",
    "username": "ana.customer",
    "email": "ana.customer@techi.test",
    "roles": ["ROLE_CUSTOMER"],
    "accountStatus": "ACTIVE",
    "verificationStatus": "NOT_STARTED",
    "verificationLevel": "NONE",
    "canOperate": false,
    "enabled": true
  }
}
```

**Errores:** `401`

---

### `GET /api/v1/users/{id}/status`

Retorna el estado IAM de un usuario por su ID. Requiere `ROLE_ADMIN` o `ROLE_SERVICE`.

**Path param:** `id` — UUID del usuario IAM (campo `id` de la tabla `iam_users`)

**Headers:** `Authorization: Bearer <admin_or_service_token>`

**Response `200 OK`:**
```json
{
  "status": "SUCCESS",
  "message": "User status resolved",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "accountStatus": "ACTIVE",
    "verificationStatus": "VERIFIED",
    "verificationLevel": "BASIC",
    "canOperate": true,
    "enabled": true,
    "verificationUpdatedAt": "2026-06-01T10:00:00Z"
  }
}
```

| `accountStatus` valores | Descripción |
|---|---|
| `REGISTERED` | Recién registrado, pendiente de activación |
| `ACTIVE` | Activo y operativo |
| `SUSPENDED` | Suspendido temporalmente |
| `BLOCKED` | Bloqueado permanentemente |

| `verificationStatus` valores | Descripción |
|---|---|
| `NOT_STARTED` | KYC no iniciado |
| `IN_PROGRESS` | KYC en proceso |
| `VERIFIED` | KYC aprobado |
| `REJECTED` | KYC rechazado |

**Errores:** `401`, `403`, `404`

---

### `POST /api/v1/users/me/simulate-kyc`

**⚠️ Solo para pruebas.** Simula una aprobación KYC instantánea. Requiere Bearer token.

**Headers:** `Authorization: Bearer <access_token>`

**Request body:** ninguno

**Response `200 OK`:**
```json
{
  "status": "SUCCESS",
  "message": "KYC simulated successfully",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "accountStatus": "ACTIVE",
    "verificationStatus": "VERIFIED",
    "verificationLevel": "BASIC",
    "canOperate": true,
    "enabled": true,
    "verificationUpdatedAt": "2026-06-06T19:30:00Z"
  }
}
```

---

### `POST /api/v1/users/password-recovery/request`

Inicia el flujo de recuperación de contraseña. No requiere Bearer token.

> **Seguridad:** Siempre devuelve `202 Accepted` independientemente de si el email existe o no, para prevenir ataques de enumeración de usuarios.
>
> El token generado se publica en Kafka (`password-recovery.requested`) para que el **communication-service** lo entregue al usuario vía email o SMS.

**Request body:**
```json
{
  "email": "ana.customer@techi.test"
}
```

| Campo | Tipo | Requerido | Validación | Descripción |
|---|---|---|---|---|
| `email` | string | ✅ | `@Email` | Email de la cuenta a recuperar |

**Response `202 Accepted`:**
```json
{
  "status": "SUCCESS",
  "message": "If an account with that email exists, a recovery token has been sent",
  "data": null
}
```

**Errores:** `400` (formato de email inválido)

---

### `POST /api/v1/users/password-recovery/reset`

Completa el flujo de recuperación aplicando la nueva contraseña. No requiere Bearer token.

El `token` es el UUID de un solo uso recibido del communication-service. Expira en **15 minutos** (configurable con `PASSWORD_RESET_TOKEN_TTL_MINUTES`).

**Request body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewSecure#Pass2026"
}
```

| Campo | Tipo | Requerido | Validación | Descripción |
|---|---|---|---|---|
| `token` | string | ✅ | — | Token de un solo uso recibido por email/SMS |
| `newPassword` | string | ✅ | min 8, max 120 | Nueva contraseña (debe cumplir la política de Keycloak) |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Password reset successfully",
  "data": null
}
```

**Errores:**
- `400` — Campos inválidos
- `404` — Token no encontrado o ya utilizado
- `409` — Token expirado. Solicitar uno nuevo con `/password-recovery/request`

---

### Endpoints internos

#### `PATCH /api/v1/internal/users/{id}/verification-status`

Actualiza el estado de verificación de un usuario (invocado por servicios internos como el futuro `kyc-service`). Requiere `ROLE_ADMIN` o `ROLE_SERVICE`.

**Path param:** `id` — UUID del usuario IAM

**Headers:** `Authorization: Bearer <service_token>`

**Request body:**
```json
{
  "verificationStatus": "VERIFIED",
  "verificationLevel": "BASIC"
}
```

**Response `200 OK`:** esquema `UserStatusResponse`

---

### Endpoints de prueba

| Endpoint | Autenticación | Rol requerido |
|---|---|---|
| `GET /api/v1/test/ping` | Ninguna | — |
| `GET /api/v1/test/authenticated` | Bearer | Cualquiera |
| `GET /api/v1/test/customer` | Bearer | `ROLE_CUSTOMER` |
| `GET /api/v1/test/admin` | Bearer | `ROLE_ADMIN` |
| `GET /api/v1/test/service` | Bearer | `ROLE_SERVICE` |

---

## Recuperación de contraseña – Flujo completo

```
Usuario             IAM Service              PostgreSQL            Kafka                communication-service (futuro)
   │                    │                        │                    │                           │
   │ POST /password-    │                        │                    │                           │
   │ recovery/request   │                        │                    │                           │
   │──────────────────>│                        │                    │                           │
   │                    │ findByEmail()           │                    │                           │
   │                    │───────────────────────>│                    │                           │
   │                    │ User found              │                    │                           │
   │                    │<───────────────────────│                    │                           │
   │                    │ generateToken (UUID)    │                    │                           │
   │                    │ setExpiresAt (now+15m)  │                    │                           │
   │                    │ save(user)              │                    │                           │
   │                    │───────────────────────>│                    │                           │
   │                    │ publish(event)          │                    │                           │
   │                    │───────────────────────────────────────────>│                           │
   │  202 Accepted      │                        │                    │ PasswordRecoveryEvent     │
   │<──────────────────│                        │                    │───────────────────────────>│
   │                    │                        │                    │  (email/SMS con el token) │
   │                    │                        │                    │                           │
   │ [Usuario recibe el token]                   │                    │                           │
   │                    │                        │                    │                           │
   │ POST /password-    │                        │                    │                           │
   │ recovery/reset     │                        │                    │                           │
   │──────────────────>│                        │                    │                           │
   │                    │ findByToken()           │                    │                           │
   │                    │───────────────────────>│                    │                           │
   │                    │ validate expiry         │                    │                           │
   │                    │ resetPassword(Keycloak) │                    │                           │
   │                    │ clearToken()            │                    │                           │
   │                    │ save(user)              │                    │                           │
   │                    │───────────────────────>│                    │                           │
   │  200 OK            │                        │                    │                           │
   │<──────────────────│                        │                    │                           │
```

---

## Kafka

### Tópicos consumidos

| Tópico | Evento | Acción |
|---|---|---|
| `customer.created` | `CustomerCreatedEvent` | Provisiona el usuario en IAM con `accountStatus=REGISTERED` |
| `customer.activated` | `CustomerActivatedEvent` | Actualiza el usuario con `accountStatus=ACTIVE` |
| `customer.disabled` | `CustomerDisabledEvent` | Desactiva usuario en IAM y deshabilita identidad en Keycloak |

### Tópicos publicados

| Tópico | Evento | Producido por |
|---|---|---|
| `password-recovery.requested` | `PasswordRecoveryEvent` | Flujo de recuperación de contraseña |

**Estructura del evento `PasswordRecoveryEvent`:**
```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "ana.customer@techi.test",
  "username": "ana.customer",
  "resetToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenExpiresAt": "2026-06-06T19:45:00Z",
  "requestedAt": "2026-06-06T19:30:00Z"
}
```

> **El communication-service** (por crear) debe consumir este tópico y enviar el `resetToken` al usuario por el canal apropiado (email o SMS). El token tiene una vida útil de **15 minutos** por defecto.

---

## Migraciones de base de datos

Flyway aplica automáticamente las migraciones al iniciar el servicio. **No hay `data.sql`, seeds ni INSERTs de población.**

| Versión | Archivo | Descripción |
|---|---|---|
| V1 | `V1__create_iam_schema.sql` | Tablas `iam_users` y `user_profiles` con índices |
| V2 | `V2__add_password_reset_token.sql` | Columnas de recuperación de contraseña en `iam_users` |

---

## Pruebas

```bash
mvn test
```

Suites de prueba unitarias actuales:

- `RegisterUserServiceTest`
- `GetAccessContextServiceTest`
- `SimulateKycVerificationServiceTest`
- `UpdateVerificationStatusServiceTest`

---

## Nota arquitectónica

> El IAM Service adopta Clean Architecture como estructura interna de diseño, mientras que a nivel de operación se implementa como un microservicio REST stateless, protegido con JWT emitido por Keycloak, con integración HTTP para administración de identidad y con capacidad de sincronización orientada a eventos mediante Kafka.
>
> **Separación de responsabilidades:**
> - **Keycloak** resuelve login, autenticación, emisión y validación criptográfica de tokens.
> - **El IAM Service** mantiene el estado de la cuenta, el perfil de usuario, el contexto de acceso, y la proyección resumida de verificación que emitirá el futuro `kyc-service`.
> - **El communication-service** (por crear) entregará notificaciones (email/SMS) consumiendo eventos Kafka emitidos por el IAM Service.
> - **El API Gateway** (por crear) utilizará el endpoint `/me/access-context` para tomar decisiones de autorización sin consultas adicionales.
