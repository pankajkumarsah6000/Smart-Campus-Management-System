# Smart Campus Management System

A centralized Web platform connecting students, faculty, admins, and parents —
built module by module, each one taken to real working depth. React 19 frontend
(Vite + Tailwind v4) + Spring Boot 3 backend (Java 17, JPA, Spring Security,
JWT), running end-to-end against an in-memory H2 database with a seeded demo
dataset.

> Status: green. `mvn clean test` passes (20 integration tests), the 54-check
> smoke suite passes 54/54, and `npm run build` + `npm run lint` are clean
> (0 errors; only pre-existing style warnings).

## What's implemented (functional, not stubbed)

### Roles & permissions
- **JWT auth** — access + refresh tokens, BCrypt password hashing, role-based
  route protection (`ADMIN` / `FACULTY` / `STUDENT` / `PARENT`).
- **Defense-in-depth** — role checks in `SecurityConfig` *and* per-method
  `@PreAuthorize`.
- **Fine-grained data isolation** — a parent can only view data for their
  *linked* children; a faculty member can only manage assignments/exams/
  attendance for the *subjects they teach*; students only ever see their own
  attendance, results, fees, assignments, and complaints.
- **Consistent envelope** — every endpoint returns `{ success, message, data }`;
  errors return `{ timestamp, status, error, message, path }` (plus
  `validationErrors` for 400s).
- **Never trust client-supplied IDs** — authoritative IDs come from the URL
  path (e.g. result records use the exam id from `/api/exams/{id}/results`),
  and ownership is re-checked server-side on every read.

### Admin portal (`/admin`)
Dashboard (live counts, attendance %, fee collection %, pending approvals) +
full management screens: students, faculty, departments, courses, classrooms,
**fees** (record/pay/delete), **timetable editor** (conflict detection),
**exams** (schedule/delete + view results), **complaints** (resolve/close),
leave approvals, notices.

### Student portal (`/student`)
Dashboard (attendance %, pending assignments, next exam, today's timetable,
recent notices) + attendance summary, **assignments** (view/submit/see grade),
**exams & results**, **fees**, **timetable**, **complaints** (raise/track),
leave applications, AI Assistant, profile.

### Faculty portal (`/faculty`)
Dashboard (own subjects, student counts, pending work), **assignments**
(create/delete, view submissions, grade), **exams & grades** (schedule, record
results), **timetable**, **students** (roster per own subject).

### Parent portal (`/parent`)
Dashboard + **children** (linked children only), per-child tabbed view:
attendance, results, fees, timetable.

### Cross-cutting
- **Notifications** — real notification bell (unread count, mark-read), backed
  by the `Notification` table (attendance/assignment/result/complaint/leave
  events).
- **AI Campus Assistant** — backend-only LLM calls, data-grounded fallback when
  no provider key is configured, every exchange logged.
- **Reports-ready aggregates** exposed via the dashboards, charts-ready data in
  `recharts` on the frontend.
- Dark/light mode, toasts, loading/empty/error states, responsive sidebar.

## Demo accounts (seeded on first boot by `DataSeeder`)

```
admin@smartcampus.edu   / Admin@123    -> ADMIN    (Campus Administrator)
faculty1@smartcampus.edu / Faculty@123 -> FACULTY  (Anil Sharma, teaches CS301/CS303)
faculty2@smartcampus.edu / Faculty@123 -> FACULTY  (Priya Verma, teaches CS302)
student1@smartcampus.edu / Student@123 -> STUDENT  (Rahul Kumar, ROLL-2024-001)
student2@smartcampus.edu / Student@123 -> STUDENT  (Sneha Gupta,  ROLL-2024-002)
student3@smartcampus.edu / Student@123 -> STUDENT  (Arjun Mehta,  ROLL-2024-003)
parent@smartcampus.edu  / Parent@123   -> PARENT   (Rajesh Kumar, linked to student1)
```

The seeder creates departments, courses, faculty, 3 students, all enrollments,
a weekly timetable, 5 days of attendance, 2 assignments, 2 exams + 1 result,
3 fees, notices, and a complaint — so every screen is populated on first boot.
**Change the credential seeds and JWT secret before any real deployment.**

## Project structure

```
smart-campus/
├── backend/                          Spring Boot 3 / Java 17
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd                Maven wrapper
│   └── src/
│       ├── main/java/com/smartcampus/
│       │   ├── entity/               22 JPA entities
│       │   ├── repository/           Spring Data JPA repos
│       │   ├── dto/{request,response}
│       │   ├── security/             JWT util, filter, UserDetails
│       │   ├── config/               SecurityConfig, DataSeeder
│       │   ├── service/ + service/impl/
│       │   ├── controller/           Auth, Admin, Student, Faculty, Parent,
│       │   │                         Attendance, Assignment, Examination,
│       │   │                         Timetable, Fee, Complaint, Leave,
│       │   │                         Notification, AI, Classroom
│       │   └── exception/            GlobalExceptionHandler
│       └── test/java/com/smartcampus/
│           └── 4 integration suites (20 tests): auth flow, parent-child
│               isolation, attendance ownership, result isolation
└── frontend/                         React 19 / Vite / Tailwind v4
    └── src/
        ├── components/               DataTable, Modal, FormField, Badge, StatCard, …
        ├── layouts/DashboardLayout.jsx (role-aware sidebar + notification bell)
        ├── context/                  Auth, Theme, Toast
        ├── services/                 api.js (axios + refresh interceptor) + per-module wrappers
        ├── routes/ProtectedRoute.jsx
        └── pages/{auth,admin,student,faculty,parent}/
```

## Running it

### Backend (Java 17+)

```bash
cd backend
.\mvnw.cmd clean test       # run the integration suite
.\mvnw.cmd spring-boot:run  # start on http://localhost:8080
```

Defaults (see `application.yml`) use an in-memory H2 database — nothing to
install. Swagger UI at `/swagger-ui.html`, H2 console at `/h2-console` (dev).
To use Postgres/MySQL, set `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` (drivers are on
the classpath). To enable real AI replies, set `AI_PROVIDER_API_KEY`; the
data-grounded fallback works without it.

### Frontend (Node 20+)

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173, proxies /api -> :8080
npm run build               # production build to dist/
npm run lint                # oxlint (0 errors; pre-existing style warnings only)
```

## Documentation

- [`docs/API.md`](docs/API.md) — complete route map with role requirements.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — module design, security
  model, data-isolation rules.
- [`docs/TESTING.md`](docs/TESTING.md) — how to run the test/smoke suites and
  what each one covers.

## Deployment (suggested)

```
Frontend → Vercel        (npm run build → dist/)
Backend  → Render / AWS  (Spring Boot jar + env vars from application.yml/.env)
Database → managed Postgres (Render/RDS/Supabase)
```