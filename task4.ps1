# ============================================
# TEST AUTH AND ROLES WITH CSRF
# ============================================

$baseUrl = "http://localhost:8081"

Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "TEST AUTH, ROLES AND CSRF" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan

$adminUser = $env:APP_ADMIN_USERNAME
$adminPass = $env:APP_ADMIN_PASSWORD
$userUser = $env:APP_USER_USERNAME
$userPass = $env:APP_USER_PASSWORD

if (-not $adminUser -or -not $adminPass) {
    Write-Host "ERROR: APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD not set" -ForegroundColor Red
    exit 1
}
if (-not $userUser -or -not $userPass) {
    Write-Host "ERROR: APP_USER_USERNAME / APP_USER_PASSWORD not set" -ForegroundColor Red
    exit 1
}

$basicAdmin = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${adminUser}:${adminPass}"))
$basicUser = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${userUser}:${userPass}"))

# TEST 1: Request without auth (401)
Write-Host "`n[TEST 1] GET /api/movies without auth" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/movies" -Method Get -UseBasicParsing
    Write-Host "  FAIL: Expected 401, got $($response.StatusCode)" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401) {
        Write-Host "  OK: Got 401 Unauthorized" -ForegroundColor Green
    } else {
        Write-Host "  FAIL: Expected 401, got $statusCode" -ForegroundColor Red
    }
}

