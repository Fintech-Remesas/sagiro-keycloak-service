# setup-keycloak.ps1
# Inicializa el realm "sagiro" en Keycloak corriendo en el servidor AWS
# Ejecutar desde Windows sin necesidad de SSH

param(
    [string]$BaseUrl            = "http://3.151.248.130:8081",
    [string]$AdminUsername      = "admin",
    [string]$AdminPassword      = "admin",
    [string]$Realm              = "sagiro",
    [string]$AdminClientId      = "iam-admin-client",
    [string]$AdminClientSecret  = "change-me",
    [string]$BackendClientId    = "iam-backend",
    [string]$BackendClientSecret = "change-me"
)

$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  SAGIRO - Keycloak Setup Script (PowerShell)"      -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Server  : $BaseUrl"
Write-Host "Realm   : $Realm"
Write-Host ""

# ── Paso 1: Obtener token de admin ──────────────────────────────────────────
Write-Host "[1/5] Obteniendo token de administrador de Keycloak..." -ForegroundColor Yellow

$tokenBody = @{
    client_id   = "admin-cli"
    username    = $AdminUsername
    password    = $AdminPassword
    grant_type  = "password"
}

try {
    $tokenResponse = Invoke-RestMethod `
        -Uri "$BaseUrl/realms/master/protocol/openid-connect/token" `
        -Method POST `
        -Body $tokenBody `
        -ContentType "application/x-www-form-urlencoded" `
        -TimeoutSec 15
    $adminToken = $tokenResponse.access_token
    Write-Host "     Token obtenido correctamente." -ForegroundColor Green
} catch {
    Write-Host "ERROR al obtener token de admin." -ForegroundColor Red
    Write-Host "Verifica que Keycloak esté corriendo en $BaseUrl" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

$headers = @{ Authorization = "Bearer $adminToken" }

# ── Paso 2: Verificar si el realm ya existe ─────────────────────────────────
Write-Host "[2/5] Verificando si el realm '$Realm' ya existe..." -ForegroundColor Yellow

try {
    $realmCheck = Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm" `
        -Headers $headers `
        -TimeoutSec 10
    Write-Host "     El realm '$Realm' YA EXISTE. Saltando creación." -ForegroundColor Green
    $realmExists = $true
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "     Realm no encontrado. Creando..." -ForegroundColor Yellow
        $realmExists = $false
    } else {
        Write-Host "ERROR inesperado al verificar realm: HTTP $statusCode" -ForegroundColor Red
        exit 1
    }
}

# ── Paso 3: Crear el realm si no existe ─────────────────────────────────────
if (-not $realmExists) {
    Write-Host "[3/5] Creando realm '$Realm' con roles y clientes..." -ForegroundColor Yellow

    $realmPayload = @{
        realm   = $Realm
        enabled = $true
        roles   = @{
            realm = @(
                @{ name = "ROLE_CUSTOMER" },
                @{ name = "ROLE_ADMIN" },
                @{ name = "ROLE_COMPLIANCE_AGENT" },
                @{ name = "ROLE_SERVICE" }
            )
        }
        clients = @(
            @{
                clientId                 = $AdminClientId
                name                     = $AdminClientId
                enabled                  = $true
                protocol                 = "openid-connect"
                publicClient             = $false
                secret                   = $AdminClientSecret
                serviceAccountsEnabled   = $true
                standardFlowEnabled      = $false
                directAccessGrantsEnabled = $false
                fullScopeAllowed         = $true
                defaultClientScopes      = @("basic","profile","email","roles")
            },
            @{
                clientId                 = $BackendClientId
                name                     = $BackendClientId
                enabled                  = $true
                protocol                 = "openid-connect"
                publicClient             = $false
                bearerOnly               = $false
                secret                   = $BackendClientSecret
                standardFlowEnabled      = $false
                directAccessGrantsEnabled = $true
                fullScopeAllowed         = $true
                defaultClientScopes      = @("basic","profile","email","roles")
            },
            @{
                clientId                 = "mobile-app"
                name                     = "mobile-app"
                enabled                  = $true
                protocol                 = "openid-connect"
                publicClient             = $true
                bearerOnly               = $false
                standardFlowEnabled      = $true
                directAccessGrantsEnabled = $true
                fullScopeAllowed         = $true
                redirectUris             = @("*")
                webOrigins               = @("*")
            },
            @{
                clientId                 = "remittance-frontend"
                name                     = "remittance-frontend"
                enabled                  = $true
                protocol                 = "openid-connect"
                publicClient             = $true
                standardFlowEnabled      = $true
                directAccessGrantsEnabled = $true
                fullScopeAllowed         = $true
            }
        )
    } | ConvertTo-Json -Depth 10

    try {
        Invoke-RestMethod `
            -Uri "$BaseUrl/admin/realms" `
            -Method POST `
            -Headers $headers `
            -Body $realmPayload `
            -ContentType "application/json" `
            -TimeoutSec 30 | Out-Null
        Write-Host "     Realm '$Realm' creado exitosamente!" -ForegroundColor Green
    } catch {
        Write-Host "ERROR al crear el realm." -ForegroundColor Red
        Write-Host $_.Exception.Message
        exit 1
    }
} else {
    Write-Host "[3/5] Saltando creación de realm (ya existe)." -ForegroundColor Gray
}

# ── Paso 4: Asignar roles de realm-management al service account ─────────────
Write-Host "[4/5] Configurando permisos del service account de '$AdminClientId'..." -ForegroundColor Yellow

# Refrescar token
$tokenResponse = Invoke-RestMethod `
    -Uri "$BaseUrl/realms/master/protocol/openid-connect/token" `
    -Method POST `
    -Body $tokenBody `
    -ContentType "application/x-www-form-urlencoded"
$adminToken = $tokenResponse.access_token
$headers = @{ Authorization = "Bearer $adminToken" }

try {
    # Obtener ID interno del cliente realm-management
    $realmMgmtClients = Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm/clients?clientId=realm-management" `
        -Headers $headers
    $realmMgmtId = $realmMgmtClients[0].id

    # Obtener ID del admin client
    $adminClients = Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm/clients?clientId=$AdminClientId" `
        -Headers $headers
    $adminClientUuid = $adminClients[0].id

    # Obtener service account user
    $serviceAccount = Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm/clients/$adminClientUuid/service-account-user" `
        -Headers $headers
    $serviceAccountUserId = $serviceAccount.id

    # Obtener roles de realm-management que necesitamos
    $allRoles = Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm/clients/$realmMgmtId/roles" `
        -Headers $headers
    $neededRoles = $allRoles | Where-Object { $_.name -in @("manage-users","query-users","view-users","view-realm") }
    $rolesPayload = $neededRoles | ConvertTo-Json -AsArray

    # Asignar los roles al service account
    Invoke-RestMethod `
        -Uri "$BaseUrl/admin/realms/$Realm/users/$serviceAccountUserId/role-mappings/clients/$realmMgmtId" `
        -Method POST `
        -Headers $headers `
        -Body $rolesPayload `
        -ContentType "application/json" | Out-Null

    Write-Host "     Roles asignados: manage-users, query-users, view-users, view-realm" -ForegroundColor Green
} catch {
    Write-Host "ADVERTENCIA: Error asignando roles del service account." -ForegroundColor DarkYellow
    Write-Host $_.Exception.Message
    Write-Host "Puedes continuar, pero el IAM service podría tener permisos limitados." -ForegroundColor DarkYellow
}

# ── Paso 5: Verificar que el IAM service puede obtener token técnico ─────────
Write-Host "[5/5] Verificando que el IAM service puede obtener token técnico..." -ForegroundColor Yellow

Start-Sleep -Seconds 2

try {
    $techTokenBody = @{
        grant_type    = "client_credentials"
        client_id     = $AdminClientId
        client_secret = $AdminClientSecret
    }
    $techToken = Invoke-RestMethod `
        -Uri "$BaseUrl/realms/$Realm/protocol/openid-connect/token" `
        -Method POST `
        -Body $techTokenBody `
        -ContentType "application/x-www-form-urlencoded" `
        -TimeoutSec 15

    if ($techToken.access_token) {
        Write-Host "     TOKEN TÉCNICO OBTENIDO CORRECTAMENTE." -ForegroundColor Green
        Write-Host ""
        Write-Host "==================================================" -ForegroundColor Green
        Write-Host "  SETUP COMPLETO - Keycloak está listo!" -ForegroundColor Green
        Write-Host "  El IAM service ahora puede registrar usuarios." -ForegroundColor Green
        Write-Host "==================================================" -ForegroundColor Green
    }
} catch {
    Write-Host "ADVERTENCIA: No se pudo obtener token técnico." -ForegroundColor DarkYellow
    Write-Host $_.Exception.Message
    Write-Host ""
    Write-Host "El realm fue creado, pero verifica la configuración del cliente '$AdminClientId'." -ForegroundColor DarkYellow
}
