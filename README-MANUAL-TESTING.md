# Guía de Pruebas Manuales — IAM Service

Flujo paso a paso para probar **todos los endpoints del IAM Service** manualmente usando Swagger UI o cualquier cliente HTTP (Postman, curl, Insomnia).

> **Swagger UI disponible en:** http://localhost:8082/swagger-ui.html  
> **Keycloak Admin UI en:** http://localhost:8081 (usuario: `admin`, contraseña: `admin`)

---

## Antes de empezar

Asegúrate de que todo esté corriendo:

```powershell
docker compose up -d postgres keycloak kafka zookeeper
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd spring-boot:run
```

---

## PASO 1 — Verificar que el servicio está disponible

**Endpoint:** `GET http://localhost:8082/api/v1/test/ping`  
**Autenticación:** Ninguna

**Ejecuta:**
```http
GET http://localhost:8082/api/v1/test/ping
```

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "message": "IAM service is reachable",
  "data": { "message": "pong" }
}
```

---

## PASO 2 — Registrar un nuevo usuario

**Endpoint:** `POST http://localhost:8082/api/v1/users/register`  
**Autenticación:** Ninguna

**Body:**
```json
{
  "email": "ana.sagiro@techi.test",
  "username": "ana.sagiro",
  "phone": "+51999888777",
  "firstName": "Ana",
  "lastName": "Sagiro",
  "country": "PE",
  "preferredLanguage": "es",
  "initialPassword": "Sagiro#Test2026"
}
```

**✅ Respuesta esperada (201):**
```json
{
  "status": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "id": "<GUARDA_ESTE_UUID>",
    "email": "ana.sagiro@techi.test",
    "username": "ana.sagiro",
    "accountStatus": "REGISTERED",
    "verificationStatus": "NOT_STARTED",
    "verificationLevel": "NONE",
    "canOperate": false,
    "enabled": true,
    "profile": {
      "country": "PE",
      "preferredLanguage": "es",
      "blockchainVisibilityEnabled": false,
      "darkModeEnabled": false
    }
  }
}
```

> 📝 **Guarda el campo `data.id`** — lo necesitarás en el Paso 9.

---

## PASO 3 — Iniciar sesión y obtener el JWT

**Endpoint:** `POST http://localhost:8082/api/v1/users/login`  
**Autenticación:** Ninguna

**Body:**
```json
{
  "usernameOrEmail": "ana.sagiro@techi.test",
  "password": "Sagiro#Test2026"
}
```

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzUxMiIs...",
    "expiresIn": 300,
    "refreshExpiresIn": 1800,
    "tokenType": "Bearer"
  }
}
```

> 📝 **Copia el campo `data.accessToken`** — lo usarás como `Bearer <token>` en todos los pasos siguientes.

---

## PASO 4 — Consultar tu propio usuario

**Endpoint:** `GET http://localhost:8082/api/v1/users/me`  
**Autenticación:** `Authorization: Bearer <token_del_paso_3>`

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "data": {
    "email": "ana.sagiro@techi.test",
    "username": "ana.sagiro",
    "firstName": "Ana",
    "lastName": "Sagiro",
    "accountStatus": "REGISTERED",
    "verificationStatus": "NOT_STARTED",
    "canOperate": false,
    "profile": {
      "country": "PE",
      "preferredLanguage": "es"
    }
  }
}
```

**✅ Valida que:**
- `accountStatus` sea `"REGISTERED"` (aún no ha hecho KYC)
- `canOperate` sea `false`

---

## PASO 5 — Consultar el contexto de acceso (roles y permisos)

**Endpoint:** `GET http://localhost:8082/api/v1/users/me/access-context`  
**Autenticación:** `Authorization: Bearer <token_del_paso_3>`

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "data": {
    "username": "ana.sagiro",
    "email": "ana.sagiro@techi.test",
    "roles": ["ROLE_CUSTOMER"],
    "accountStatus": "REGISTERED",
    "verificationStatus": "NOT_STARTED",
    "canOperate": false,
    "enabled": true
  }
}
```

**✅ Valida que:**
- `roles` contiene `"ROLE_CUSTOMER"`
- `canOperate` sea `false` (no ha completado KYC)

---

## PASO 6 — Actualizar datos del perfil

**Endpoint:** `PATCH http://localhost:8082/api/v1/users/me/profile`  
**Autenticación:** `Authorization: Bearer <token_del_paso_3>`

