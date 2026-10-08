# Travel Planner (Turism_APP)

Вебзастосунок для планування подорожей на Spring Boot: подорожі, міста в них і місця для відвідування.

| Лабораторна | Тема | Гілка |
|---|---|---|
| №1 | Simple web-application | `Lab1-web-security` |
| **№2** | **Create CRUD http-requests for the project** | **`Lab2-crud`** |

---

## Що зроблено в лабораторній №2

- Підключено базу даних **PostgreSQL** через **Spring Data JPA**.
- Таблиці й тестові дані створюються автоматично міграціями **Flyway** (`src/main/resources/db/migration`).
- Повний **CRUD** (Create, Read, Update, Delete) для трьох пов'язаних сутностей: **Trip → Destination → Place**.
- **Валідація** вхідних даних: обов'язкові поля, довжина, діапазони, логіка дат.
- Єдиний формат **помилок** (400, 404) у JSON.
- Готові **HTTP-запити**: файли `.http` для IntelliJ IDEA та колекція **Postman**.

---

## Технології

| Технологія | Версія |
|---|---|
| Java (JDK) | 25 |
| Spring Boot | 4.1.1 (Web MVC, Data JPA, Validation, Flyway) |
| PostgreSQL | 14+ (перевірено на 16) |
| Lombok | у складі Spring Boot |
| Maven | через Maven Wrapper (`mvnw`) |

---

## Інструкція по запуску

### Крок 1. Запустити PostgreSQL і створити базу `travel_planner`

**Варіант А: через Docker (найпростіше).** У корені проєкту виконати:
```bash
docker compose up -d
```
Буде створено базу `travel_planner` з користувачем `postgres` і паролем `postgres` на порту 5432.

**Варіант Б: встановлений PostgreSQL.** Створити базу через pgAdmin або psql:
```sql
CREATE DATABASE travel_planner;
```

### Крок 2. Перевірити логін і пароль до БД

За замовчуванням застосунок підключається з такими параметрами (`src/main/resources/application.properties`):

| Параметр | Значення за замовчуванням | Змінна середовища |
|---|---|---|
| URL | `jdbc:postgresql://localhost:5432/travel_planner` | `DB_URL` |
| Користувач | `postgres` | `DB_USERNAME` |
| Пароль | `postgres` | `DB_PASSWORD` |

Якщо у вашого PostgreSQL інший пароль, задайте змінну `DB_PASSWORD` (у IntelliJ: *Run → Edit Configurations → Environment variables*) або змініть значення в `application.properties`.

### Крок 3. Запустити застосунок

**IntelliJ IDEA:** відкрити `src/main/java/org/example/turism_app/TurismAppApplication.java` і натиснути **Run ▶**.

**Командний рядок:**
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Під час першого запуску Flyway сам створить таблиці й додасть тестові дані (3 подорожі, 4 міста, 6 місць). У консолі мають з'явитися рядки:
```
Successfully applied 2 migrations to schema "public"
Tomcat started on port 8080
```

### Крок 4. Перевірити

- http://localhost:8080: головна сторінка зі списком подорожей
- http://localhost:8080/api/trips: подорожі у форматі JSON

---

## HTTP-запити для перевірки CRUD

Усі запити вже готові, вручну нічого писати не потрібно.

| Інструмент | Файли |
|---|---|
| **Postman** | `postman/Travel-Planner-CRUD.postman_collection.json` |
| **IntelliJ IDEA HTTP Client** | `http-requests/trips.http`, `destinations.http`, `places.http`, `delete.http` |

**Postman:** *Import* → вибрати файл колекції → *Run collection* (або запускати запити по черзі). Кожен запит має тест, який перевіряє статус відповіді. Id створених записів автоматично зберігаються у змінних `tripId`, `destinationId`, `placeId`, тому наступні запити працюють саме з ними.

**IntelliJ IDEA:** відкрити файл `.http`, вибрати оточення `dev` і натискати ▶ біля запитів зверху вниз, у порядку `trips` → `destinations` → `places` → `delete`.

---

## REST API

### Trips: подорожі

| Метод | URL | Опис | Успіх |
|---|---|---|---|
| GET | `/api/trips` | Усі подорожі (можна `?status=PLANNED`) | 200 |
| GET | `/api/trips/{id}` | Одна подорож | 200 |
| POST | `/api/trips` | Створити подорож | 201 |
| PUT | `/api/trips/{id}` | Оновити подорож | 200 |
| DELETE | `/api/trips/{id}` | Видалити подорож разом з містами й місцями | 204 |

### Destinations: міста в подорожі

| Метод | URL | Опис | Успіх |
|---|---|---|---|
| GET | `/api/trips/{tripId}/destinations` | Міста подорожі | 200 |
| POST | `/api/trips/{tripId}/destinations` | Додати місто | 201 |
| GET | `/api/destinations/{id}` | Одне місто | 200 |
| PUT | `/api/destinations/{id}` | Оновити місто | 200 |
| DELETE | `/api/destinations/{id}` | Видалити місто разом з місцями | 204 |

