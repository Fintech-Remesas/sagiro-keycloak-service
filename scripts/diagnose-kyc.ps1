# Diagnóstico del error de KYC
$ErrorActionPreference = "Continue"

Write-Host "=== DIAGNÓSTICO KYC ==="
Write-Host ""

# 1. Registrar usuario fresco para la prueba
$email = "kyc_test_$(Get-Random -Maximum 9999)@sagiro.test"
$username = "kyc_test_$(Get-Random -Maximum 9999)"
Write-Host "[1] Registrando usuario de prueba: $email"
try {
    $body = "{`"email`":`"$email`",`"username`":`"$username`",`"phone`":`"+51999000001`",`"firstName`":`"KYC`",`"lastName`":`"Test`",`"country`":`"PE`",`"preferredLanguage`":`"es`",`"initialPassword`":`"KycTest#2026`"}"
    $reg = Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/api/v1/users/register' -ContentType 'application/json' -Body $body
    Write-Host "    ✅ Registrado: id=$($reg.data.id), status=$($reg.data.accountStatus)"
} catch {
    try { $r = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream()); Write-Host "    ❌ Error registro: $($r.ReadToEnd())" } catch { Write-Host "    ❌ $($_.Exception.Message)" }
    exit
}

# 2. Login
Write-Host "[2] Login..."
try {
    $loginBody = "{`"usernameOrEmail`":`"$email`",`"password`":`"KycTest#2026`"}"
    $loginResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/api/v1/users/login' -ContentType 'application/json' -Body $loginBody
    $token = $loginResp.data.accessToken
    Write-Host "    ✅ Token obtenido"
} catch {
    try { $r = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream()); Write-Host "    ❌ Error login: $($r.ReadToEnd())" } catch { Write-Host "    ❌ $($_.Exception.Message)" }
    exit
}

# 3. Simulate KYC - capturando el error completo
Write-Host "[3] POST /api/v1/users/me/simulate-kyc..."
try {
    $kycResp = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8082/api/v1/users/me/simulate-kyc' `
        -Headers @{Authorization = "Bearer $token"}
    Write-Host "    ✅ verificationStatus=$($kycResp.data.verificationStatus)"
    Write-Host "       accountStatus=$($kycResp.data.accountStatus)"
    Write-Host "       canOperate=$($kycResp.data.canOperate)"
} catch {
    Write-Host "    ❌ ERROR en KYC:"
    try {
        $r = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "    $($r.ReadToEnd())"
        Write-Host "    HTTP Status: $($_.Exception.Response.StatusCode)"
    } catch {
        Write-Host "    $($_.Exception.Message)"
    }
}
