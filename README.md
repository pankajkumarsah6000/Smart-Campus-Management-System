# Smart Campus Management System

The application source, setup instructions, and project documentation are in
[`smart-campus/`](smart-campus/).

## Demo login accounts

These accounts are created by the backend seeder when it initializes a fresh
database. They are for local/demo use only.

| Role | Email | Password |
|---|---|---|
| Admin | `admin@smartcampus.edu` | `Admin@123` |
| Faculty | `faculty1@smartcampus.edu` | `Faculty@123` |
| Student | `student1@smartcampus.edu` | `Student@123` |
| Parent | `parent@smartcampus.edu` | `Parent@123` |

An existing database account is not overwritten by the seeder. Therefore,
these demo passwords may not work on a deployed instance if its database
already has different account data. Never use these demo credentials for
real accounts.

See [`smart-campus/README.md`](smart-campus/README.md) for all seeded demo
accounts, setup steps, architecture, and deployment notes.
