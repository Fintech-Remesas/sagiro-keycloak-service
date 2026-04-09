# iam-service

Base técnica del `iam-service` para una plataforma de remesas internacionales orientada a tesis. El servicio adopta Clean Architecture como estructura interna y opera como microservicio REST stateless protegido con JWT emitido por Keycloak.

## Arquitectura

El servicio separa responsabilidades en cuatro zonas:

- `domain`: modelo puro del negocio IAM, sin dependencias de Spring ni JPA.
- `application`: casos de uso, DTOs de aplicación y puertos de entrada/salida.
- `infrastructure`: adaptadores web, seguridad, persistencia, OpenAPI, Keycloak y Kafka.
- `src/main/resources`: configuración `application.yml` y migraciones Flyway solo estructurales.

### Estructura de carpetas

```text
iam-service
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java/com/techi/iamservice
│   │   │   ├── IamServiceApplication.java
│   │   │   ├── domain
│   │   │   │   ├── enums
│   │   │   │   ├── model
│   │   │   │   ├── service
│   │   │   │   └── valueobject
│   │   │   ├── application
│   │   │   │   ├── dto
│   │   │   │   ├── exception
│   │   │   │   ├── mapper
│   │   │   │   ├── port/input
│   │   │   │   ├── port/output
│   │   │   │   └── service
│   │   │   └── infrastructure
│   │   │       ├── config
│   │   │       ├── exception
│   │   │       ├── keycloak
│   │   │       │   ├── adapter
│   │   │       │   ├── client
│   │   │       │   └── dto
│   │   │       ├── messaging/kafka
│   │   │       │   ├── config
│   │   │       │   ├── event
│   │   │       │   └── listener
│   │   │       ├── openapi
│   │   │       ├── persistence
│   │   │       │   ├── adapter
│   │   │       │   ├── entity
│   │   │       │   ├── mapper
│   │   │       │   └── repository
│   │   │       ├── security
│   │   │       ├── shared
│   │   │       └── web
│   │   │           ├── controller
│   │   │           ├── mapper
│   │   │           ├── request
│   │   │           └── response
│   │   └── resources
│   │       ├── application.yml
│   │       └── db/migration/V1__create_iam_schema.sql
│   └── test
│       └── java/com/techi/iamservice/application/service
└── .mvn
```

## Alcance funcional del IAM

- Gestiona identidad digital enlazada con Keycloak mediante `keycloakUserId`.
- Mantiene el perfil base del usuario y el contexto de acceso.
- Proyecta `accountStatus`, `verificationStatus`, `verificationLevel` y `canOperate`.
- No implementa KYC, biometría, validación documental ni AML.
- Solo consume y expone el estado resumido de verificación que enviará un futuro `kyc-service`.

## Responsabilidad IAM vs Keycloak

- Keycloak resuelve login, autenticación, emisión y validación criptográfica de tokens.
- El IAM funciona como `OAuth2 Resource Server`, por lo que valida JWT emitidos por Keycloak.
- El IAM usa integración HTTP administrativa para crear usuarios, buscarlos, asignar roles y deshabilitarlos.
- El IAM nunca almacena contraseñas en PostgreSQL; una contraseña temporal, si se envía, solo se reenvía a Keycloak.

## Requisitos

- Java 21
- Maven 3.9+
- Docker y Docker Compose

## Variables de entorno principales

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP del servicio | `8080` |
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5432/iam_db` |
| `DB_USERNAME` | Usuario PostgreSQL | `iam_user` |
| `DB_PASSWORD` | Password PostgreSQL | `iam_password` |
| `KEYCLOAK_BASE_URL` | URL base de Keycloak | `http://localhost:8081` |
| `KEYCLOAK_REALM` | Realm de tesis | `remittance-thesis` |
| `KEYCLOAK_ISSUER_URI` | Issuer URI para validar JWT | `http://localhost:8081/realms/remittance-thesis` |
| `KEYCLOAK_ADMIN_CLIENT_ID` | Cliente técnico para admin APIs | `iam-admin-client` |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | Secreto del cliente técnico | `change-me` |
| `KEYCLOAK_BACKEND_CLIENT_ID` | Cliente backend/resource server | `iam-backend` |
| `KAFKA_BOOTSTRAP_SERVERS` | Bootstrap servers Kafka | `localhost:9092` |

