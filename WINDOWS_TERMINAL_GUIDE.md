# 🎬 Тестирования API 

## ⚙️ Базовая настройка

### Базовый URL
```
http://localhost:8081
```

```powershell
# Alias 
function POST-API { param($url, $body) Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json; charset=utf-8" }
function GET-API { param($url) Invoke-RestMethod -Uri $url -Method Get }
function PUT-API { param($url, $body) Invoke-RestMethod -Uri $url -Method Put -Body $body -ContentType "application/json; charset=utf-8" }
function DELETE-API { param($url) Invoke-RestMethod -Uri $url -Method Delete }
```

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

