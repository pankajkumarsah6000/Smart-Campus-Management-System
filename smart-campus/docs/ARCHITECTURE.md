# Architecture

## Stack

- **Backend** — Spring Boot 3.3 (Java 17), Spring Web, Spring Data JPA,
  Spring Security, validation, springdoc-openapi, JJWT, H2 (dev) / Postgres +
  MySQL (deploy), Lombok (entities) — Maven wrapper, single module.
- **Frontend** — React 19, Vite 8, Tailwind CSS v4, react-router 7, axios,
  lucide-react, recharts. Single-page app with role-aware layouts.

## Backend layering

```
Controller (@RestController, @PreAuthorize)
   -> Service interface + ServiceImpl (@Transactional(readOnly = true) at class level)
        -> Repository (Spring Data JPA)
             -> Entity (JPA, Lombok)
```

- **Transactions**: every service impl is `@Transactional(readOnly = true)` at
  class level (this is what fixed universal `LazyInitializationException`
  500s — `open-in-view` is disabled); each write method declares its own
  `@Transactional`. LAZY relationships are always mapped to DTOs inside the
  transaction.
- **DTOs**: manual getters/setters, request DTOs carry `jakarta.validation`
  annotations; entities never leave the service layer.
- **Exception handling**: a single `GlobalExceptionHandler` maps exceptions to
  the consistent error envelope (see `docs/API.md`).

## Security model

1. **Route matchers** (`SecurityConfig`): coarse role guards on URL prefixes
   (`/api/admin/**`, `/api/faculty/**`, `/api/students/me/**`,
   `/api/parents/**`, `/api/attendance/mark`, leave review routes).
2. **Method security** (`@PreAuthorize`): second, exact role check on the
   controller method.
3. **Service-level ownership checks** (the real safety net):
   - **Parent → child**: every `ParentServiceImpl` child method verifies the
     student is in the parent's `children` set, else `UnauthorizedException` (401).
   - **Faculty → subject**: assignment/exam/attendance writes call
     `requireTeaching(...)` / faculty-subject checks. A faculty member can
     only see and grade submissions, create exams, mark attendance, and view
     results for the subjects they are assigned to.
   - **Student → own data**: attendance summaries, results (`/exams/results/me`),
     fees (`/fees/me`), assignments, and complaints always resolve by the
     authenticated user, never by a client-supplied student id.
   - **Path ids are authoritative**: e.g. `POST /api/exams/{id}/results` ignores
     any body `examinationId` and uses the path id.
4. **JWT**: access token (1h) + refresh token (7d); the axios interceptor
   transparently refreshes. Passwords BCrypt-hashed.

## Data isolation invariants (covered by integration tests)

| Guard | Test |
|---|---|
| Wrong password / unauthenticated rejected | `AuthFlowTest` |
| Cross-role access rejected (student → admin, parent → student APIs) | `AuthFlowTest` |
| Parent sees linked child only; unrelated child → 401 | `ParentIsolationTest` |
| Faculty marks attendance for own subject only (foreign → 401), admin bypass | `AttendanceOwnershipTest` |
| Students see only own results; faculty see/record results only for own-subject exams | `ResultIsolationTest` |

## Frontend structure

- `context/AuthContext` — user + roles from the JWT payload; `ProtectedRoute`
  gates routes by allowed role and redirects to `/login` or a 404.
- `layouts/DashboardLayout` — role-aware sidebar (nav arrays live in
  `App.jsx`), notification bell, theme toggle.
- `services/` — thin axios wrappers matching the backend routes 1:1; `api.js`
  unwraps the envelope (`r.data.data`) and refreshes tokens on 401.
- `pages/{admin,student,faculty,parent}/` — one screen per module; shared
  `Modal`, `FormField`, `Badge`, `LoadingState`, `ErrorState`, `EmptyState`,
  `DataTable` components.

### Routing map (React Router)

| Area | Routes |
|---|---|
| Admin | `/admin`, `/admin/students`, `/admin/faculty`, `/admin/departments`, `/admin/courses`, `/admin/classrooms`, `/admin/fees`, `/admin/timetable`, `/admin/exams`, `/admin/complaints`, `/admin/leave-requests`, `/admin/notices` |
| Student | `/student`, `/student/attendance`, `/student/assignments`, `/student/exams`, `/student/fees`, `/student/timetable`, `/student/complaints`, `/student/leave`, `/student/ai-assistant`, `/student/profile` |
| Faculty | `/faculty`, `/faculty/assignments`, `/faculty/exams`, `/faculty/timetable`, `/faculty/students` |
| Parent | `/parent`, `/parent/children/:studentId` (tabbed child details) |

## Data model (22 entities)

`User`, `Role`, `Student`, `Faculty`, `Parent`, `Department`, `Course`,
`Subject`, `Enrollment`, `Timetable`, `Attendance`, `Assignment`,
`Submission`, `Examination`, `Result`, `Fee`, `Complaint`, `LeaveRequest`,
`Notice`, `Notification`, `AIConversation`, `Classroom`.

Relations of note: `Parent` ↔ `Student` (many-to-many via `parent_student` —
the basis of parent-child isolation); `Subject.Faculty` (single instructor —
the basis of faculty ownership checks); `Enrollment` links students to subjects.

## Known pre-existing polish items

- Frontend lint reports 36 pre-existing style warnings (e.g. set-state-in-effect
  pattern shared across pages); no errors.
- The dev AI chat returns a data-grounded fallback unless `AI_PROVIDER_API_KEY`
  is configured.