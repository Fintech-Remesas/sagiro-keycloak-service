# Probar login directamente contra Keycloak
$ErrorActionPreference = "Continue"

Write-Host "=== Test 1: Login directo a Keycloak (iam-backend) ==="
try {
    $resp = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8081/realms/sagiro/protocol/openid-connect/token' `
        -ContentType 'application/x-www-form-urlencoded' `
        -Body 'grant_type=password&client_id=iam-backend&client_secret=change-me&username=Medali@gmail.com&password=medali'
    Write-Host "✅ LOGIN DIRECTO EXITOSO - token_type: $($resp.token_type)"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    Write-Host "❌ Error: $($_.Exception.Message)"
    Write-Host "   Detalle: $($reader.ReadToEnd())"
}

Write-Host ""
Write-Host "=== Test 2: Login via IAM Service ==="
try {
    $body = '{"usernameOrEmail":"Medali@gmail.com","password":"medali"}'
    $resp2 = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body $body
    Write-Host "✅ LOGIN IAM EXITOSO"
} catch {
    Write-Host "❌ Error IAM: $($_.Exception.Message)"
    try {
        $reader2 = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "   Body: $($reader2.ReadToEnd())"
    } catch {}
}

Write-Host ""
Write-Host "=== Test 3: Verificar que el IAM service está vivo ==="
try {
    $ping = Invoke-RestMethod -Uri 'http://localhost:8082/api/v1/test/ping'
    Write-Host "✅ IAM Service vivo: $($ping.message)"
} catch {
    Write-Host "❌ IAM Service no responde: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "=== Test 4: Verificar usuario en Keycloak realm sagiro ==="
$tokenResp = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8081/realms/master/protocol/openid-connect/token' `
    -ContentType 'application/x-www-form-urlencoded' `
    -Body 'grant_type=password&client_id=admin-cli&username=admin&password=admin'
$token = $tokenResp.access_token
$users = Invoke-RestMethod -Method Get `
    -Uri 'http://localhost:8081/admin/realms/sagiro/users?search=Medali' `
    -Headers @{Authorization = "Bearer $token"}
Write-Host "Usuarios encontrados en realm sagiro con 'Medali': $($users.Count)"
foreach ($u in $users) {
    Write-Host "  - username: $($u.username), email: $($u.email), enabled: $($u.enabled)"
}
