# Prueba completa del flujo post-fix
$ErrorActionPreference = "Continue"

Write-Host "=== PRUEBA FINAL: LOGIN Y FLUJO COMPLETO ==="
Write-Host ""

# Login Medali
Write-Host "[1] Login con Medali@gmail.com / medali"
try {
    $r = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body '{"usernameOrEmail":"Medali@gmail.com","password":"medali"}'
    Write-Host "    ✅ EXITOSO - tokenType: $($r.data.tokenType), expiresIn: $($r.data.expiresIn)s"
    $token = $r.data.accessToken

    # GET /me
    Write-Host "[2] GET /me con el token obtenido"
    $me = Invoke-RestMethod -Uri 'http://localhost:8082/api/v1/users/me' `
        -Headers @{Authorization = "Bearer $token"}
    Write-Host "    ✅ email=$($me.data.email), status=$($me.data.accountStatus), canOperate=$($me.data.canOperate)"

    # Simulate KYC
    Write-Host "[3] Simulate KYC"
    $kyc = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/me/simulate-kyc' `
        -Headers @{Authorization = "Bearer $token"}
    Write-Host "    ✅ verificationStatus=$($kyc.data.verificationStatus), accountStatus=$($kyc.data.accountStatus), canOperate=$($kyc.data.canOperate)"

} catch {
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "    ❌ Error: $($reader.ReadToEnd())"
    } catch {
        Write-Host "    ❌ Error: $($_.Exception.Message)"
    }
}

Write-Host ""
Write-Host "=== PRUEBA REGISTRO NUEVO USUARIO ==="
$newEmail = "nuevo_$(Get-Random -Maximum 9999)@sagiro.test"
$newUser = "nuevo_$(Get-Random -Maximum 9999)"
Write-Host "[4] Registrando $newEmail..."
try {
    $body = "{`"email`":`"$newEmail`",`"username`":`"$newUser`",`"phone`":`"+51999000009`",`"firstName`":`"Nuevo`",`"lastName`":`"Usuario`",`"country`":`"PE`",`"preferredLanguage`":`"es`",`"initialPassword`":`"NuevoPass#2026`"}"
    $reg = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/register' `
        -ContentType 'application/json' `
        -Body $body
    Write-Host "    ✅ Registrado: id=$($reg.data.id), status=$($reg.data.accountStatus)"

    Write-Host "[5] Login inmediato con contraseña NuevoPass#2026..."
    $r2 = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/login' `
        -ContentType 'application/json' `
        -Body "{`"usernameOrEmail`":`"$newEmail`",`"password`":`"NuevoPass#2026`"}"
    Write-Host "    ✅ LOGIN INMEDIATO EXITOSO - tokenType: $($r2.data.tokenType)"
    Write-Host ""
    Write-Host "    *** EL BUG ESTA CORREGIDO: usuarios nuevos pueden loguearse de inmediato ***"
} catch {
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "    ❌ Error: $($reader.ReadToEnd())"
    } catch {
        Write-Host "    ❌ Error: $($_.Exception.Message)"
    }
}