# TEST 2: Admin auth - check /api/auth/me
Write-Host "`n[TEST 2] GET /api/auth/me with BasicAuth (ADMIN)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -Headers @{
        "Authorization" = "Basic $basicAdmin"
    }
    Write-Host "  OK: Authorized as '$($response.username)'" -ForegroundColor Green
    Write-Host "  Roles: $($response.roles -join ', ')" -ForegroundColor Gray
    if ($response.roles -contains "ROLE_ADMIN") {
        Write-Host "  OK: ADMIN role present" -ForegroundColor Green
    } else {
        Write-Host "  FAIL: ADMIN role missing" -ForegroundColor Red
    }
} catch {
    Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 3: User auth - check /api/auth/me
Write-Host "`n[TEST 3] GET /api/auth/me with BasicAuth (USER)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/me" -Method Get -Headers @{
        "Authorization" = "Basic $basicUser"
    }
    Write-Host "  OK: Authorized as '$($response.username)'" -ForegroundColor Green
    Write-Host "  Roles: $($response.roles -join ', ')" -ForegroundColor Gray
    if ($response.roles -contains "ROLE_USER" -and $response.roles -notcontains "ROLE_ADMIN") {
        Write-Host "  OK: Only USER role (no ADMIN)" -ForegroundColor Green
    } else {
        Write-Host "  WARNING: Roles do not match expectations" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 4: GET with auth (no CSRF needed)
Write-Host "`n[TEST 4] GET /api/movies with auth (CSRF not needed for GET)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/movies" -Method Get -Headers @{
        "Authorization" = "Basic $basicUser"
    }
    Write-Host "  OK: Got movies list (count: $($response.Count))" -ForegroundColor Green
} catch {
    Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 5: POST without CSRF (403)
Write-Host "`n[TEST 5] POST /api/movies without CSRF token" -ForegroundColor Yellow
try {
    $body = '{"title":"Test","description":"Test","durationMinutes":120,"genre":"Test","director":"Test","year":2024}'
    $response = Invoke-WebRequest -Uri "$baseUrl/api/movies" -Method Post -Body $body -ContentType "application/json" -Headers @{
        "Authorization" = "Basic $basicAdmin"
    } -UseBasicParsing
    Write-Host "  FAIL: Expected 403, request succeeded" -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "  OK: Got 403 Forbidden (CSRF protection works)" -ForegroundColor Green
    } else {
        Write-Host "  FAIL: Expected 403, got $statusCode" -ForegroundColor Red
    }
}

# TEST 6: Get CSRF token
Write-Host "`n[TEST 6] Getting CSRF token" -ForegroundColor Yellow
$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $csrfResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/csrf" -Method Get -WebSession $sess
    $csrfHeader = $csrfResp.headerName
    $csrfToken = $csrfResp.token
    Write-Host "  OK: CSRF obtained" -ForegroundColor Green
    Write-Host "  Header: $csrfHeader" -ForegroundColor Gray
    Write-Host "  Token: $($csrfToken.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "  FAIL: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# TEST 7: POST with CSRF (ADMIN) - create movie
Write-Host "`n[TEST 7] POST /api/movies with CSRF (ADMIN)" -ForegroundColor Yellow
$movieBody = @{
    title = "Test Movie $(Get-Random)"
    description = "Test Description"
    durationMinutes = 120
    genre = "Action"
    director = "Test Director"
    year = 2024
} | ConvertTo-Json

$testMovieId = $null
try {
    $headers = @{
        "Authorization" = "Basic $basicAdmin"
        $csrfHeader = $csrfToken
        "Content-Type" = "application/json; charset=utf-8"
    }
    $movie = Invoke-RestMethod -Uri "$baseUrl/api/movies" -Method Post -Body $movieBody -Headers $headers -WebSession $sess
    Write-Host "  OK: Movie created (ID: $($movie.id))" -ForegroundColor Green
    $testMovieId = $movie.id
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "  FAIL: Status $statusCode - $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 8: POST with CSRF (USER) - check access
Write-Host "`n[TEST 8] POST /api/movies with CSRF (USER) - check access" -ForegroundColor Yellow
$sessUser = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrfRespUser = Invoke-RestMethod -Uri "$baseUrl/api/auth/csrf" -Method Get -WebSession $sessUser
$csrfHeaderUser = $csrfRespUser.headerName
$csrfTokenUser = $csrfRespUser.token

$movieBody2 = @{
    title = "User Movie $(Get-Random)"
    description = "User Description"
    durationMinutes = 90
    genre = "Drama"
    director = "User Director"
    year = 2024
} | ConvertTo-Json

try {
    $headers = @{
        "Authorization" = "Basic $basicUser"
        $csrfHeaderUser = $csrfTokenUser
        "Content-Type" = "application/json; charset=utf-8"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/movies" -Method Post -Body $movieBody2 -Headers $headers -WebSession $sessUser -UseBasicParsing
    Write-Host "  INFO: USER can create movies (status: $($response.StatusCode))" -ForegroundColor Yellow
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "  OK: USER cannot create movies (403 Forbidden)" -ForegroundColor Green
    } else {
        Write-Host "  Status: $statusCode" -ForegroundColor Yellow
    }
}

# TEST 9: DELETE with CSRF (ADMIN)
if ($testMovieId) {
    Write-Host "`n[TEST 9] DELETE /api/movies/$testMovieId with CSRF (ADMIN)" -ForegroundColor Yellow
    try {
        $headers = @{
            "Authorization" = "Basic $basicAdmin"
            $csrfHeader = $csrfToken
        }
        Invoke-RestMethod -Uri "$baseUrl/api/movies/$testMovieId" -Method Delete -Headers $headers -WebSession $sess
        Write-Host "  OK: Movie deleted" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  FAIL: Status $statusCode" -ForegroundColor Red
    }
}

# TEST 10: Register new user (no auth, with CSRF)
Write-Host "`n[TEST 10] POST /api/auth/register (no auth, with CSRF)" -ForegroundColor Yellow
$sessReg = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrfRespReg = Invoke-RestMethod -Uri "$baseUrl/api/auth/csrf" -Method Get -WebSession $sessReg
$csrfHeaderReg = $csrfRespReg.headerName
$csrfTokenReg = $csrfRespReg.token

$newUsername = "testuser$([Guid]::NewGuid().ToString('N').Substring(0,6))"
$regBody = @{
    username = $newUsername
    password = "Test@12345"
} | ConvertTo-Json

try {
    $headers = @{
        $csrfHeaderReg = $csrfTokenReg
        "Content-Type" = "application/json; charset=utf-8"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody -Headers $headers -WebSession $sessReg
    Write-Host "  OK: User '$newUsername' registered" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "  FAIL: Status $statusCode - $($_.Exception.Message)" -ForegroundColor Red
}

# SUMMARY
Write-Host ("`n" + "=" * 60) -ForegroundColor Cyan
Write-Host "TESTING COMPLETED" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan
