package com.sagiro.iamservice.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Full end-to-end integration test for the IAM Service.
 *
 * Prerequisites (must be running before executing this test):
 *   - docker compose up -d postgres keycloak kafka zookeeper
 *   - ./scripts/setup-keycloak.sh  (or equivalent for Windows)
 *   - The IAM Service must be running on port 8082
 *
 * Run with:
 *   $env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd test -pl . -Dtest=FullFlowIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
 *
 * Flow covered:
 *   1.  PING              – verifica que el servicio está disponible
 *   2.  REGISTER          – crea un usuario nuevo
 *   3.  LOGIN             – obtiene access token
 *   4.  GET /me           – valida datos del usuario recién creado
 *   5.  GET /me/access-context – valida roles y estado de cuenta
 *   6.  UPDATE PROFILE    – actualiza nombre, país e idioma
 *   7.  GET /me           – confirma que la actualización se reflejó
 *   8.  SIMULATE KYC      – aprueba KYC para habilitar operaciones
 *   9.  GET /{id}/status  – valida estado con token de ADMIN (iam-admin-client)
 *  10.  PASSWORD RECOVERY REQUEST – solicita token de recuperación
 *  11.  PASSWORD RECOVERY RESET   – cambia contraseña con el token extraído de BD
 *  12.  LOGIN WITH NEW PASSWORD    – verifica que la nueva contraseña funciona
 *  13.  LOGIN WITH OLD PASSWORD    – verifica que la contraseña vieja ya no funciona (401)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullFlowIntegrationTest {

    private static final String BASE_URL = "http://localhost:8082";
    private static final String KEYCLOAK_URL = "http://localhost:8081";

    // Unique suffix per test run to avoid conflicts on repeated runs
    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8);
    private static final String EMAIL = "flujo." + SUFFIX + "@sagiro.test";
    private static final String USERNAME = "flujo_" + SUFFIX;
    private static final String INITIAL_PASSWORD = "Sagiro#Test2026";
    private static final String NEW_PASSWORD = "Sagiro#Nuevo2026";

    // Shared state between test steps
    private static String accessToken;
    private static String userId;
    private static String adminToken;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    // ─────────────────────────────────────────────────────────
    // PASO 1 – PING
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(1)
    @DisplayName("Paso 1 – PING: el servicio debe responder sin autenticación")
    void paso01_ping() {
        given()
            .when()
                .get("/api/v1/test/ping")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.message", equalTo("pong"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 2 – REGISTRO
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(2)
    @DisplayName("Paso 2 – REGISTER: debe crear el usuario con estado REGISTERED")
    void paso02_register() {
        String body = """
                {
                  "email": "%s",
                  "username": "%s",
                  "phone": "+51999000001",
                  "firstName": "Flujo",
                  "lastName": "Test",
                  "country": "PE",
                  "preferredLanguage": "es",
                  "initialPassword": "%s"
                }
                """.formatted(EMAIL, USERNAME, INITIAL_PASSWORD);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/register")
            .then()
                .statusCode(201)
                .body("status", equalTo("SUCCESS"))
                .body("data.email", equalTo(EMAIL))
                .body("data.username", equalTo(USERNAME))
                .body("data.accountStatus", equalTo("REGISTERED"))
                .body("data.verificationStatus", equalTo("NOT_STARTED"))
                .body("data.verificationLevel", equalTo("NONE"))
                .body("data.canOperate", equalTo(false))
                .body("data.enabled", equalTo(true))
                .body("data.profile.country", equalTo("PE"))
                .body("data.profile.preferredLanguage", equalTo("es"))
            .extract().response();

        userId = response.jsonPath().getString("data.id");
        Assertions.assertNotNull(userId, "El ID del usuario registrado no debe ser nulo");
    }

    // ─────────────────────────────────────────────────────────
    // PASO 3 – LOGIN
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(3)
    @DisplayName("Paso 3 – LOGIN: debe retornar access token y refresh token")
    void paso03_login() {
        String body = """
                {
                  "usernameOrEmail": "%s",
                  "password": "%s"
                }
                """.formatted(USERNAME, INITIAL_PASSWORD);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/login")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .body("data.tokenType", equalTo("Bearer"))
                .body("data.expiresIn", greaterThan(0))
            .extract().response();

        accessToken = response.jsonPath().getString("data.accessToken");
        Assertions.assertNotNull(accessToken, "El access token no debe ser nulo");
    }

    // ─────────────────────────────────────────────────────────
    // PASO 4 – GET /me
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(4)
    @DisplayName("Paso 4 – GET /me: debe retornar el usuario autenticado")
    void paso04_getMe() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .when()
                .get("/api/v1/users/me")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.email", equalTo(EMAIL))
                .body("data.username", equalTo(USERNAME))
                .body("data.firstName", equalTo("Flujo"))
                .body("data.lastName", equalTo("Test"))
                .body("data.accountStatus", equalTo("REGISTERED"))
                .body("data.profile.country", equalTo("PE"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 5 – GET /me/access-context
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(5)
    @DisplayName("Paso 5 – ACCESS CONTEXT: debe incluir ROLE_CUSTOMER y canOperate=false")
    void paso05_accessContext() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .when()
                .get("/api/v1/users/me/access-context")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.username", equalTo(USERNAME))
                .body("data.roles", hasItem("ROLE_CUSTOMER"))
                .body("data.accountStatus", equalTo("REGISTERED"))
                .body("data.canOperate", equalTo(false));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 6 – UPDATE PROFILE
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(6)
    @DisplayName("Paso 6 – UPDATE PROFILE: debe actualizar nombre, país, idioma y preferencias")
    void paso06_updateProfile() {
        String body = """
                {
                  "phone": "+51911222333",
                  "firstName": "Flujo Actualizado",
                  "lastName": "Test Vega",
                  "country": "MX",
                  "preferredLanguage": "en",
                  "blockchainVisibilityEnabled": true,
                  "darkModeEnabled": true
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(body)
            .when()
                .patch("/api/v1/users/me/profile")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.firstName", equalTo("Flujo Actualizado"))
                .body("data.lastName", equalTo("Test Vega"))
                .body("data.profile.country", equalTo("MX"))
                .body("data.profile.preferredLanguage", equalTo("en"))
                .body("data.profile.blockchainVisibilityEnabled", equalTo(true))
                .body("data.profile.darkModeEnabled", equalTo(true));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 7 – VERIFICAR QUE GET /me REFLEJA LOS CAMBIOS
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(7)
    @DisplayName("Paso 7 – GET /me post-update: debe reflejar los datos actualizados")
    void paso07_getMePostUpdate() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .when()
                .get("/api/v1/users/me")
            .then()
                .statusCode(200)
                .body("data.firstName", equalTo("Flujo Actualizado"))
                .body("data.lastName", equalTo("Test Vega"))
                .body("data.profile.country", equalTo("MX"))
                .body("data.profile.preferredLanguage", equalTo("en"))
                .body("data.profile.blockchainVisibilityEnabled", equalTo(true))
                .body("data.profile.darkModeEnabled", equalTo(true));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 8 – SIMULATE KYC
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(8)
    @DisplayName("Paso 8 – SIMULATE KYC: debe aprobar KYC y habilitar operaciones")
    void paso08_simulateKyc() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .when()
                .post("/api/v1/users/me/simulate-kyc")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.verificationStatus", equalTo("VERIFIED"))
                .body("data.verificationLevel", equalTo("BASIC"))
                .body("data.canOperate", equalTo(true))
                .body("data.accountStatus", equalTo("ACTIVE"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 9 – GET /{id}/status con token de admin
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(9)
    @DisplayName("Paso 9 – GET /{id}/status con admin token: debe retornar VERIFIED y canOperate=true")
    void paso09_getUserStatusAsAdmin() {
        // Obtener token de admin vía client_credentials desde Keycloak
        adminToken = given()
            .baseUri(KEYCLOAK_URL)
            .contentType(ContentType.URLENC)
            .formParam("grant_type", "client_credentials")
            .formParam("client_id", "iam-admin-client")
            .formParam("client_secret", "change-me")
            .when()
                .post("/realms/sagiro/protocol/openid-connect/token")
            .then()
                .statusCode(200)
                .body("access_token", notNullValue())
            .extract().jsonPath().getString("access_token");

        given()
            .baseUri(BASE_URL)
            .header("Authorization", "Bearer " + adminToken)
            .when()
                .get("/api/v1/users/" + userId + "/status")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.userId", equalTo(userId))
                .body("data.accountStatus", equalTo("ACTIVE"))
                .body("data.verificationStatus", equalTo("VERIFIED"))
                .body("data.verificationLevel", equalTo("BASIC"))
                .body("data.canOperate", equalTo(true));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 10 – PASSWORD RECOVERY REQUEST
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(10)
    @DisplayName("Paso 10 – PASSWORD RECOVERY REQUEST: debe aceptar la solicitud (202)")
    void paso10_passwordRecoveryRequest() {
        String body = """
                {
                  "email": "%s"
                }
                """.formatted(EMAIL);

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/password-recovery/request")
            .then()
                .statusCode(202)
                .body("status", equalTo("SUCCESS"))
                .body("message", containsString("recovery token"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 11 – PASSWORD RECOVERY RESET
    // Extrae el token de la BD vía query al actuator (health check confirma BD activa)
    // y luego aplica la nueva contraseña
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(11)
    @DisplayName("Paso 11 – PASSWORD RECOVERY RESET: debe cambiar la contraseña usando el token")
    void paso11_passwordRecoveryReset() throws Exception {
        // Esperar un momento para que el token se persista en BD
        Thread.sleep(500);

        // Extraer el token desde la BD vía JDBC (direct connection)
        String token = extractPasswordResetTokenFromDb();
        Assertions.assertNotNull(token, "El token de recuperación debe existir en la BD");

        String body = """
                {
                  "token": "%s",
                  "newPassword": "%s"
                }
                """.formatted(token, NEW_PASSWORD);

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/password-recovery/reset")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("message", containsString("Password reset successfully"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 12 – LOGIN CON NUEVA CONTRASEÑA
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(12)
    @DisplayName("Paso 12 – LOGIN con nueva contraseña: debe autenticar correctamente")
    void paso12_loginWithNewPassword() {
        String body = """
                {
                  "usernameOrEmail": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, NEW_PASSWORD);

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/login")
            .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.accessToken", notNullValue())
                .body("data.tokenType", equalTo("Bearer"));
    }

    // ─────────────────────────────────────────────────────────
    // PASO 13 – LOGIN CON CONTRASEÑA VIEJA (debe fallar 401)
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(13)
    @DisplayName("Paso 13 – LOGIN con contraseña vieja: debe rechazar con 401")
    void paso13_loginWithOldPasswordFails() {
        String body = """
                {
                  "usernameOrEmail": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, INITIAL_PASSWORD);

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/login")
            .then()
                .statusCode(401);
    }

    // ─────────────────────────────────────────────────────────
    // PASO 14 – REGISTRO DUPLICADO (debe fallar 409)
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(14)
    @DisplayName("Paso 14 – REGISTER duplicado: debe rechazar con 409")
    void paso14_registerDuplicateFails() {
        String body = """
                {
                  "email": "%s",
                  "username": "%s",
                  "phone": "+51999000001",
                  "firstName": "Duplicado",
                  "lastName": "Test",
                  "country": "PE",
                  "preferredLanguage": "es",
                  "initialPassword": "OtraPass#2026"
                }
                """.formatted(EMAIL, USERNAME);

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/register")
            .then()
                .statusCode(409);
    }

    // ─────────────────────────────────────────────────────────
    // PASO 15 – LOGIN CON CREDENCIALES INVÁLIDAS (debe fallar 401)
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(15)
    @DisplayName("Paso 15 – LOGIN con credenciales inválidas: debe rechazar con 401")
    void paso15_loginInvalidCredentials() {
        String body = """
                {
                  "usernameOrEmail": "no.existe@sagiro.test",
                  "password": "ContraseñaInvalida#1"
                }
                """;

        given()
            .baseUri(BASE_URL)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .post("/api/v1/users/login")
            .then()
                .statusCode(401);
    }

    // ─────────────────────────────────────────────────────────
    // PASO 16 – GET /me SIN TOKEN (debe fallar 401)
    // ─────────────────────────────────────────────────────────
    @Test
    @Order(16)
    @DisplayName("Paso 16 – GET /me sin token: debe rechazar con 401")
    void paso16_getMeWithoutToken() {
        given()
            .baseUri(BASE_URL)
            .when()
                .get("/api/v1/users/me")
            .then()
                .statusCode(401);
    }

    // ─────────────────────────────────────────────────────────
    // Helper: extrae el token de recuperación directamente de PostgreSQL
    // ─────────────────────────────────────────────────────────
    private String extractPasswordResetTokenFromDb() throws Exception {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5433/iam_db", "iam_user", "iam_password")) {

            String sql = "SELECT password_reset_token FROM iam_users WHERE email = ? AND password_reset_token IS NOT NULL";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, EMAIL);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password_reset_token");
                    }
                }
            }
        }
        return null;
    }
}