### Places: місця для відвідування

| Метод | URL | Опис | Успіх |
|---|---|---|---|
| GET | `/api/destinations/{destinationId}/places` | Місця в місті | 200 |
| POST | `/api/destinations/{destinationId}/places` | Додати місце | 201 |
| GET | `/api/places/{id}` | Одне місце | 200 |
| PUT | `/api/places/{id}` | Оновити місце | 200 |
| PATCH | `/api/places/{id}/visit` | Позначити відвіданим (+ оцінка) | 200 |
| DELETE | `/api/places/{id}` | Видалити місце | 204 |

### Інше

| Метод | URL | Опис |
|---|---|---|
| GET | `/api/health` | Перевірка, що сервер працює |

### Приклад: створення подорожі

```http
POST /api/trips
Content-Type: application/json

{
  "title": "Київ — вихідні",
  "description": "Музеї, лавра і Поділ",
  "startDate": "2026-12-18",
  "endDate": "2026-12-21",
  "budget": 6000,
  "status": "PLANNED"
}
```

Відповідь `201 Created`:
```json
{
  "id": 4,
  "title": "Київ — вихідні",
  "description": "Музеї, лавра і Поділ",
  "startDate": "2026-12-18",
  "endDate": "2026-12-21",
  "budget": 6000,
  "status": "PLANNED",
  "createdAt": "2026-10-08T20:30:00"
}
```

### Допустимі значення

- `status` подорожі: `PLANNED`, `ONGOING`, `COMPLETED`
- `category` місця: `MUSEUM`, `FOOD`, `NATURE`, `LANDMARK`, `OTHER`
- Дати у форматі `yyyy-MM-dd`

---

## Валідація і помилки

| Правило | Код |
|---|---|
| Обов'язкові поля (`title`, дати, `city`, `country`, `name`, `category`) | 400 |
| Бюджет і вартість ≥ 0, оцінка від 1 до 5 | 400 |
| Дата завершення не раніше дати початку | 400 |
| Дати міста в межах дат подорожі | 400 |
| Запланована дата місця в межах перебування в місті | 400 |
| Оцінку можна ставити лише відвіданому місцю | 400 |
| Некоректний JSON, дата або значення enum | 400 |
| Запис з таким id не існує | 404 |

Приклад відповіді з помилкою:
```json
{
  "timestamp": "2026-10-08T20:31:12",
  "status": 400,
  "error": "Bad Request",
  "message": "Помилка валідації даних",
  "fieldErrors": {
    "title": "Назва обов'язкова",
    "budget": "Бюджет не може бути від'ємним"
  }
}
```

---

## Структура бази даних

```
trips (подорожі)
 └── destinations (міста)          trip_id → trips.id, ON DELETE CASCADE
      └── places (місця)           destination_id → destinations.id, ON DELETE CASCADE
```

- **trips**: id, title, description, start_date, end_date, budget, status, created_at
- **destinations**: id, city, country, arrival_date, departure_date, trip_id
- **places**: id, name, category, planned_date, cost, visited, rating, destination_id

---

## Структура проєкту

```
Turism_APP/
├── docker-compose.yml                    # PostgreSQL у Docker
├── http-requests/                        # HTTP-запити для IntelliJ IDEA
├── postman/                              # колекція Postman
└── src/main/
    ├── java/org/example/turism_app/
    │   ├── controller/                   # REST-контролери (HTTP → сервіс)
    │   │   ├── TripController.java
    │   │   ├── DestinationController.java
    │   │   ├── PlaceController.java
    │   │   └── HealthController.java
    │   ├── service/                      # бізнес-логіка і перевірки
    │   ├── repository/                   # доступ до БД (Spring Data JPA)
    │   ├── model/                        # JPA-сутності і enum-и
    │   ├── dto/                          # об'єкти запитів і відповідей
    │   └── exception/                    # винятки і глобальний обробник помилок
    └── resources/
        ├── application.properties
        ├── db/migration/                 # SQL-міграції Flyway
        │   ├── V1__create_tables.sql
        │   └── V2__insert_sample_data.sql
        └── static/index.html             # головна сторінка
```

**Як проходить запит:** `Controller` приймає HTTP-запит і перевіряє DTO (`@Valid`) → `Service` перевіряє бізнес-правила → `Repository` звертається до PostgreSQL → у відповідь повертається DTO у форматі JSON.

---

## Можливі проблеми

| Проблема | Рішення |
|---|---|
| `Connection to localhost:5432 refused` | PostgreSQL не запущений, див. крок 1 |
| `password authentication failed for user "postgres"` | Неправильний пароль, задайте `DB_PASSWORD` (крок 2) |
| `database "travel_planner" does not exist` | Створіть базу (крок 1) |
| `Port 8080 was already in use` | Додайте `server.port=8081` в `application.properties` |
| `invalid target release: 25` | Встановіть JDK 25 і виберіть його в *File → Project Structure → SDK* |
