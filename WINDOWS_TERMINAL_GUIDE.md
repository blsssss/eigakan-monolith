# API Testing Guide - Eigakan Cinema

## Base URL
```
http://localhost:8081
```

---

# TASK 4: Basic API Security (Spring Security)

## 4.1 What was implemented

- Spring Security with JWT authentication (stateless)
- CSRF disabled (not needed for JWT)
- Role-based authorization (ROLE_USER, ROLE_ADMIN)
- User registration with password validation
- Users stored in database via environment variables (not hardcoded!)

## 4.2 User Management

Users are created from environment variables at startup:
- `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD` - admin user (ROLE_ADMIN + ROLE_USER)
- `APP_USER_USERNAME`, `APP_USER_PASSWORD` - regular user (ROLE_USER)

## 4.3 Role-based Access Control

| Endpoint | GET | POST | PUT | DELETE |
|----------|-----|------|-----|--------|
| /api/movies/** | USER, ADMIN | ADMIN | ADMIN | ADMIN |
| /api/halls/** | USER, ADMIN | ADMIN | ADMIN | ADMIN |
| /api/screenings/** | USER, ADMIN | ADMIN | ADMIN | ADMIN |
| /api/customers/** | USER, ADMIN | ADMIN | ADMIN | ADMIN |
| /api/tickets/** | USER, ADMIN | USER, ADMIN | USER, ADMIN | ADMIN |
| /api/auth/** | ALL | ALL | - | - |

## 4.4 Test: User Registration with Password Validation

```powershell
# TEST 1: Successful registration
$body = '{"username":"testuser1","password":"Test@1234"}'
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
# Expected: 201 Created, user created

# TEST 2: Password too short (should fail)
$body = '{"username":"testuser2","password":"Ab@1"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Expected error: Password must be at least 8 characters" -ForegroundColor Green
}

# TEST 3: Password without special symbol (should fail)
$body = '{"username":"testuser3","password":"TestTest1"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Expected error: Password must contain special symbol" -ForegroundColor Green
}

# TEST 4: Password without digit (should fail)
$body = '{"username":"testuser4","password":"TestTest@"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Expected error: Password must contain digit" -ForegroundColor Green
}

# TEST 5: Duplicate username (should fail)
$body = '{"username":"testuser1","password":"Test@1234"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Expected error: Username is already taken" -ForegroundColor Green
}
```

## 4.5 Test: Access without Authentication

```powershell
# Without token - should return 403 Forbidden
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get
} catch {
    Write-Host "Expected: 403 Forbidden (no authentication)" -ForegroundColor Green
}
```

## 4.6 Test: Role-based Authorization

```powershell
# First, login as regular user
$loginBody = '{"username":"user","password":"User@1234"}'
$tokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$userHeaders = @{ "Authorization" = "Bearer $($tokens.accessToken)" }

# USER can GET movies
$movies = Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get -Headers $userHeaders
Write-Host "USER can read movies: $($movies.Count) found" -ForegroundColor Green

# USER cannot POST movies (ADMIN only)
try {
    $movieBody = '{"title":"Test","description":"Test","durationMinutes":120,"genre":"Drama","director":"Test","year":2024}'
    Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Post -Body $movieBody -ContentType "application/json" -Headers $userHeaders
    Write-Host "ERROR: USER should not create movies!" -ForegroundColor Red
} catch {
    Write-Host "Expected: 403 Forbidden (USER cannot create movies)" -ForegroundColor Green
}

# Login as admin
$adminBody = '{"username":"admin","password":"Admin@1234"}'
$adminTokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $adminBody -ContentType "application/json"
$adminHeaders = @{ "Authorization" = "Bearer $($adminTokens.accessToken)" }

# ADMIN can POST movies
$movieBody = '{"title":"Admin Test Movie","description":"Created by admin","durationMinutes":120,"genre":"Drama","director":"Admin","year":2024}'
$newMovie = Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Post -Body $movieBody -ContentType "application/json" -Headers $adminHeaders
Write-Host "ADMIN created movie with ID: $($newMovie.id)" -ForegroundColor Green
```

---

# TASK 5: JWT Access/Refresh Tokens and Session Management

## 5.1 What was implemented

- **JwtTokenProvider** - generates and validates access/refresh tokens
- **UserSession entity** - stores sessions in `user_sessions` table
- **SessionStatus enum** - ACTIVE, REFRESHED, REVOKED, EXPIRED
- **UserSessionRepository** - database operations for sessions
- **POST /api/auth/login** - returns access + refresh token pair
- **POST /api/auth/refresh** - exchanges refresh token for new pair

## 5.2 JWT Configuration

```properties
# Access Token: 15 minutes
jwt.access-token-expiration-ms=900000

# Refresh Token: 7 days  
jwt.refresh-token-expiration-ms=604800000
```

## 5.3 Token Structure

**Access Token payload:**
```json
{
  "type": "access",
  "userId": 2,
  "roles": ["ROLE_USER"],
  "sub": "user",
  "iat": 1764794133,
  "exp": 1764795033
}
```

**Refresh Token payload:**
```json
{
  "type": "refresh",
  "userId": 2,
  "sessionId": "b3a06f53-b690-449a-90bd-f285ba1ad272",
  "sub": "user",
  "iat": 1764794133,
  "exp": 1765398933
}
```

---

## 5.4 FULL TEST SCENARIO (Required for Task 5)

Run the test script: `.\test-jwt.ps1`

Or execute steps manually:

### Step 1: POST /auth/login - Get access and refresh tokens

```powershell
Write-Host "=== STEP 1: Login and get tokens ===" -ForegroundColor Cyan

$loginBody = '{"username":"user","password":"User@1234"}'
$tokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"

$accessToken = $tokens.accessToken
$refreshToken = $tokens.refreshToken

Write-Host "Access Token received!" -ForegroundColor Green
Write-Host "  - Expires in: $($tokens.accessTokenExpiresIn) seconds (15 min)" -ForegroundColor Gray
Write-Host "Refresh Token received!" -ForegroundColor Green  
Write-Host "  - Expires in: $($tokens.refreshTokenExpiresIn) seconds (7 days)" -ForegroundColor Gray
```

### Step 2: Access protected endpoint with access token

```powershell
Write-Host "`n=== STEP 2: Access protected endpoint ===" -ForegroundColor Cyan

$headers = @{ "Authorization" = "Bearer $accessToken" }

# Get movies
$movies = Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get -Headers $headers
Write-Host "SUCCESS: Got $($movies.Count) movies" -ForegroundColor Green

# Check current user
$me = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method Get -Headers $headers
Write-Host "SUCCESS: Authenticated as '$($me.username)' with roles: $($me.roles -join ', ')" -ForegroundColor Green
```

### Step 3: POST /auth/refresh - Get new token pair

```powershell
Write-Host "`n=== STEP 3: Refresh tokens ===" -ForegroundColor Cyan

$refreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
$newTokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"

$newAccessToken = $newTokens.accessToken
$newRefreshToken = $newTokens.refreshToken

Write-Host "SUCCESS: New token pair received!" -ForegroundColor Green
Write-Host "Tokens are different: $($accessToken -ne $newAccessToken)" -ForegroundColor Gray
```

### Step 4: Verify OLD refresh token is rejected (401/403)

```powershell
Write-Host "`n=== STEP 4: Old refresh token should be rejected ===" -ForegroundColor Cyan

try {
    $oldRefreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8081/api/auth/refresh" -Method Post -Body $oldRefreshBody -ContentType "application/json"
    Write-Host "ERROR: Old token should NOT work!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Old refresh token rejected (401/400 error)" -ForegroundColor Green
    Write-Host "This is expected - each refresh token can only be used once!" -ForegroundColor Gray
}
```

### Step 5: Check session statuses in database

```sql
-- Run in PostgreSQL:
SELECT 
    us.id,
    ua.username,
    us.status,
    us.created_at,
    us.expires_at
FROM user_sessions us
JOIN users ua ON us.user_id = ua.id
ORDER BY us.created_at DESC
LIMIT 5;
```

Expected results:
- First session: status = **REFRESHED** (was used in Step 3)
- Second session: status = **ACTIVE** (current session from Step 3)

### Step 6: Verify new access token works

```powershell
Write-Host "`n=== STEP 6: New access token works ===" -ForegroundColor Cyan

$newHeaders = @{ "Authorization" = "Bearer $newAccessToken" }
$me2 = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method Get -Headers $newHeaders
Write-Host "SUCCESS: New token works! User: $($me2.username)" -ForegroundColor Green
```

---

## 5.5 Session Status Values

| Status | Description |
|--------|-------------|
| ACTIVE | Current valid session |
| REFRESHED | Session was refreshed (old token used) |
| REVOKED | Session manually invalidated (logout) |
| EXPIRED | Session expired |

## 5.6 Logout Endpoints

```powershell
# Logout current session
$headers = @{ "Authorization" = "Bearer $accessToken" }
$logoutBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/logout" -Method Post -Body $logoutBody -ContentType "application/json" -Headers $headers

# Logout ALL sessions
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/logout-all" -Method Post -Headers $headers
```

---

# API Endpoints Reference

## Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /api/auth/register | Register new user | No |
| POST | /api/auth/login | Login, get tokens | No |
| POST | /api/auth/refresh | Refresh token pair | No |
| POST | /api/auth/logout | Logout session | Yes |
| POST | /api/auth/logout-all | Logout all sessions | Yes |
| GET | /api/auth/me | Current user info | No* |

*Returns `{"authenticated": false}` if not authenticated

## Protected Endpoints Examples

```powershell
# Get tokens first
$tokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body '{"username":"user","password":"User@1234"}' -ContentType "application/json"
$headers = @{ "Authorization" = "Bearer $($tokens.accessToken)" }

# For admin operations
$adminTokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body '{"username":"admin","password":"Admin@1234"}' -ContentType "application/json"
$adminHeaders = @{ "Authorization" = "Bearer $($adminTokens.accessToken)" }
```

### Movies
```powershell
# GET all movies (USER, ADMIN)
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get -Headers $headers

# POST new movie (ADMIN only)
$movieBody = '{"title":"Interstellar","description":"Space odyssey","durationMinutes":169,"genre":"Sci-Fi","director":"Christopher Nolan","year":2014}'
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Post -Body $movieBody -ContentType "application/json" -Headers $adminHeaders
```

### Halls
```powershell
# GET all halls
Invoke-RestMethod -Uri "http://localhost:8081/api/halls" -Method Get -Headers $headers

# POST new hall (ADMIN only)
$hallBody = '{"name":"VIP Hall","capacity":50}'
Invoke-RestMethod -Uri "http://localhost:8081/api/halls" -Method Post -Body $hallBody -ContentType "application/json" -Headers $adminHeaders
```

### Screenings
```powershell
# GET all screenings
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings" -Method Get -Headers $headers

# POST new screening (ADMIN only)
$screeningBody = '{"movieId":1,"hallId":1,"startTime":"2025-12-15T19:00:00","price":500.0}'
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings" -Method Post -Body $screeningBody -ContentType "application/json" -Headers $adminHeaders
```

### Tickets
```powershell
# POST buy ticket (USER can do this)
$ticketBody = '{"screeningId":1,"customerId":1,"seatNumber":15}'
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets" -Method Post -Body $ticketBody -ContentType "application/json" -Headers $headers

# POST bulk purchase
$bulkBody = '{"screeningId":1,"customerId":1,"seatNumbers":[10,11,12,13]}'
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/bulk-purchase" -Method Post -Body $bulkBody -ContentType "application/json" -Headers $headers
```

---

# Quick Reference

## Get Tokens
```powershell
$tokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -Body '{"username":"user","password":"User@1234"}' -ContentType "application/json"
$headers = @{ "Authorization" = "Bearer $($tokens.accessToken)" }
```

## Make Authenticated Request
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get -Headers $headers
```

## Refresh Tokens
```powershell
$refreshBody = @{ refreshToken = $tokens.refreshToken } | ConvertTo-Json
$newTokens = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
```

