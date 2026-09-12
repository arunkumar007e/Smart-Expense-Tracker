# Personal Expense Tracker

A full-stack personal finance web app: **register an account, log in, add income and
expense transactions, organize them into categories, and get an instant dashboard and
reports.** Every user's data is strictly isolated — you only ever see your own records.

Built small, modern, and interview-friendly: no microservices, no message queues, no
cloud dependencies. Just a clean Spring Boot REST API, a React dashboard, and PostgreSQL.

---

## Features

- **Authentication** — JWT (stateless) + Spring Security + BCrypt password hashing.
  Public endpoints are only `/auth/register` and `/auth/login`.
- **Transactions** — add / edit / delete income & expense transactions (amount, date,
  description, category), filter by type, category, month, and search.
- **Dashboard** — total income, total expense, balance, last-6-months income/expense bar
  chart, all-time spending pie chart, and recent transactions.
- **Categories** — each account gets sensible defaults (expense & income) automatically;
  create/edit/delete your own, color-coded. Categories in use by transactions can't be deleted.
- **Reports** — monthly summary (income/expense/balance) and category breakdown with
  share-of-total per category, filterable by month and type.
- **Data isolation** — every query is scoped to the authenticated user; accessing another
  user's resources returns `404`.
- **API docs** — Swagger UI with a working **Authorize** button (JWT).

---

## Tech Stack

| Layer      | Technology                                             |
|------------|--------------------------------------------------------|
| Frontend   | React 18 + TypeScript + Vite, react-router, recharts   |
| Backend    | Java 21, Spring Boot 3.3, Spring Security, JPA         |
| Database   | PostgreSQL (17)                                        |
| Auth       | JWT (jjwt 0.12), BCrypt                                |
| API Docs   | springdoc-openapi (Swagger UI / OpenAPI 3)             |
| Tests      | JUnit 5 + Mockito (unit & slice / controller tests)    |

---

## Project Structure

```
D:\Personal-Expense-Tracker
├── backend/                    # Spring Boot REST API (Java 21, Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/expense/tracker/
│       │   ├── controller/     # REST controllers (auth, transactions, categories, dashboard, reports)
│       │   ├── service/        # Business logic & in-memory aggregation
│       │   ├── repository/     # Spring Data JPA repositories
│       │   ├── entity/         # JPA entities (User, Category, Transaction)
│       │   ├── enums/          # TransactionType (INCOME | EXPENSE)
│       │   ├── dto/            # Request/response records
│       │   ├── exception/      # Centralized REST error handling
│       │   ├── security/       # JWT filter, UserDetails, entry point, access denied
│       │   └── config/         # Security config, CORS, OpenAPI
│       └── main/resources/application.properties
│       └── test/java/com/expense/tracker/   # JUnit + Mockito tests
├── frontend/                   # React + TypeScript SPA (Vite)
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts          # proxies /api -> :8080
│   └── src/
│       ├── api/                # typed API client (Bearer token, error handling)
│       ├── context/            # auth state + 401 handling
│       ├── components/         # layout, tables, forms, modals, filters
│       ├── pages/              # Login, Register, Dashboard, Transactions, Categories, Reports
│       ├── utils/              # INR currency & date formatting
│       └── types/              # shared TypeScript types
└── database/
    └── create_database.sql     # PostgreSQL schema + monthly summary view (optional)
```

---

## Prerequisites

- **Java 21** (JDK) — `java -version`
- **Maven 3.9+** — `mvn -version`
- **Node.js 18+ / npm** — `node -v`
- **PostgreSQL** running on `localhost:5432`
- Git-friendly: `.gitignore` and `.env.example` are included; **secrets must never be committed**.

---

## Database Setup (once)

```powershell
# Create the database + schema (optionally uses the included view/trigger)
psql -U postgres -h localhost -p 5432 -f database\create_database.sql
```

> The script drops stale v1 tables (`expenses`, old `categories`) automatically.
> Alternatively, skip the script entirely — the backend's
> `spring.jpa.hibernate.ddl-auto=update` creates the tables on first run.
> Keep column types in the script in sync with the JPA entities so "update" is a no-op.

A leftover database from **v1** of this project (only `expenses`, no users) is not
compatible; drop it if needed:

```sql
DROP TABLE IF EXISTS expenses; DROP TABLE IF EXISTS categories;
```

---

## Configuration

All secrets are read from **environment variables**, with local-only dev defaults so the
app runs out of the box. See `.env.example`.

