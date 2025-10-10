# Cinema Management System - Система управления кинотеатром

## Описание проекта
REST API для управления кинотеатром с функциями управления фильмами, залами, сеансами, покупателями и билетами.

## Технологии
- Java 21
- Spring Boot 3.5.6
- PostgreSQL 


## Сущности

### 1. Movie 
- `id` - уникальный идентификатор
- `title` - название фильма
- `description` - описание
- `durationMinutes` - продолжительность в минутах
- `genre` - жанр
- `director` - режиссер
- `year` - год выпуска

### 2. Hall 
- `id` - уникальный идентификатор
- `name` - название зала
- `capacity` - вместимость

### 3. Customer 
- `id` - уникальный идентификатор
- `firstName` - имя
- `lastName` - фамилия
- `email` - email
- `phone` - телефон

### 4. Screening 
- `id` - уникальный идентификатор
- `movieId` - ID фильма
- `hallId` - ID зала
- `startTime` - время начала
- `price` - цена билета
- `availableSeats` - доступные места

### 5. Ticket 
- `id` - уникальный идентификатор
- `screeningId` - ID сеанса
- `customerId` - ID покупателя
- `seatNumber` - номер места
- `purchaseTime` - время покупки
- `isCancelled` - статус отмены

## API Endpoints

### Movies 
- `POST /api/movies` - создать фильм
- `GET /api/movies` - получить все фильмы
- `GET /api/movies/{id}` - получить фильм по ID
- `PUT /api/movies/{id}` - обновить фильм
- `DELETE /api/movies/{id}` - удалить фильм
- `GET /api/movies/search?title={title}` - поиск по названию
- `GET /api/movies/genre/{genre}` - получить фильмы по жанру

### Halls 
- `POST /api/halls` - создать зал
- `GET /api/halls` - получить все залы
- `GET /api/halls/{id}` - получить зал по ID
- `PUT /api/halls/{id}` - обновить зал
- `DELETE /api/halls/{id}` - удалить зал

### Customers 
- `POST /api/customers` - создать покупателя
- `GET /api/customers` - получить всех покупателей
- `GET /api/customers/{id}` - получить покупателя по ID
- `PUT /api/customers/{id}` - обновить покупателя
- `DELETE /api/customers/{id}` - удалить покупателя

### Screenings 
- `POST /api/screenings` - создать сеанс
- `GET /api/screenings` - получить все сеансы
- `GET /api/screenings/{id}` - получить сеанс по ID
- `PUT /api/screenings/{id}` - обновить сеанс
- `DELETE /api/screenings/{id}` - удалить сеанс
- `GET /api/screenings/upcoming` - получить предстоящие сеансы
- `GET /api/screenings/movie/{movieId}` - получить сеансы по фильму
- `GET /api/screenings/hall/{hallId}` - получить сеансы по залу

### Tickets 
- `POST /api/tickets` - купить билет
- `GET /api/tickets` - получить все билеты
- `GET /api/tickets/{id}` - получить билет по ID
- `PUT /api/tickets/{id}` - обновить билет
- `DELETE /api/tickets/{id}` - удалить билет
- `POST /api/tickets/{id}/cancel` - отменить билет (возврат)
- `GET /api/tickets/screening/{screeningId}` - получить билеты по сеансу
- `GET /api/tickets/customer/{customerId}` - получить билеты покупателя
- `GET /api/tickets/screening/{screeningId}/active` - активные билеты на сеанс

## Бизнес-логика

### Правила системы:
1. **Вместимость зала**: Количество проданных билетов не может превышать вместимость зала
2. **Уникальность мест**: Одно место может быть продано только один раз на сеанс
3. **Возврат билетов**: Возврат возможен только до начала сеанса
4. **Автоматический подсчет**: Количество доступных мест автоматически уменьшается при покупке и увеличивается при возврате

## Настройка базы данных PostgreSQL

### Создание базы данных:
1. Откройте pgAdmin
2. Подключитесь к серверу PostgreSQL
3. Правой кнопкой на "Databases" → "Create" → "Database..."
4. Введите имя: `cinemadb`
5. Owner: `postgres`
6. Нажмите "Save"

### Настройки подключения (application.properties):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cinemadb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

## Запуск приложения

```bash
.\gradlew.bat bootRun
```


### Сборка проекта:
```bash
.\gradlew.bat clean build -x test
```

Приложение будет доступно по адресу: `http://localhost:8081`

## Примеры запросов

### Для Windows PowerShell / Terminal:
Подробный файл [WINDOWS_TERMINAL_GUIDE.md](WINDOWS_TERMINAL_GUIDE.md) для всех команд.


## Проверка ошибок

```bash
try {
    Invoke-RestMethod -Uri "http://localhost:8081/api/customers" -Method Post `
        -Body '{"firstName":"","lastName":"","email":"bad","phone":""}' `
        -ContentType "application/json; charset=utf-8"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $reader.BaseStream.Position = 0
    $responseBody = $reader.ReadToEnd()
    
    $result = $responseBody | ConvertFrom-Json
    
    Write-Host "`nAll validation errors:" -ForegroundColor Yellow
    $result.errors.PSObject.Properties | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Value)" -ForegroundColor Yellow
    }
}
```

## Будущие функции
1. Аутентификация и авторизация пользователей
2. Административная панель
3. Лендинг фильмов(?наверное, если потрбуете что-то с фронтом)
