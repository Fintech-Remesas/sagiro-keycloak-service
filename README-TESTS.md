# Pruebas de Integración — IAM Service

Documentación completa de las pruebas automatizadas end-to-end del flujo del IAM Service.

---

## Tabla de Contenidos

1. [Requisitos previos](#requisitos-previos)
2. [Cómo ejecutar los tests](#cómo-ejecutar-los-tests)
3. [Flujo cubierto](#flujo-cubierto)
4. [Detalle de cada paso](#detalle-de-cada-paso)
5. [Qué valida cada test](#qué-valida-cada-test)

---

## Requisitos previos

Antes de ejecutar los tests, asegúrate de que los siguientes servicios estén corriendo:

```powershell
# 1. Levantar dependencias externas
docker compose up -d postgres keycloak kafka zookeeper

# 2. Configurar Keycloak (solo primera vez)
$env:PATH += ";$PWD"; & "C:\Program Files\Git\bin\bash.exe" ./scripts/setup-keycloak.sh

# 3. Levantar el IAM Service (en otra terminal)
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd spring-boot:run
```

> El IAM Service debe estar escuchando en `http://localhost:8082` antes de ejecutar los tests.

---

## Cómo ejecutar los tests

### Sólo los tests de integración (flujo completo)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd test -Dtest=FullFlowIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```

### Todos los tests (unitarios + integración)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd test
```

### Solo los tests unitarios (sin necesitar Docker ni el servicio corriendo)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd test -Dtest="!FullFlowIntegrationTest"
```

---

## Flujo cubierto

El test `FullFlowIntegrationTest` ejecuta **16 pasos en orden estricto**, simulando el ciclo de vida completo de un usuario en la plataforma Sagiro:

```
┌─────────────────────────────────────────────────────────────────────┐
│  PASO 1  →  PING              (servicio disponible)                  │
│  PASO 2  →  REGISTER          (crea usuario nuevo)                   │
│  PASO 3  →  LOGIN             (obtiene JWT)                          │
│  PASO 4  →  GET /me           (valida datos recién creados)          │
│  PASO 5  →  ACCESS CONTEXT    (valida roles y canOperate=false)      │
│  PASO 6  →  UPDATE PROFILE    (actualiza nombre, país, preferencias) │
│  PASO 7  →  GET /me           (confirma cambios persistidos)         │
│  PASO 8  →  SIMULATE KYC      (aprueba KYC → canOperate=true)       │
│  PASO 9  →  GET /{id}/status  (valida con token de admin)            │
│  PASO 10 →  RECOVERY REQUEST  (solicita token de recuperación)       │
│  PASO 11 →  RECOVERY RESET    (aplica nueva contraseña)              │
│  PASO 12 →  LOGIN NUEVA PASS  (verifica que funciona)                │
│  PASO 13 →  LOGIN VIEJA PASS  (verifica que falla 401)               │
│  PASO 14 →  REGISTER DUPLIC.  (verifica 409 en duplicado)            │
│  PASO 15 →  LOGIN INVÁLIDO    (verifica 401 en credenciales erróneas)│
│  PASO 16 →  GET /me SIN TOKEN (verifica 401 sin autenticación)       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Detalle de cada paso

### Paso 1 — Ping

**Endpoint:** `GET /api/v1/test/ping`  
**Objetivo:** Verificar que el servicio está disponible y responde sin autenticación.  
**Valida:**
- HTTP 200
- `status = "SUCCESS"`
- `data.message = "pong"`

---

### Paso 2 — Register

**Endpoint:** `POST /api/v1/users/register`  
**Objetivo:** Crear un nuevo usuario con datos únicos (sufijo aleatorio por ejecución para evitar conflictos).  
**Valida:**
- HTTP 201
- `data.accountStatus = "REGISTERED"`
- `data.verificationStatus = "NOT_STARTED"`
- `data.verificationLevel = "NONE"`
- `data.canOperate = false`
- `data.enabled = true`
- `data.profile.country = "PE"`
- Guarda el `data.id` para usarlo en pasos siguientes.

---

### Paso 3 — Login

**Endpoint:** `POST /api/v1/users/login`  
**Objetivo:** Autenticar al usuario recién creado y obtener un JWT válido.  
**Valida:**
- HTTP 200
- `data.accessToken` no nulo
- `data.refreshToken` no nulo
- `data.tokenType = "Bearer"`
- `data.expiresIn > 0`
- Guarda el `accessToken` para pasos siguientes.

---

### Paso 4 — GET /me (datos iniciales)

**Endpoint:** `GET /api/v1/users/me`  
**Header:** `Authorization: Bearer <token>`  
**Objetivo:** Confirmar que el usuario recién registrado existe en el IAM con los datos correctos.  
**Valida:**
- HTTP 200
- Email, username, firstName, lastName correctos
- `accountStatus = "REGISTERED"`
- `profile.country = "PE"`

---

### Paso 5 — Access Context

**Endpoint:** `GET /api/v1/users/me/access-context`  
**Header:** `Authorization: Bearer <token>`  
**Objetivo:** Verificar que el JWT incluye `ROLE_CUSTOMER` y que `canOperate=false` antes del KYC.  
**Valida:**
- HTTP 200
- `data.roles` contiene `"ROLE_CUSTOMER"`
- `data.accountStatus = "REGISTERED"`
- `data.canOperate = false`

---

### Paso 6 — Update Profile

**Endpoint:** `PATCH /api/v1/users/me/profile`  
**Header:** `Authorization: Bearer <token>`  
**Objetivo:** Actualizar datos del perfil (nombre, país, idioma, preferencias visuales).  
**Valida:**
- HTTP 200
- `data.firstName = "Flujo Actualizado"`
- `data.lastName = "Test Vega"`
- `data.profile.country = "MX"`
- `data.profile.preferredLanguage = "en"`
- `data.profile.blockchainVisibilityEnabled = true`
- `data.profile.darkModeEnabled = true`

---

### Paso 7 — GET /me (post-actualización)

**Endpoint:** `GET /api/v1/users/me`  
**Header:** `Authorization: Bearer <token>`  
**Objetivo:** Confirmar que la actualización del perfil se persistió correctamente en la BD.  
**Valida:** Mismos campos del paso 6, pero leyendo desde el servidor.

---

### Paso 8 — Simulate KYC

**Endpoint:** `POST /api/v1/users/me/simulate-kyc`  
**Header:** `Authorization: Bearer <token>`  
**Objetivo:** Simular aprobación KYC y verificar que el estado de la cuenta se actualiza.  
**Valida:**
- HTTP 200
- `data.verificationStatus = "VERIFIED"`
- `data.verificationLevel = "BASIC"`
- `data.canOperate = true`
- `data.accountStatus = "ACTIVE"`

---

### Paso 9 — GET /{id}/status con token de Admin

**Endpoint:** `GET /api/v1/users/{id}/status`  
**Obtiene admin token de:** `POST http://localhost:8081/realms/sagiro/protocol/openid-connect/token` con `client_credentials`  
**Objetivo:** Verificar el estado de un usuario desde la perspectiva de un servicio administrador.  
**Valida:**
- Admin token obtenido correctamente desde Keycloak
- HTTP 200 en el endpoint de status
- `data.accountStatus = "ACTIVE"`
- `data.verificationStatus = "VERIFIED"`
- `data.canOperate = true`

---

### Paso 10 — Password Recovery Request

**Endpoint:** `POST /api/v1/users/password-recovery/request`  
**Objetivo:** Solicitar un token de recuperación de contraseña (la respuesta siempre es 202 por seguridad).  
**Valida:**
- HTTP 202
- `status = "SUCCESS"`
- `message` contiene `"recovery token"`
- El token se persiste en la BD (verificado en el paso 11)

---

### Paso 11 — Password Recovery Reset

**Endpoint:** `POST /api/v1/users/password-recovery/reset`  
**Objetivo:** Usar el token de recuperación para cambiar la contraseña.  
**Mecanismo:** El test extrae el token directamente de la BD PostgreSQL vía JDBC (ya que en producción el communication-service lo envía por email/SMS).  
**Valida:**
- HTTP 200
- `status = "SUCCESS"`
- `message` contiene `"Password reset successfully"`

---

### Paso 12 — Login con nueva contraseña

**Endpoint:** `POST /api/v1/users/login`  
**Objetivo:** Confirmar que la nueva contraseña funciona correctamente en Keycloak.  
**Valida:**
- HTTP 200
- `data.accessToken` no nulo
- `data.tokenType = "Bearer"`

---

### Paso 13 — Login con contraseña vieja (debe fallar)

**Endpoint:** `POST /api/v1/users/login`  
**Objetivo:** Confirmar que la contraseña anterior fue invalidada por Keycloak.  
**Valida:**
- HTTP 401

---

### Paso 14 — Register duplicado (debe fallar)

**Endpoint:** `POST /api/v1/users/register`  
**Objetivo:** Verificar que no se puede registrar dos veces el mismo email/username.  
**Valida:**
- HTTP 409 (Conflict)

---

### Paso 15 — Login con credenciales inválidas (debe fallar)

**Endpoint:** `POST /api/v1/users/login`  
**Objetivo:** Verificar que Keycloak rechaza credenciales incorrectas.  
**Valida:**
- HTTP 401

---

### Paso 16 — GET /me sin token (debe fallar)

**Endpoint:** `GET /api/v1/users/me`  
**Objetivo:** Verificar que los endpoints protegidos requieren autenticación.  
**Valida:**
- HTTP 401

---

## Qué valida cada test

| Paso | Endpoint | HTTP | Qué valida |
|------|----------|------|------------|
| 1 | `GET /test/ping` | 200 | Servicio disponible |
| 2 | `POST /users/register` | 201 | Registro exitoso, estado inicial correcto |
| 3 | `POST /users/login` | 200 | JWT emitido con campos completos |
| 4 | `GET /users/me` | 200 | Datos del usuario en IAM |
| 5 | `GET /users/me/access-context` | 200 | Roles del JWT, canOperate inicial |
| 6 | `PATCH /users/me/profile` | 200 | Actualización persistida en respuesta |
| 7 | `GET /users/me` | 200 | Actualización confirmada en BD |
| 8 | `POST /users/me/simulate-kyc` | 200 | KYC aprobado, canOperate=true, ACTIVE |
| 9 | `GET /users/{id}/status` | 200 | Vista de admin, estado VERIFIED |
| 10 | `POST /password-recovery/request` | 202 | Anti-enumeración, token generado |
| 11 | `POST /password-recovery/reset` | 200 | Contraseña cambiada en Keycloak |
| 12 | `POST /users/login` | 200 | Nueva contraseña funciona |
| 13 | `POST /users/login` | 401 | Contraseña vieja invalidada |
| 14 | `POST /users/register` | 409 | Duplicado rechazado |
| 15 | `POST /users/login` | 401 | Credenciales inválidas rechazadas |
| 16 | `GET /users/me` | 401 | Endpoint protegido requiere token |

---

## Archivo del test

[FullFlowIntegrationTest.java](file:///c:/Users/USUARIO/Documents/La%20Techi/sagiro/sagiro-keycloak-service/src/test/java/com/sagiro/iamservice/integration/FullFlowIntegrationTest.java)
