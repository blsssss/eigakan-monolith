# 🎬 Тестирования API 

## ⚙️ Базовая настройка

### Базовый URL
```
http://localhost:8081
```

## 🔐 Аутентификация и CSRF 
В проект подключена Spring Security с Basic Auth и включённой защитой CSRF. Это значит:
- Для всех запросов требуется аутентификация (кроме `/api/auth/**`).
- Для методов POST/PUT/DELETE дополнительно требуется CSRF-токен (в заголовке) и cookie `XSRF-TOKEN`.
- CSRF-токен можно получить через эндпоинт `GET /api/auth/csrf` (он же установит cookie).

Учётные данные пользователей задаются через переменные окружения:
- `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`
- `APP_USER_USERNAME`, `APP_USER_PASSWORD`
Если переменные не заданы, сидинг пользователей при старте приложения не выполняется. Вставляйте свои логин/пароль в примеры ниже.

Быстрое использование своих логина/пароля в PowerShell:
```powershell
# Задайте значения в системных переменных окружения или в .env (если IDE их подхватывает)
$adminUser = $env:APP_ADMIN_USERNAME
$adminPass = $env:APP_ADMIN_PASSWORD
$userUser  = $env:APP_USER_USERNAME
$userPass  = $env:APP_USER_PASSWORD

# BasicAuth из переменных окружения (пример для админа)
$basicAdmin = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$adminUser:$adminPass"))
# BasicAuth для пользователя
$basicUser  = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$userUser:$userPass"))
```

> В примерах ниже там, где ранее были захардкоженные креды `admin:Admin@123`/`user:User@1234`, используйте свои `$basicAdmin`/`$basicUser`.

### Быстрый старт (PowerShell): сессия, токен и функции
```powershell
# 1) Создаём web-сессию для хранения cookie
$sess = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 2) Получаем CSRF-токен (и cookie XSRF-TOKEN) — без аутентификации
$csrfResp = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/csrf" -Method Get -WebSession $sess
$CSRF_HEADER = $csrfResp.headerName   # Обычно: X-XSRF-TOKEN
$CSRF_TOKEN  = $csrfResp.token

# 3) Готовим заголовки. Для BasicAuth используем ваши переменные окружения
$SECURE_HEADERS = @{ "Authorization" = "Basic $basicUser"; $($CSRF_HEADER) = $CSRF_TOKEN; "Content-Type" = "application/json; charset=utf-8" }

# 4) Удобные функции
function GET-AUTH { param($url) Invoke-RestMethod -Uri $url -Method Get -Headers $SECURE_HEADERS -WebSession $sess }
function POST-SECURE { param($url, $body) Invoke-RestMethod -Uri $url -Method Post -Body $body -Headers $SECURE_HEADERS -WebSession $sess }
function PUT-SECURE { param($url, $body) Invoke-RestMethod -Uri $url -Method Put -Body $body -Headers $SECURE_HEADERS -WebSession $sess }
function DELETE-SECURE { param($url) Invoke-RestMethod -Uri $url -Method Delete -Headers $SECURE_HEADERS -WebSession $sess }
```

### Быстрый старт (curl): cookies и CSRF
```powershell
# 1) Получаем токен и сохраняем cookie
curl -c cookies.txt http://localhost:8081/api/auth/csrf > csrf.json
$token = (Get-Content csrf.json | ConvertFrom-Json).token
$header = (Get-Content csrf.json | ConvertFrom-Json).headerName

# 2) Делаем авторизованные запросы (пример POST)
# -u логин:пароль добавит BasicAuth, -b cookies.txt отправит cookie XSRF-TOKEN,
# -H "$header: $token" добавит заголовок с токеном
curl -X POST "http://localhost:8081/api/movies" -H "Content-Type: application/json; charset=utf-8" -H "$header: $token" -b cookies.txt -u "$env:APP_ADMIN_USERNAME:$env:APP_ADMIN_PASSWORD" -d '{"title":"Интерстеллар","description":"Космос","durationMinutes":169,"genre":"Фантастика","director":"Кристофер Нолан","year":2014}'
```

### Регистрация пользователя
```powershell
# Регистрация (без авторизации, но с CSRF)
# СГЕНЕРИРУЕМ УНИКАЛЬНОЕ ИМЯ, чтобы не получить 400 из‑за дубликата
$u = "newuser$([Guid]::NewGuid().ToString('N').Substring(0,6))"
$body = @{ username = $u; password = "Strong@123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json; charset=utf-8" -Headers @{ $CSRF_HEADER = $CSRF_TOKEN } -WebSession $sess
```

> После регистрации обновите переменную $basic, чтобы выполнять запросы от нового пользователя: `$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$u:Strong@123"))`.


---

