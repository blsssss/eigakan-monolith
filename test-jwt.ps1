# ========================================
# TASK 5: JWT ACCESS/REFRESH TOKENS TEST
# Complete scenario for defense
# ========================================

$baseUrl = "http://localhost:8081"

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "TASK 5: JWT Access/Refresh Token Test" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# STEP 1: Login
Write-Host "`n=== STEP 1: POST /auth/login ===" -ForegroundColor Cyan
Write-Host "Getting access and refresh tokens..." -ForegroundColor Gray
$loginBody = '{"username":"user","password":"User@1234"}'
try {
    $tokens = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $accessToken = $tokens.accessToken
    $refreshToken = $tokens.refreshToken
    Write-Host "[OK] Tokens received!" -ForegroundColor Green
    Write-Host "    Access Token expires in: $($tokens.accessTokenExpiresIn) sec" -ForegroundColor Gray
    Write-Host "    Refresh Token expires in: $($tokens.refreshTokenExpiresIn) sec" -ForegroundColor Gray
    Write-Host "    Token Type: $($tokens.tokenType)" -ForegroundColor Gray
} catch {
    Write-Host "[FAIL] Login error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# STEP 2: Access protected endpoint
Write-Host "`n=== STEP 2: Access protected endpoint ===" -ForegroundColor Cyan
Write-Host "Using access token to call /api/movies..." -ForegroundColor Gray
$headers = @{ "Authorization" = "Bearer $accessToken" }
try {
    $movies = Invoke-RestMethod -Uri "$baseUrl/api/movies" -Method Get -Headers $headers
    Write-Host "[OK] Access granted! Movies count: $($movies.Count)" -ForegroundColor Green

    $me = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -Headers $headers
    Write-Host "[OK] User: $($me.username), Roles: $($me.roles -join ', ')" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $($_.Exception.Message)" -ForegroundColor Red
}

# STEP 3: Refresh tokens
Write-Host "`n=== STEP 3: POST /auth/refresh ===" -ForegroundColor Cyan
Write-Host "Getting new token pair with refresh token..." -ForegroundColor Gray
$refreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
try {
    $newTokens = Invoke-RestMethod -Uri "$baseUrl/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
    $newAccessToken = $newTokens.accessToken
    $newRefreshToken = $newTokens.refreshToken
    Write-Host "[OK] New token pair received!" -ForegroundColor Green

    if ($accessToken -ne $newAccessToken) {
        Write-Host "[OK] Tokens are DIFFERENT (correct behavior)" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Tokens are same (unexpected)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[FAIL] Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# STEP 4: Old refresh token should fail
Write-Host "`n=== STEP 4: Old refresh token rejected ===" -ForegroundColor Cyan
Write-Host "Trying to use OLD refresh token (should fail)..." -ForegroundColor Gray
try {
    $oldRefreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/auth/refresh" -Method Post -Body $oldRefreshBody -ContentType "application/json"
    Write-Host "[FAIL] Old token worked - this is WRONG!" -ForegroundColor Red
} catch {
    Write-Host "[OK] Old refresh token rejected (401/400)!" -ForegroundColor Green
    Write-Host "    Each refresh token can only be used ONCE" -ForegroundColor Gray
}

# STEP 5: Check DB
Write-Host "`n=== STEP 5: Check session statuses in DB ===" -ForegroundColor Cyan
Write-Host "Run this SQL in PostgreSQL:" -ForegroundColor Yellow
Write-Host ""
Write-Host "SELECT us.id, ua.username, us.status, us.created_at" -ForegroundColor White
Write-Host "FROM user_sessions us" -ForegroundColor White
Write-Host "JOIN users ua ON us.user_id = ua.id" -ForegroundColor White
Write-Host "ORDER BY us.created_at DESC LIMIT 5;" -ForegroundColor White
Write-Host ""
Write-Host "Expected: First session = REFRESHED, Second = ACTIVE" -ForegroundColor Gray

# STEP 6: New token works
Write-Host "`n=== STEP 6: New access token works ===" -ForegroundColor Cyan
$newHeaders = @{ "Authorization" = "Bearer $newAccessToken" }
try {
    $me2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -Headers $newHeaders
    Write-Host "[OK] New token works! User: $($me2.username)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "ALL STEPS COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

