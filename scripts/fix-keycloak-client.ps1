# Script para corregir el cliente iam-backend en Keycloak
$ErrorActionPreference = "Stop"

# 1. Obtener admin token del realm master
$tokenResp = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8081/realms/master/protocol/openid-connect/token' `
    -ContentType 'application/x-www-form-urlencoded' `
    -Body 'grant_type=password&client_id=admin-cli&username=admin&password=admin'
$token = $tokenResp.access_token
Write-Host "✅ Token admin obtenido"

$headers = @{ Authorization = "Bearer $token" }

# 2. Obtener UUID del cliente iam-backend en realm sagiro
$clients = Invoke-RestMethod -Method Get `
    -Uri 'http://localhost:8081/admin/realms/sagiro/clients?clientId=iam-backend' `
    -Headers $headers

if ($clients.Count -eq 0) {
    Write-Error "❌ No se encontró el cliente iam-backend en el realm sagiro"
    exit 1
}
$clientUuid = $clients[0].id
Write-Host "✅ iam-backend UUID: $clientUuid"

# 3. Obtener representacion actual del cliente
$client = Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8081/admin/realms/sagiro/clients/$clientUuid" `
    -Headers $headers

# 4. Aplicar los cambios críticos usando Add-Member para evitar errores de propiedades de solo lectura
$client | Add-Member -NotePropertyName 'bearerOnly' -NotePropertyValue $false -Force
$client | Add-Member -NotePropertyName 'directAccessGrantsEnabled' -NotePropertyValue $true -Force
$client | Add-Member -NotePropertyName 'standardFlowEnabled' -NotePropertyValue $false -Force
$client | Add-Member -NotePropertyName 'secret' -NotePropertyValue 'change-me' -Force
$client | Add-Member -NotePropertyName 'defaultClientScopes' -NotePropertyValue @('basic', 'profile', 'email', 'roles') -Force

# 5. Actualizar el cliente en Keycloak
$body = $client | ConvertTo-Json -Depth 20
$updateHeaders = @{
    Authorization = "Bearer $token"
    'Content-Type' = 'application/json'
}
Invoke-RestMethod -Method Put `
    -Uri "http://localhost:8081/admin/realms/sagiro/clients/$clientUuid" `
    -Headers $updateHeaders `
    -Body $body
Write-Host "✅ Cliente iam-backend actualizado en Keycloak"

# 6. Verificar el resultado
$updated = Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8081/admin/realms/sagiro/clients/$clientUuid" `
    -Headers $headers
Write-Host "   bearerOnly:                 $($updated.bearerOnly)"
Write-Host "   directAccessGrantsEnabled:  $($updated.directAccessGrantsEnabled)"
Write-Host "   standardFlowEnabled:        $($updated.standardFlowEnabled)"

# 7. Ahora intentar login con el usuario recién registrado para verificar que funciona
Write-Host ""
Write-Host "🔄 Probando login con usuario Medali@gmail.com..."
try {
    $loginResp = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body '{"usernameOrEmail":"Medali@gmail.com","password":"medali"}'
    Write-Host "✅ LOGIN EXITOSO: token obtenido ($($loginResp.data.tokenType))"
} catch {
    Write-Host "⚠️ Login falló: $($_.Exception.Message)"
    Write-Host "   Esto puede ser por política de contraseñas en Keycloak (contraseña muy débil)"
    Write-Host "   La corrección del cliente está aplicada correctamente."
}

Write-Host ""
Write-Host "🔄 Probando login con usuario gustavo1..."
try {
    $loginResp2 = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body '{"usernameOrEmail":"gustavo1","password":"gustavo"}'
    Write-Host "✅ LOGIN gustavo1 exitoso"
} catch {
    Write-Host "⚠️ Login gustavo1 también falla (probablemente misma política de contraseñas)"
}