## 1️⃣ MOVIES (Фильмы)

### Создать фильм
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Post -Body '{"title":"Интерстеллар","description":"Космическое путешествие за пределы галактики","durationMinutes":169,"genre":"Фантастика","director":"Кристофер Нолан","year":2014}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/movies" -H "Content-Type: application/json; charset=utf-8" -d "{\"title\":\"Интерстеллар\",\"description\":\"Космическое путешествие\",\"durationMinutes\":169,\"genre\":\"Фантастика\",\"director\":\"Кристофер Нолан\",\"year\":2014}"
```

### Получить все фильмы
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get
```

**Или с curl:**
```powershell
curl http://localhost:8081/api/movies
```

### Получить фильм по ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies/1" -Method Get
```

**Или с curl:**
```powershell
curl http://localhost:8081/api/movies/1
```

### Обновить фильм
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies/1" -Method Put -Body '{"title":"Интерстеллар (IMAX)","description":"Космическое путешествие. IMAX версия","durationMinutes":169,"genre":"Фантастика","director":"Кристофер Нолан","year":2014}' -ContentType "application/json; charset=utf-8"
```

### Удалить фильм
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies/1" -Method Delete
```

### Поиск по названию
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies/search?title=Интерстеллар" -Method Get
```

### Получить по жанру
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies/genre/Фантастика" -Method Get
```

---

## 2️⃣ HALLS (Залы)

### Создать зал
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/halls" -Method Post -Body '{"name":"VIP Зал","capacity":50}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/halls" -H "Content-Type: application/json" -d "{\"name\":\"VIP Зал\",\"capacity\":50}"
```

### Получить все залы
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/halls" -Method Get
```

### Получить зал по ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/halls/1" -Method Get
```

### Обновить зал
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/halls/1" -Method Put -Body '{"name":"VIP Зал Премиум","capacity":60}' -ContentType "application/json; charset=utf-8"
```

### Удалить зал
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/halls/1" -Method Delete
```

---

## 3️⃣ CUSTOMERS (Покупатели)

### Создать покупателя
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/customers" -Method Post -Body '{"firstName":"Иван","lastName":"Иванов","email":"ivan@example.com","phone":"+79991234567"}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/customers" -H "Content-Type: application/json" -d "{\"firstName\":\"Иван\",\"lastName\":\"Иванов\",\"email\":\"ivan@example.com\",\"phone\":\"+79991234567\"}"
```

### Получить всех покупателей
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/customers" -Method Get
```

### Получить покупателя по ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/customers/1" -Method Get
```

### Обновить покупателя
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/customers/1" -Method Put -Body '{"firstName":"Иван","lastName":"Иванов","email":"ivan.new@example.com","phone":"+79991234567"}' -ContentType "application/json; charset=utf-8"
```

### Удалить покупателя
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/customers/1" -Method Delete
```

---

## 4️⃣ SCREENINGS (Сеансы)

### Создать сеанс
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings" -Method Post -Body '{"movieId":1,"hallId":1,"startTime":"2025-10-15T19:00:00","price":500.0}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/screenings" -H "Content-Type: application/json" -d "{\"movieId\":1,\"hallId\":1,\"startTime\":\"2025-10-15T19:00:00\",\"price\":500.0}"
```

### Получить все сеансы
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings" -Method Get
```

### Получить сеанс по ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/1" -Method Get
```

### Обновить сеанс
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/1" -Method Put -Body '{"movieId":1,"hallId":1,"startTime":"2025-10-15T20:00:00","price":600.0}' -ContentType "application/json; charset=utf-8"
```

### Удалить сеанс
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/1" -Method Delete
```

### Получить предстоящие сеансы
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/upcoming" -Method Get
```

### Получить сеансы фильма
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/movie/1" -Method Get
```

### Получить сеансы зала
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/screenings/hall/1" -Method Get
```

---

## 5️⃣ TICKETS (Билеты)

### Купить билет
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets" -Method Post -Body '{"screeningId":1,"customerId":1,"seatNumber":15}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/tickets" -H "Content-Type: application/json" -d "{\"screeningId\":1,\"customerId\":1,\"seatNumber\":15}"
```

### Получить все билеты
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets" -Method Get
```

### Получить билет по ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/1" -Method Get
```

### Отменить билет (возврат)
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/1/cancel" -Method Post
```

### Получить билеты по сеансу
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/screening/1" -Method Get
```

### Получить билеты покупателя
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/customer/1" -Method Get
```

### Получить активные билеты на сеанс
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/screening/1/active" -Method Get
```

