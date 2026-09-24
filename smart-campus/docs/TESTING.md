# Testing

## 1. Backend integration suite — `mvn test`

35 tests across 6 classes in `backend/src/test/java/com/smartcampus/`. Each
class boots its own isolated H2 context (different in-memory DB URL) seeded by
`DataSeeder`, so tests never interfere with each other.

| Suite | Covers |
|---|---|
| `AuthFlowTest` | login for all four roles, wrong-password 401, unauthenticated 403, cross-role access denied (student→admin), admin dashboard access |
| `ParentIsolationTest` | parent sees only linked children; unrelated child → 401; students blocked from `/api/parents/**`; per-child results/fees/timetable/attendance |
| `AttendanceOwnershipTest` | faculty marks own subject OK; foreign subject → 401; admin can mark any subject; students can't mark |
| `ResultIsolationTest` | students see only their own results (empty for students with none); faculty view/record results only for exams on taught subjects (foreign → 401); admin result record uses the path exam id |
| `AdminCatalogTest` | departments/courses/notices/subjects list+create+delete; faculty-by-id; missing faculty → 404; unmapped paths → 404 (not 500); non-admin blocked from admin catalog |
| `UserScopedModuleTest` | notifications (`/me`, unread-count, read-all), leave pending scoping, complaint round-trip (raise → admin resolve), fee scoping, timetable `/my` for all roles, exam scoping (admin/faculty list vs student `/my`/`/next`/`/results/me`), seeded data consistency |

Run (Windows):

```powershell
cd backend
.\mvnw.cmd test
```

```bash
# macOS/Linux
cd backend && ./mvnw test
```

A full clean build: `.\mvnw.cmd clean test package`.

## 2. Smoke suite — 54 live HTTP checks

`smoke-test.ps1` (local helper, kept outside the repo) exercises the running
server end-to-end with real logins: it covers every role portal plus the
write flows — assignment submit + grade, exam create + result record + delete,
fee payment, complaint resolution, timetable conflict detection, parent IDOR,
and faculty subject filtering.

Prerequisites: backend running on `http://localhost:8080`.

```powershell
powershell -ExecutionPolicy Bypass -File C:\Users\panka\AppData\Local\Temp\opencode\smoke-test.ps1
```

Expected: `SMOKE TEST SUMMARY: 54 passed, 0 failed`.

Notes:
- H2 is in-memory, so restart the backend before re-running the smoke suite
  (the suite is single-run-per-boot by design: it submits the demo assignment,
  pays a demo fee, etc.).
- Any backend change → rebuild (junction workdir) → restart on `:8080` →
  re-run this suite before claiming green.

## 3. Frontend — build + lint

```powershell
cd frontend
npm run build   # must exit 0 (dist/ generated)
npm run lint    # must report 0 errors; 36 pre-existing style warnings are expected
```

## 4. Manual verification paths

1. Log in as each demo role (see README) and click through the portal.
2. Parent: open child details (attendance/results/fees/timetable tabs) and
   confirm the data matches student1.
3. Faculty: create/grade an assignment, schedule an exam, record a result.
4. Admin: resolve a complaint, pay a fee, add a timetable slot, view all exams.
5. Cross-check data isolation: a parent has no way to reach another student's
   data; faculty2 cannot open faculty1's subject exams (401).