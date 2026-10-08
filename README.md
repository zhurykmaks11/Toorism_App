# Travel Planner (Turism_APP)

Лабораторна робота №1: **Simple web-application**.

Вебзастосунок для планування подорожей на Spring Boot. Користувач бачить список своїх подорожей з датами, описом і бюджетом. У наступних лабораторних роботах проєкт буде розширено: CRUD, база даних, міста й місця для відвідування.

---

## Технології

| Технологія | Версія |
|---|---|
| Java (JDK) | 25 |
| Spring Boot | 4.1.1 |
| Spring Web MVC | у складі Spring Boot |
| Lombok | у складі Spring Boot |
| Maven | через Maven Wrapper (`mvnw`), окремо встановлювати не потрібно |
| Frontend | HTML + CSS + JavaScript (Fetch API) |

---

## Вимоги для запуску

- Встановлений **JDK 25** (перевірити: `java -version`)
- Вільний порт **8080**
- Встановлювати Maven, базу даних чи інші програми не потрібно

---

## Інструкція по запуску

### Варіант 1: через IntelliJ IDEA

1. Клонувати репозиторій:
   ```bash
   git clone https://github.com/zhurykmaks11/Toorism_App.git
   ```
2. Відкрити папку проєкту в IntelliJ IDEA (*File → Open*, вибрати папку з `pom.xml`).
3. Дочекатися завантаження залежностей Maven.
4. Відкрити файл `src/main/java/org/example/turism_app/TurismAppApplication.java` і натиснути **Run ▶**.
5. У консолі має з'явитися рядок `Tomcat started on port 8080`.

### Варіант 2: через командний рядок

У папці проєкту виконати:

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Під час першого запуску Maven Wrapper сам завантажить Maven і всі залежності.

---

## Перевірка роботи

Після запуску відкрити в браузері:

| Адреса | Що показує |
|---|---|
| http://localhost:8080 | Головна сторінка зі статусом сервера і картками подорожей |
| http://localhost:8080/api/health | Перевірка, що сервер працює (текст) |
| http://localhost:8080/api/trips | Список усіх подорожей (JSON) |
| http://localhost:8080/api/trips/1 | Одна подорож за id (JSON) |
| http://localhost:8080/api/trips/99 | Неіснуюча подорож, відповідь **404 Not Found** |

---

## REST API

| Метод | URL | Опис | Відповідь |
|---|---|---|---|
| GET | `/api/health` | Перевірка стану сервера | `200 OK`, текст |
| GET | `/api/trips` | Отримати всі подорожі | `200 OK`, масив JSON |
| GET | `/api/trips/{id}` | Отримати подорож за id | `200 OK` або `404 Not Found` |

Приклад відповіді `GET /api/trips/1`:
```json
{
  "id": 1,
  "title": "Карпати",
  "description": "Похід на Говерлу",
  "startDate": "2026-07-10",
  "endDate": "2026-07-15",
  "budget": 8000.0
}
```

---

## Структура проєкту

```
Turism_APP/
├── pom.xml                                   # залежності Maven
├── mvnw, mvnw.cmd                            # Maven Wrapper
└── src/main/
    ├── java/org/example/turism_app/
    │   ├── TurismAppApplication.java         # точка входу (main)
    │   ├── controller/
    │   │   ├── HealthController.java         # GET /api/health
    │   │   └── TripController.java           # GET /api/trips, /api/trips/{id}
    │   └── model/
    │       └── Trip.java                     # модель подорожі
    └── resources/
        ├── application.properties            # налаштування застосунку
        └── static/
            └── index.html                    # головна сторінка (frontend)
```