### 🎟️ Массовая покупка билетов (Бизнес-операция)
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/bulk-purchase" -Method Post -Body '{"screeningId":1,"customerId":1,"seatNumbers":[10,11,12,13]}' -ContentType "application/json; charset=utf-8"
```

**Или с curl:**
```powershell
curl -X POST "http://localhost:8081/api/tickets/bulk-purchase" -H "Content-Type: application/json" -d "{\"screeningId\":1,\"customerId\":1,\"seatNumbers\":[10,11,12,13]}"
```

---



## ❌ ПРОВЕРКА ОШИБОК

### Попытка купить занятое место
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/tickets" -Method Post -Body '{"screeningId":1,"customerId":1,"seatNumber":15}' -ContentType "application/json; charset=utf-8"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $responseBody = $reader.ReadToEnd()
    Write-Host "Ошибка: $responseBody" -ForegroundColor Red
}
```

### Попытка массовой покупки с занятыми местами
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/bulk-purchase" -Method Post -Body '{"screeningId":1,"customerId":1,"seatNumbers":[15,16,17]}' -ContentType "application/json; charset=utf-8"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $responseBody = $reader.ReadToEnd()
    Write-Host "Ошибка: $responseBody" -ForegroundColor Red
}
```

### Попытка отменить билет после начала сеанса
```powershell
# Создать сеанс в прошлом
$pastScreening = Invoke-RestMethod -Uri "http://localhost:8081/api/screenings" -Method Post -Body '{"movieId":1,"hallId":1,"startTime":"2023-10-15T19:00:00","price":500.0}' -ContentType "application/json; charset=utf-8"

# Купить билет
$ticket = Invoke-RestMethod -Uri "http://localhost:8081/api/tickets" -Method Post -Body "{\"screeningId\":$($pastScreening.id),\"customerId\":1,\"seatNumber\":10}" -ContentType "application/json; charset=utf-8"

# Попытка отменить
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/tickets/$($ticket.id)/cancel" -Method Post
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $responseBody = $reader.ReadToEnd()
    Write-Host "Ошибка: $responseBody" -ForegroundColor Red
}
```

### Проверка валидации данных
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/customers" -Method Post -Body '{"firstName":"","lastName":"","email":"invalid-email","phone":""}' -ContentType "application/json; charset=utf-8"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $responseBody = $reader.ReadToEnd()
    
    $result = $responseBody | ConvertFrom-Json
    
    Write-Host "`nОшибки валидации:" -ForegroundColor Yellow
    $result.errors.PSObject.Properties | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Value)" -ForegroundColor Yellow
    }
}
```



---

## Примеры запросов: без авторизации и с авторизацией/CSRF

Ниже — конкретные сценарии и ожидаемые ответы. Это поможет быстро понять, почему приходит 401/403/400 и как сделать правильный запрос.

### 1) Без авторизации (ожидаемый 401 Unauthorized)
- Любой запрос к защищённым ресурсам (например, `GET /api/movies`) без заголовка `Authorization` вернёт 401.

PowerShell:
```powershell
# Windows PowerShell 5.1 — покажет исключение с кодом 401
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get
```

Чтобы увидеть тело ответа при ошибке (PS 5.1):
```powershell
try {
  Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get
} catch {
  $resp = $_.Exception.Response
  if ($resp -ne $null) {
    $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
    $reader.ReadToEnd() | Write-Host
  }
}
```

PowerShell 7+ (Core):
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Get -SkipHttpErrorCheck
```

curl:
```bash
curl -i http://localhost:8081/api/movies    # HTTP/1.1 401 Unauthorized
```

### 2) С авторизацией, но без CSRF на POST/PUT/DELETE (ожидаемый 403 Forbidden)
- Для методов изменения данных требуется CSRF. Если отправите только BasicAuth без CSRF — будет 403.

PowerShell:
```powershell
# Используем заранее подготовленную переменную $basicAdmin из секции выше
Invoke-RestMethod -Uri "http://localhost:8081/api/movies" -Method Post -Body '{"title":"X"}' -ContentType "application/json" -Headers @{ "Authorization" = "Basic $basicAdmin" }
# => 403 Forbidden (нет CSRF)
```

curl:
```bash
curl -i -X POST http://localhost:8081/api/movies -u "$env:APP_ADMIN_USERNAME:$env:APP_ADMIN_PASSWORD" -H "Content-Type: application/json" -d '{"title":"X"}'
# => HTTP/1.1 403 Forbidden (нет CSRF)
```

### 3) Полностью корректный запрос: BasicAuth + CSRF
Используйте шаги из раздела «Быстрый старт»: получите CSRF и cookie, сформируйте `SECURE_HEADERS`, затем:

