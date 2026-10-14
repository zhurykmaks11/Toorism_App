# Travel Planner (Turism_APP)

Вебзастосунок для планування подорожей на Spring Boot: подорожі, міста в них і місця для відвідування.

| Лабораторна | Тема | Гілка |
|---|---|---|
| №1 | Simple web-application | `Lab1-web-security` |
| №2 | Create CRUD http-requests for the project | `lab2-web-security` |
| **№3** | **Protect by basic authorization all the requests from task № 2** | **`lab3-web-security`** |

---

## Що зроблено в лабораторній №3

- Підключено **Spring Security** і **HTTP Basic Authorization**.
- Усі CRUD-запити з лаби №2 (`/api/trips/**`, `/api/destinations/**`, `/api/places/**`) працюють **лише з логіном і паролем**.
- Два користувачі з різними ролями:

| Логін | Пароль | Роль | Що дозволено |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | усі операції: GET, POST, PUT, PATCH, DELETE |
| `user` | `user123` | USER | лише читання: GET |

- Паролі зберігаються як **BCrypt-хеш**; логіни й паролі можна змінити змінними середовища `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `USER_USERNAME`, `USER_PASSWORD`.
- Відповіді при відмові у доступі повертаються у JSON:

| Ситуація | Код |
|---|---|
| Немає заголовка `Authorization` | **401 Unauthorized** |
| Неправильний логін або пароль | **401 Unauthorized** |
| `user` намагається створити, змінити або видалити дані | **403 Forbidden** |

```json
{
  "timestamp": "2026-10-13T22:10:05",
  "status": 401,
  "error": "Unauthorized",
  "message": "Потрібна авторизація: вкажіть правильний логін і пароль (Basic Auth)",
  "path": "/api/trips"
}
```

Без авторизації залишились лише `GET /api/health` і головна сторінка (браузер сам попросить логін і пароль, коли сторінка звернеться до API).

### 10 HTTP-запитів (лаба №3)

Файли: `postman/Travel-Planner-Basic-Auth.postman_collection.json` (Postman) і `http-requests/basic-auth.http` (IntelliJ IDEA).

| № | Авторизація | Запит | Очікується |
|---|---|---|---|
| 1 | немає | `GET /api/trips` | 401 |
| 2 | admin / неправильний пароль | `GET /api/trips` | 401 |
| 3 | user | `GET /api/trips` | 200 |
| 4 | user | `GET /api/trips/1/destinations` | 200 |
| 5 | user | `POST /api/trips` | 403 |
| 6 | admin | `POST /api/trips` | 201 |
| 7 | admin | `PUT /api/trips/{tripId}` | 200 |
| 8 | admin | `POST /api/trips/{tripId}/destinations` | 201 |
| 9 | user | `DELETE /api/trips/{tripId}` | 403 |
| 10 | admin | `DELETE /api/trips/{tripId}` | 204 |

**Як запустити в Postman:** *Import* → файл колекції → **Run collection** → **Run**. Логіни й паролі вже налаштовані у змінних колекції, id створеної подорожі підставляється автоматично. Кожен запит перевіряє очікуваний код відповіді.

**Як додати Basic Auth вручну:** вкладка **Authorization** → Type **Basic Auth** → Username / Password. Postman сам додасть заголовок `Authorization: Basic <base64(логін:пароль)>`.

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

- http://localhost:8080/api/health: працює без авторизації
- http://localhost:8080/api/trips: браузер попросить логін і пароль (`user` / `user123` або `admin` / `admin123`)
- http://localhost:8080: головна сторінка зі списком подорожей

---

## REST API

> Починаючи з лаби №3, усі запити нижче потребують Basic Authorization (див. вище).

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
├── http-requests/basic-auth.http         # 10 HTTP-запитів для IntelliJ IDEA (лаба №3)
├── postman/                              # колекція Postman (лаба №3)
└── src/main/
    ├── java/org/example/turism_app/
    │   ├── config/                       # Spring Security: Basic Auth, користувачі, 401/403
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

**Як проходить запит:** Spring Security перевіряє логін, пароль і роль → `Controller` приймає HTTP-запит і перевіряє DTO (`@Valid`) → `Service` перевіряє бізнес-правила → `Repository` звертається до PostgreSQL → у відповідь повертається DTO у форматі JSON.

---

## Можливі проблеми

| Проблема | Рішення |
|---|---|
| `Connection to localhost:5432 refused` | PostgreSQL не запущений, див. крок 1 |
| `password authentication failed for user "postgres"` | Неправильний пароль, задайте `DB_PASSWORD` (крок 2) |
| `database "travel_planner" does not exist` | Створіть базу (крок 1) |
| `Port 8080 was already in use` | Додайте `server.port=8081` в `application.properties` |
| `401 Unauthorized` у Postman | Перевірте вкладку *Authorization*: Type = Basic Auth, правильні логін і пароль |
| `403 Forbidden` на POST / PUT / DELETE | Ці дії дозволені лише `admin` |
| `invalid target release: 25` | Встановіть JDK 25 і виберіть його в *File → Project Structure → SDK* |
