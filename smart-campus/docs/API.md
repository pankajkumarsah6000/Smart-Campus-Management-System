# API Reference

Base URL: `http://localhost:8080/api` (dev). All routes require a JWT bearer
token (`Authorization: Bearer <accessToken>`) unless marked **public**.

## Envelope

Success: `{ "success": true, "message": "...", "data": ... }`

Error (from `GlobalExceptionHandler`):
`{ "timestamp": "...", "status": 4xx/5xx, "error": "...", "message": "...", "path": "/..." }`
plus `validationErrors` on 400 validation failures.

Status map used by the handler:

| Exception | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `BadRequestException` / validation | 400 |
| `DuplicateResourceException` | 409 |
| `UnauthorizedException` | 401 |
| `BadCredentialsException` (login) | 401 |
| `AccessDeniedException` (wrong role) | 403 |
| Unauthenticated request | 403 (Spring Security default) |
| Unknown/unmapped path (`NoResourceFoundException`) | 404 |
| anything else | 500 |

## Role shorthand

| Short | Roles allowed |
|---|---|
| any | any authenticated user |
| ADMIN | ROLE_ADMIN |
| FACULTY | ROLE_FACULTY (assignment/exam/attendance writes also enforce teaching-subject ownership) |
| STUDENT | ROLE_STUDENT |
| PARENT | ROLE_PARENT (child endpoints enforce parent→student linkage) |

## Auth — `/api/auth`

| Method | Path | Roles |
|---|---|---|
| POST | `/login` | public |
| POST | `/refresh` | any (token) |
| POST | `/logout` | any |

Login body: `{ "email": "...", "password": "..." }` → `data.accessToken`,
`data.refreshToken`, `data.role...`.

## Admin — `/api/admin`

| Method | Path | Roles |
|---|---|---|
| GET | `/dashboard` | ADMIN |
| GET | `/students` | ADMIN |
| GET | `/students/{id}` | ADMIN |
| POST | `/students` | ADMIN |
| PUT | `/students/{id}` | ADMIN |
| DELETE | `/students/{id}` | ADMIN |
| GET/POST | `/faculty` | ADMIN |
| GET | `/faculty/{id}` | ADMIN |
| DELETE | `/faculty/{id}` | ADMIN |
| GET/POST/PUT/DELETE | `/departments` (+`/{id}` for PUT/DELETE) | ADMIN |
| GET/POST | `/courses` | ADMIN |
| DELETE | `/courses/{id}` | ADMIN |
| GET/POST | `/subjects` | ADMIN |
| DELETE | `/subjects/{id}` | ADMIN |
| GET/POST | `/notices` | ADMIN |
| DELETE | `/notices/{id}` | ADMIN |
| GET/POST/PUT/DELETE | `/classrooms` (+`/{id}` for PUT/DELETE) | GET: any; writes ADMIN |

## Student — `/api/students`

| Method | Path | Roles |
|---|---|---|
| GET | `/me` | STUDENT, ADMIN |
| GET | `/me/dashboard` | STUDENT, ADMIN |

## Faculty — `/api/faculty`

| Method | Path | Roles |
|---|---|---|
| GET | `/me` | FACULTY, ADMIN |
| GET | `/me/dashboard` | FACULTY, ADMIN |
| GET | `/me/subjects` | FACULTY, ADMIN |
| GET | `/me/timetable` | FACULTY, ADMIN |
| GET | `/subjects/{subjectId}/students` | FACULTY, ADMIN |

## Attendance — `/api/attendance`

| Method | Path | Roles |
|---|---|---|
| POST | `/mark` | FACULTY, ADMIN (faculty limited to own subjects) |
| GET | `/subject/{subjectId}?date=yyyy-MM-dd` | FACULTY, ADMIN |
| GET | `/me/summary` | STUDENT |
| GET | `/student/{studentId}/summary` | ADMIN, FACULTY, PARENT (parents: linked children only) |

Mark body:
`{ "subjectId": 1, "attendanceDate": "2026-09-21", "entries": [ { "studentId": 1, "status": "PRESENT|ABSENT|LATE|EXCUSED", "remarks": null } ] }`

## Assignments — `/api/assignments`