| Variable              | Default                                                     | Description                              |
|-----------------------|-------------------------------------------------------------|------------------------------------------|
| `DB_URL`              | `jdbc:postgresql://localhost:5432/expense_tracker`          | PostgreSQL JDBC URL                      |
| `DB_USERNAME`         | `postgres`                                                  | DB user                                  |
| `DB_PASSWORD`         | `postgres`                                                  | DB password                              |
| `JWT_SECRET`          | dev-only default (see `application.properties`)             | Base64 secret ≥ 32 bytes for HS256       |
| `JWT_EXPIRATION_MS`   | `86400000` (24 h)                                           | Token lifetime                           |

Generate a strong secret: `openssl rand -base64 48`

---

## Run the App

Simplest: double-click **`run.bat`** (creates the DB if missing, starts backend + frontend).
Or manually:

```powershell
# 1. Backend  (serves the REST API on http://localhost:8080)
$env:JWT_SECRET = "your-base64-secret"
cd backend
mvn spring-boot:run

# 2. Frontend (Vite dev server on http://localhost:5173)
cd frontend
npm install
npm run dev
```

Vite proxies `/api` to `http://localhost:8080`, so the app uses relative URLs.

### Where to look

- **App:** `http://localhost:5173`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (click **Authorize**, paste your
  JWT, and every `Try it out` call will be authenticated)
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Try it

1. Open the app → **Create an account** (default expense + income categories are seeded).
2. Add an income (e.g. Salary) and an expense (e.g. Groceries).
3. Check the **Dashboard**, then **Reports**.
4. Register a **second account** — its data starts empty and is never mixed with the first.

---

## API Overview

Public:

- `POST /api/auth/register` — register; returns JWT + profile; seeds default categories
- `POST /api/auth/login` — login; returns JWT

Authenticated (put `Authorization: Bearer <token>` on every request):

- `GET  /api/auth/me` — current profile (used to restore the session)
- `GET  /api/transactions` — list with optional `type`, `categoryId`, `month` (yyyy-MM), `search`
- `POST /api/transactions` — create (validates amount > 0, category belongs to you and
  matches the transaction type)
- `GET/PUT/DELETE /api/transactions/{id}` — read/update/delete (404 if not yours)
- `GET  /api/categories` — list yours (optional `type=` filter)
- `POST/PUT/DELETE /api/categories/{id}` — manage categories (409 if name/type duplicated,
  409 if still in use)
- `GET  /api/dashboard/summary` — totals, balance, monthly series, category breakdown, recent
- `GET  /api/reports/monthly?month=yyyy-MM` — income/expense/balance + category breakdown
- `GET  /api/reports/category?month=yyyy-MM&type=INCOME` — category totals (defaults: current month, EXPENSE)

Money is always `BigDecimal`/`NUMERIC(14,2)`; dates are `yyyy-MM-dd`. Error responses are
uniform JSON: `{ timestamp, status, error, message }` (`400` bad input, `401` unauthenticated,
`404` missing/not yours, `409` conflict).

---

## Security & Data Rules

- Passwords are stored only as **BCrypt hashes**; the JWT secret never leaves env config.
- All repositories are scoped by `user_id`; controllers resolve the user from the JWT
  (`@AuthenticationPrincipal`), never trust the client.
- A transaction's type must match its category's type (`EXPENSE ↔ EXPENSE`),
  otherwise `409 Conflict`.
- Deleting a category that still has transactions is blocked (`409 Conflict`).
- Aggregation for dashboards/reports is computed on the JPA-loaded dataset in the service
  layer (portable; no DB-specific SQL functions).

---

## Testing

```powershell
cd backend
mvn test          # 17 tests (services + MVC slice), all use mocks / in-memory
mvn package       # build the runnable jar (target\expense-tracker-backend-1.0.0.jar)
cd frontend
npm run build     # TypeScript check + production bundle
```

Coverage: auth (register/login/duplicate/wrong password), transaction service rules
(wrong-category type, foreign id, filters, delete), dashboard and report aggregation,
and a `@WebMvcTest` controller slice verifying status codes and JSON error shape
(201/400/401/404).

An end-to-end smoke test (real PostgreSQL + running backend) was executed during
development covering: register → login → restore session → seeded categories → add
income/expense → dashboard totals → filters → reports → edit → delete → category delete
guard → full **two-user data isolation**.

---

## Future Improvements

- Refresh/refresh-token rotation and logout blacklist
- Pagination for large transaction lists
- CSV export and budget targets per category
- User profile editing and password change
- Rate limiting and account lockout

---

## Notes

- This project was migrated from an early **v1** (expense-only, no users); old tables are
  dropped by the setup script, so starting fresh is the recommended path.
- Currency is formatted as **Indian Rupees (₹)** via `Intl.NumberFormat('en-IN')`.