PowerShell:
```powershell
# Предполагается, что вы уже выполнили шаги: $sess, $csrfResp, $CSRF_HEADER, $CSRF_TOKEN, $SECURE_HEADERS
# Пример авторизованного чтения (GET) — CSRF для GET не обязателен
GET-AUTH "http://localhost:8081/api/movies"

# Пример создания (POST) — нужен CSRF
$body = '{"title":"Интерстеллар","description":"Космос","durationMinutes":169,"genre":"Фантастика","director":"Кристофер Нолан","year":2014}'
POST-SECURE "http://localhost:8081/api/movies" $body
```

curl (полный сценарий):
```bash
# 1) Получаем CSRF и cookie
curl -c cookies.txt http://localhost:8081/api/auth/csrf > csrf.json
TOKEN=$(jq -r .token csrf.json)
HEADER=$(jq -r .headerName csrf.json)

# 2) GET (без CSRF, но с авторизацией)
curl -i http://localhost:8081/api/movies -u "$env:APP_USER_USERNAME:$env:APP_USER_PASSWORD"

# 3) POST (с CSRF + cookie + авторизацией)
curl -i -X POST "http://localhost:8081/api/movies" \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "$HEADER: $TOKEN" \
  -b cookies.txt \
  -u admin:Admin@123 \
  -d '{"title":"Интерстеллар","description":"Космос","durationMinutes":169,"genre":"Фантастика","director":"Кристофер Нолан","year":2014}'
```

### 4) Регистрация без авторизации (НО с CSRF)
`POST /api/auth/register` открыт без аутентификации, но CSRF обязателен (cookie + заголовок). Также действуют правила:
- username — уникальный (ошибка 400, если уже занят),
- пароль — минимум 8 символов, хотя бы 1 спецсимвол и 1 цифра.

PowerShell (уникальный логин):
```powershell
# Предполагается, что $sess, $CSRF_HEADER, $CSRF_TOKEN уже получены из /api/auth/csrf
$u = "newuser$([Guid]::NewGuid().ToString('N').Substring(0,6))"
$bodyObj = @{ username = $u; password = "Strong@123" }
$body = ($bodyObj | ConvertTo-Json)
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body $body -ContentType "application/json; charset=utf-8" -Headers @{ $CSRF_HEADER = $CSRF_TOKEN } -WebSession $sess
```

curl:
```bash
curl -c cookies.txt http://localhost:8081/api/auth/csrf > csrf.json
TOKEN=$(jq -r .token csrf.json)
HEADER=$(jq -r .headerName csrf.json)
curl -i -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "$HEADER: $TOKEN" \
  -b cookies.txt \
  -d '{"username":"newuser123456","password":"Strong@123"}'
```

### 5) Диагностика 400 Bad Request при регистрации
Если `Invoke-RestMethod` показывает только `(400) Bad Request`, то причина обычно одна из:
- такой username уже существует (повторная регистрация),
- пароль не соответствует политике (нет спецсимвола/цифры, длина < 8),
- не передан CSRF-токен/нет cookie `XSRF-TOKEN`.

Как увидеть текст ошибки в PowerShell 5.1:
```powershell
try {
  Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body '{"username":"newuser","password":"Strong@123"}' -ContentType "application/json; charset=utf-8" -Headers @{ $CSRF_HEADER = $CSRF_TOKEN } -WebSession $sess
} catch {
  $resp = $_.Exception.Response
  if ($resp -ne $null) {
    $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
    $reader.ReadToEnd() | Write-Host
  }
}
```

PowerShell 7+:
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -Body '{"username":"newuser","password":"Strong@123"}' -ContentType "application/json; charset=utf-8" -Headers @{ $CSRF_HEADER = $CSRF_TOKEN } -WebSession $sess -SkipHttpErrorCheck
```

curl (всегда печатает тело, удобно для отладки):
```bash
curl -i -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "$HEADER: $TOKEN" \
  -b cookies.txt \
  -d '{"username":"newuser","password":"Strong@123"}'
```

Ожидаемое тело ошибки (пример):
```json
{
  "timestamp": "2025-11-08T13:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username is already taken",
  "path": "/api/auth/register"
}
```

### 6) Проверка текущего пользователя
- Без авторизации: `GET /api/auth/me` вернёт `{ "authenticated": false }`.
- С BasicAuth: вернёт имя и роли.

PowerShell:
```powershell
# Без авторизации
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method Get

# С BasicAuth
$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("user:User@1234"))
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method Get -Headers @{ "Authorization" = "Basic $basic" }
```

curl:
```bash
curl -i http://localhost:8081/api/auth/me
curl -i http://localhost:8081/api/auth/me -u user:User@1234
```

> Примечание: имя заголовка для CSRF, возвращаемое `/api/auth/csrf`, обычно `X-XSRF-TOKEN`. Используйте именно то имя, которое вернул сервер (`headerName`).