| Method | Path | Roles |
|---|---|---|
| POST | `` | FACULTY, ADMIN (subject ownership) |
| PUT | `/{id}` | FACULTY, ADMIN (subject ownership) |
| DELETE | `/{id}` | FACULTY, ADMIN (subject ownership) |
| GET | `/my` | any (own list) |
| GET | `/{id}` | any (visibility/ownership checks) |
| POST | `/{id}/submit` | STUDENT (enrolled in the subject) |
| GET | `/{id}/submissions` | FACULTY, ADMIN (teaching faculty or admin) |
| GET | `/submissions/me` | STUDENT |
| PUT | `/submissions/{submissionId}/grade` | FACULTY, ADMIN (teaching faculty or admin) |

Submit body: `{ "textAnswer": "...", "fileUrl": "...", "submittedAt": ... }` —
at least one of `textAnswer`/`fileUrl`.
Grade body: `{ "marksAwarded": 18.5, "feedback": "..." }`.

## Examinations — `/api/exams`

| Method | Path | Roles |
|---|---|---|
| POST | `` | FACULTY, ADMIN (subject ownership) |
| PUT | `/{id}` | FACULTY, ADMIN (subject ownership) |
| DELETE | `/{id}` | FACULTY, ADMIN (subject ownership) |
| GET | `` | ADMIN, FACULTY (faculty filtered to own subjects) |
| GET | `/my` | STUDENT (own enrolled subjects) |
| GET | `/next` | STUDENT |
| GET | `/results/me` | STUDENT (own results only) |
| GET | `/{id}/results` | FACULTY, ADMIN (teaching faculty only) |
| POST | `/{id}/results` | FACULTY, ADMIN; **exam id taken from the path (never the body)** |
| PUT | `/results/{resultId}` | FACULTY, ADMIN (teaching faculty only) |

Create body:
`{ "name": "...", "subjectId": 1, "examDate": "2026-12-15T10:00:00", "durationMinutes": 90, "maxMarks": 100, "venue": "...", }`
Result body: `{ "studentId": 1, "marksObtained": 42, "remarks": "..." }`
(`grade`/`passed` are computed from `maxMarks` when omitted.)

## Timetable — `/api/timetable`

| Method | Path | Roles |
|---|---|---|
| POST | `` | ADMIN (default slot conflicts → 400) |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |
| GET | `?section=&semester=` | any |
| GET | `/my` | any (own role view: student section, faculty subjects) |

Create body:
`{ "subjectId": 1, "section": "A", "semester": "3", "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "10:00", "room": "LT-101" }`

## Fees — `/api/fees`

| Method | Path | Roles |
|---|---|---|
| POST | `` | ADMIN |
| GET | `` | ADMIN |
| GET | `/me` | STUDENT |
| GET | `/student/{studentId}` | ADMIN |
| POST | `/{feeId}/pay` | ADMIN |
| DELETE | `/{feeId}` | ADMIN |

Pay body: `{ "amountPaid": 25000.00 }`.

## Complaints — `/api/complaints`

| Method | Path | Roles |
|---|---|---|
| POST | `` | any (raise) |
| GET | `/me` | any (own) |
| GET | `` | ADMIN |
| PUT | `/{id}/resolve` | ADMIN (status `IN_PROGRESS`/`RESOLVED`/`CLOSED` + optional `resolutionNote`) |

## Parent — `/api/parents`

| Method | Path | Roles |
|---|---|---|
| GET | `/me` | PARENT |
| GET | `/me/children` | PARENT |
| GET | `/children/{studentId}/dashboard` | PARENT (linked child only) |
| GET | `/children/{studentId}/attendance` | PARENT (linked child only) |
| GET | `/children/{studentId}/results` | PARENT (linked child only) |
| GET | `/children/{studentId}/fees` | PARENT (linked child only) |
| GET | `/children/{studentId}/timetable` | PARENT (linked child only) |

## Leave — `/api/leave`

| Method | Path | Roles |
|---|---|---|
| POST | `` | any (apply) |
| GET | `/me` | any (own) |
| GET | `/pending` | ADMIN, FACULTY |
| GET | `/all` | ADMIN |
| PUT | `/{id}/review` | ADMIN, FACULTY |

## Notifications — `/api/notifications`

| Method | Path | Roles |
|---|---|---|
| GET | `/me` | any |
| GET | `/me/unread-count` | any |
| PUT | `/{id}/read` | any (own) |
| PUT | `/me/read-all` | any |

## AI Assistant — `/api/ai`

| Method | Path | Roles |
|---|---|---|
| POST | `/chat` | any (student-grounded) |