## Levantar el entorno con Docker

1. Construir y levantar todo:

```bash
docker compose up --build
```

2. Servicios disponibles:

- IAM Service: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Keycloak: `http://localhost:8081`
- Kafka broker: `localhost:9092`

> Nota: la imagen incluida en `docker-compose.yml` usa Keycloak `25.0.6`. Para bootstrapear el usuario administrador en esa versión se emplean `KEYCLOAK_ADMIN` y `KEYCLOAK_ADMIN_PASSWORD`.

## Ejecutar localmente con Maven

1. Levanta dependencias externas:

```bash
docker compose up postgres kafka zookeeper keycloak
```

2. Ejecuta el servicio:

```bash
./mvnw spring-boot:run
```

o

```bash
mvn spring-boot:run
```

3. Inicializa Keycloak para este servicio:

```bash
./scripts/setup-keycloak.sh
```

## Configuración sugerida de Keycloak para la tesis

Configura manualmente estos elementos en Keycloak. No se importan seeds ni archivos de población.

### Realm sugerido

- Realm: `remittance-thesis`

### Clientes sugeridos

- `iam-backend`
  - Tipo: confidential o bearer-only según tu estrategia de backend
  - Uso: resource server del IAM
- `iam-admin-client`
  - Tipo: confidential
  - Service accounts enabled: `true`
  - Uso: token técnico `client_credentials` para admin APIs
- `remittance-frontend`
  - Tipo: public o confidential
  - Uso: frontend web o app móvil

### Roles sugeridos

- `ROLE_CUSTOMER`
- `ROLE_ADMIN`
- `ROLE_COMPLIANCE_AGENT`
- `ROLE_SERVICE`

### Claims mínimos esperados en el JWT

- `sub`: se usa como `keycloakUserId` lógico y principal técnico del usuario autenticado
- `preferred_username`
- `email`
- `realm_access.roles` y/o `resource_access.<client>.roles`

## Endpoints principales

### REST de negocio

- `POST /api/v1/users/register`
- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me/profile`
- `GET /api/v1/users/me/access-context`
- `GET /api/v1/users/{id}/status`
- `PATCH /api/v1/internal/users/{id}/verification-status`

### Endpoints técnicos de prueba

- `GET /api/v1/test/ping`
- `GET /api/v1/test/authenticated`
- `GET /api/v1/test/customer`
- `GET /api/v1/test/admin`
- `GET /api/v1/test/service`

## Probar endpoints autenticados con Bearer Token

1. Obtén un token desde Keycloak para un usuario válido del realm `remittance-thesis`.
2. Abre Swagger en `http://localhost:8080/swagger-ui.html`.
3. Usa el botón `Authorize`.
4. Pega el token con formato:

```text
Bearer eyJ...
```

5. Ejecuta los endpoints protegidos.

## Kafka

El proyecto deja preparada la base de sincronización orientada a eventos mediante estos tópicos:

- `customer.created`
- `customer.activated`
- `customer.disabled`

Los listeners Kafka viven en infraestructura y delegan en casos de uso, manteniendo la lógica de negocio dentro de la capa de aplicación.

## Migraciones

- Flyway se usa solo para estructura.
- No existen `data.sql`, `import.sql`, seeds automáticos ni `INSERT` de población.

## Pruebas

Ejecuta:

```bash
./mvnw test
```

o

```bash
mvn test
```

## Nota arquitectónica para la tesis

Puedes describir esta base así:

> El IAM Service adopta Clean Architecture como estructura interna de diseño, mientras que a nivel de operación se implementa como un microservicio REST stateless, protegido con JWT emitido por Keycloak, con integración HTTP para administración de identidad y con capacidad de sincronización orientada a eventos mediante Kafka.