**Body:**
```json
{
  "phone": "+51911222333",
  "firstName": "Ana Maria",
  "lastName": "Sagiro Vega",
  "country": "MX",
  "preferredLanguage": "en",
  "blockchainVisibilityEnabled": true,
  "darkModeEnabled": true
}
```

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "message": "User profile updated successfully",
  "data": {
    "firstName": "Ana Maria",
    "lastName": "Sagiro Vega",
    "profile": {
      "country": "MX",
      "preferredLanguage": "en",
      "blockchainVisibilityEnabled": true,
      "darkModeEnabled": true
    }
  }
}
```

---

## PASO 7 — Confirmar que los datos actualizados se guardaron

**Endpoint:** `GET http://localhost:8082/api/v1/users/me`  
**Autenticación:** `Authorization: Bearer <token_del_paso_3>`

**✅ Valida que en la respuesta:**
- `data.firstName` = `"Ana Maria"`
- `data.lastName` = `"Sagiro Vega"`
- `data.profile.country` = `"MX"`
- `data.profile.preferredLanguage` = `"en"`
- `data.profile.blockchainVisibilityEnabled` = `true`
- `data.profile.darkModeEnabled` = `true`

---

## PASO 8 — Simular la aprobación del KYC

**Endpoint:** `POST http://localhost:8082/api/v1/users/me/simulate-kyc`  
**Autenticación:** `Authorization: Bearer <token_del_paso_3>`  
**Body:** vacío

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "message": "KYC simulated successfully",
  "data": {
    "accountStatus": "ACTIVE",
    "verificationStatus": "VERIFIED",
    "verificationLevel": "BASIC",
    "canOperate": true,
    "enabled": true
  }
}
```

**✅ Valida que:**
- `accountStatus` cambió a `"ACTIVE"`
- `verificationStatus` cambió a `"VERIFIED"`
- `canOperate` cambió a `true`

---

## PASO 9 — Consultar estado de usuario con token de ADMIN

Para este paso necesitas un token del cliente `iam-admin-client`. Puedes obtenerlo desde Keycloak directamente:

**Obtener token de admin:**
```http
POST http://localhost:8081/realms/sagiro/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=iam-admin-client
&client_secret=change-me
```

Copia el `access_token` de la respuesta.

**Endpoint:** `GET http://localhost:8082/api/v1/users/<ID_DEL_PASO_2>/status`  
**Autenticación:** `Authorization: Bearer <admin_token>`

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "data": {
    "accountStatus": "ACTIVE",
    "verificationStatus": "VERIFIED",
    "verificationLevel": "BASIC",
    "canOperate": true,
    "enabled": true
  }
}
```

---

## PASO 10 — Solicitar recuperación de contraseña

**Endpoint:** `POST http://localhost:8082/api/v1/users/password-recovery/request`  
**Autenticación:** Ninguna

**Body:**
```json
{
  "email": "ana.sagiro@techi.test"
}
```

**✅ Respuesta esperada (202):**
```json
{
  "status": "SUCCESS",
  "message": "If an account with that email exists, a recovery token has been sent",
  "data": null
}
```

> ⚠️ **Importante:** La respuesta siempre es `202` aunque el email no exista (protección anti-enumeración).

---

## PASO 11 — Obtener el token de recuperación de la base de datos

Como el `communication-service` aún no existe, el token se enviaría por email en producción. Para pruebas, lo extraemos directamente de PostgreSQL:

**Opción A — Usando psql en Docker:**
```bash
docker exec -it iam-postgres psql -U iam_user -d iam_db -c \
  "SELECT password_reset_token FROM iam_users WHERE email = 'ana.sagiro@techi.test';"
```

