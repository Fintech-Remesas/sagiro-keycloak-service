# Limpiar required actions de todos los usuarios del realm sagiro que tengan UPDATE_PASSWORD pendiente
$ErrorActionPreference = "Stop"

# Obtener admin token
$tokenResp = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8081/realms/master/protocol/openid-connect/token' `
    -ContentType 'application/x-www-form-urlencoded' `
    -Body 'grant_type=password&client_id=admin-cli&username=admin&password=admin'
$token = $tokenResp.access_token
$headers = @{ Authorization = "Bearer $token" }

# Obtener todos los usuarios del realm sagiro
$users = Invoke-RestMethod -Method Get `
    -Uri 'http://localhost:8081/admin/realms/sagiro/users?max=100' `
    -Headers $headers

Write-Host "Usuarios encontrados en realm sagiro: $($users.Count)"

foreach ($u in $users) {
    Write-Host ""
    Write-Host "Usuario: $($u.username) | email: $($u.email)"
    Write-Host "  requiredActions: $($u.requiredActions -join ', ')"

    if ($u.requiredActions -and $u.requiredActions.Count -gt 0) {
        Write-Host "  -> Limpiando required actions..."
        $u | Add-Member -NotePropertyName 'requiredActions' -NotePropertyValue @() -Force
        $body = $u | ConvertTo-Json -Depth 10
        Invoke-RestMethod -Method Put `
            -Uri "http://localhost:8081/admin/realms/sagiro/users/$($u.id)" `
            -Headers @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' } `
            -Body $body
        Write-Host "  ✅ Required actions limpiadas"
    } else {
        Write-Host "  -> Sin required actions pendientes"
    }
}

Write-Host ""
Write-Host "=== Probando login después del fix ==="
$loginBody = '{"usernameOrEmail":"Medali@gmail.com","password":"medali"}'
try {
    $loginResp = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body $loginBody
    Write-Host "✅ LOGIN EXITOSO para Medali!"
    Write-Host "   tokenType: $($loginResp.data.tokenType)"
    Write-Host "   expiresIn: $($loginResp.data.expiresIn)"
} catch {
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errBody = $reader.ReadToEnd()
        Write-Host "❌ Login aún falla: $errBody"
    } catch {
        Write-Host "❌ Login falla: $($_.Exception.Message)"
    }
}