**Opción B — Usando DBeaver/pgAdmin:**
```sql
SELECT password_reset_token, password_reset_token_expires_at
FROM iam_users
WHERE email = 'ana.sagiro@techi.test';
```

> 📝 **Copia el valor del token** — es un UUID como `550e8400-e29b-41d4-a716-446655440000`

---

## PASO 12 — Restablecer la contraseña

**Endpoint:** `POST http://localhost:8082/api/v1/users/password-recovery/reset`  
**Autenticación:** Ninguna

**Body:**
```json
{
  "token": "<TOKEN_DEL_PASO_11>",
  "newPassword": "Sagiro#Nueva2026"
}
```

**✅ Respuesta esperada (200):**
```json
{
  "status": "SUCCESS",
  "message": "Password reset successfully. You can now log in with your new password.",
  "data": null
}
```

---

## PASO 13 — Verificar que la nueva contraseña funciona

**Endpoint:** `POST http://localhost:8082/api/v1/users/login`  
**Body:**
```json
{
  "usernameOrEmail": "ana.sagiro@techi.test",
  "password": "Sagiro#Nueva2026"
}
```

**✅ Respuesta esperada (200):** Un nuevo `accessToken` válido.

---

## PASO 14 — Verificar que la contraseña vieja ya NO funciona

**Endpoint:** `POST http://localhost:8082/api/v1/users/login`  
**Body:**
```json
{
  "usernameOrEmail": "ana.sagiro@techi.test",
  "password": "Sagiro#Test2026"
}
```

**✅ Respuesta esperada (401):**
```json
{
  "status": "ERROR",
  "message": "Invalid credentials"
}
```

---

## Casos de error adicionales para probar

### Registro duplicado → 409

```json
POST /api/v1/users/register
{
  "email": "ana.sagiro@techi.test",   ← email ya existe
  "username": "otro.username",
  ...
}
```
**Respuesta esperada:** `409 Conflict`

---

### Login con credenciales inválidas → 401

```json
POST /api/v1/users/login
{
  "usernameOrEmail": "nadie@sagiro.test",
  "password": "contraseniaIncorrecta"
}
```
**Respuesta esperada:** `401 Unauthorized`

---

### Endpoint protegido sin token → 401

```http
GET /api/v1/users/me
(sin header Authorization)
```
**Respuesta esperada:** `401 Unauthorized`

---

### Token de recuperación expirado → 409

Si esperas más de 15 minutos luego del Paso 10 antes de hacer el Paso 12:
```json
{
  "status": "ERROR",
  "message": "The password reset token has expired. Please request a new one."
}
```
**Respuesta esperada:** `409 Conflict`

---

## Resumen del flujo completo

| # | Endpoint | Método | Auth | Resultado esperado |
|---|----------|--------|------|--------------------|
| 1 | `/test/ping` | GET | ❌ | 200 — pong |
| 2 | `/users/register` | POST | ❌ | 201 — usuario REGISTERED |
| 3 | `/users/login` | POST | ❌ | 200 — JWT token |
| 4 | `/users/me` | GET | ✅ | 200 — datos iniciales del usuario |
| 5 | `/users/me/access-context` | GET | ✅ | 200 — ROLE_CUSTOMER, canOperate=false |
| 6 | `/users/me/profile` | PATCH | ✅ | 200 — datos actualizados |
| 7 | `/users/me` | GET | ✅ | 200 — cambios confirmados |
| 8 | `/users/me/simulate-kyc` | POST | ✅ | 200 — ACTIVE, VERIFIED, canOperate=true |
| 9 | `/users/{id}/status` | GET | ✅ Admin | 200 — estado completo |
| 10 | `/users/password-recovery/request` | POST | ❌ | 202 — token generado en BD |
| 11 | BD Query | SQL | — | Token UUID extraído |
| 12 | `/users/password-recovery/reset` | POST | ❌ | 200 — contraseña cambiada |
| 13 | `/users/login` | POST | ❌ | 200 — nueva contraseña funciona |
| 14 | `/users/login` | POST | ❌ | 401 — contraseña vieja rechazada |
