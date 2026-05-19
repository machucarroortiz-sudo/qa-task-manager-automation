# QA Task Manager SUT

QA Task Manager SUT is a small Spring Boot task management application created as a controlled system under test for QA automation practice.

The project is designed to support UI testing, API testing, role-based access control testing, validation testing, test data management, and regression practice. It is intentionally simple, stable, and auditable so the application behavior is easy to understand, review, and test.

## Quick Setup

### Requirements

- Java 25 LTS
- Maven 3.9+
- Docker Desktop

### Run locally with Maven

```bash
mvn spring-boot:run
```

### Run with Docker Compose

```bash
docker compose up --build
```

Docker Compose starts the application with PostgreSQL and exposes the app on port `8080`.

To stop the containers:

```bash
docker compose down
```

You can also build and run only the application image with the default in-memory H2 database:

```bash
docker build -t qa-task-manager-sut:1.0.0 .
docker run --rm -p 8080:8080 qa-task-manager-sut:1.0.0
```

### Access URLs

| Area | URL |
| --- | --- |
| UI | `http://localhost:8080` |
| Health API | `http://localhost:8080/api/health` |
| Swagger/OpenAPI | `http://localhost:8080/swagger-ui/index.html` |
| H2 Console | `http://localhost:8080/h2-console` when running the default H2 profile |

Docker Compose uses PostgreSQL, so the H2 console is only useful for the default local H2 run mode.

### Demo Users

| Email | Password | Role |
| --- | --- | --- |
| `admin@example.com` | `password123` | `ADMIN` |
| `user1@example.com` | `password123` | `USER` |
| `user2@example.com` | `password123` | `USER` |

The application also creates predefined tasks, labels, issues, and comments for predictable test data.

## Technology Stack

- Java 25 LTS
- Spring Boot 4.0.6
- Maven
- Thymeleaf
- Spring Security with form login for UI and JWT for API access
- Spring Data JPA
- H2 for local development
- PostgreSQL with Docker Compose
- springdoc-openapi 3.0.3 for Swagger/OpenAPI

## SUT Versioning

The SUT starts formal QA tracking at version `1.0.0`, the first stable QA baseline. Earlier GitHub commits are considered initial development snapshots.

Versioning follows `MAJOR.MINOR.PATCH`:

- `PATCH`: defect fixes found during QA, for example `1.0.1`.
- `MINOR`: meaningful compatible improvements, for example `1.1.0`.
- `MAJOR`: important or incompatible SUT changes, for example `2.0.0`.

See [`VERSIONING.md`](VERSIONING.md) for the full versioning rules.

## Application Metadata

The SUT exposes application metadata for QA evidence and issue context.

Available UI area:

- `SUT Information`, available from the sidebar navigation.

Available API endpoint:

- `GET /api/sut-info`

The metadata includes SUT version, release name, lifecycle stage, active Spring profiles, runtime, database, and host information. It intentionally avoids secrets, passwords, full environment variable dumps, and connection strings with credentials.

## Localization

The portfolio UI is English by default and supports Spanish from the language selector on the login page and authenticated top bar. The selected language is stored in the `sut_locale` cookie, so the preference survives navigation without changing API contracts.

Localization is intentionally scoped:

- GUI labels, navigation, forms, validation messages, empty states, and feedback messages use message bundles.
- API routes, DTOs, enums, JSON contracts, code, and test selectors remain in English.
- Task and issue user-entered content is a separate feature. Detail pages include a `Translate content` toggle with an icon-only tooltip warning that the assisted translation is approximate and does not modify stored data.

## Business Rules

- Users can log in.
- Users can create, view, update, complete, and delete their own tasks.
- Users cannot access or modify tasks owned by another user.
- Admins can view and manage all users' tasks.
- Tasks require a title, status, priority, due date, and owner.
- Task due date cannot be in the past.
- Task priority values are `LOW`, `MEDIUM`, and `HIGH`.
- Task status values are `TODO`, `IN_PROGRESS`, `DONE`, and `CANCELLED`.
- Users can create, view, update, delete, comment on, and upload attachments to issues they created or are assigned to.
- Users cannot modify issues created by another user unless they are `ADMIN`.
- Admins can manage all issues and manage labels.
- Issues require title, description, start date, end date, assigned user, status, and priority.
- Issue end date cannot be before start date.
- Issue statuses are `OPEN`, `IN_PROGRESS`, `BLOCKED`, `RESOLVED`, `CLOSED`, and `CANCELLED`.
- Issue priorities are `LOW`, `MEDIUM`, `HIGH`, and `CRITICAL`.
- Comments can include PNG, JPG, and MP4 attachments up to 5 MB.

## API Endpoints

Use `POST /api/auth/login` to obtain a JWT, then send it as:

```text
Authorization: Bearer <token>
```

### Authentication

- `POST /api/auth/login`

### User Information

- `GET /api/users/me`
- `GET /api/users` admin only

### Task CRUD

- `GET /api/tasks`
- `POST /api/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `POST /api/tasks/{id}/complete`
- `DELETE /api/tasks/{id}`

Supported task list query parameters:

- `status`
- `priority`
- `search`
- `page`
- `size`

Example:

```text
GET /api/tasks?status=IN_PROGRESS&search=review&page=0&size=10
```

Task and issue list endpoints return a paginated response with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`.

### Issue CRUD

- `GET /api/issues`
- `POST /api/issues`
- `GET /api/issues/{id}`
- `PUT /api/issues/{id}`
- `DELETE /api/issues/{id}`

Supported issue list query parameters:

- `status`
- `priority`
- `search`
- `label`
- `page`
- `size`

Example:

```text
GET /api/issues?status=OPEN&priority=HIGH&label=security&page=0&size=10
```

### Issue Comments and Attachments

- `GET /api/issues/{id}/comments`
- `POST /api/issues/{id}/comments`
- `POST /api/issues/{issueId}/comments/{commentId}/attachments`
- `GET /api/issues/{issueId}/comments/{commentId}/attachments/{attachmentId}`

### Label Management

- `GET /api/labels`
- `POST /api/labels` admin only
- `PUT /api/labels/{id}` admin only
- `DELETE /api/labels/{id}` admin only

### Admin Task Management

- `GET /api/admin/tasks`
- `PUT /api/admin/tasks/{id}`
- `DELETE /api/admin/tasks/{id}`

### Test Data

- `POST /api/test-data/reset` admin only, enabled in `local` and `test` profiles
- `POST /api/test-data/demo` admin only, loads bundled `demodata.json`
- `POST /api/test-data/import` admin only, imports controlled JSON data

### Dashboard and Notifications

- `GET /api/dashboard/summary`
- `GET /api/notifications`
- `POST /api/notifications/{id}/read`

Example dashboard response:

```json
{
  "tasks": {
    "total": 25,
    "todo": 9,
    "inProgress": 7,
    "done": 7,
    "cancelled": 2
  },
  "issues": {
    "total": 25,
    "open": 8,
    "inProgress": 4,
    "blocked": 4,
    "resolved": 4,
    "closed": 3,
    "cancelled": 2
  },
  "unreadNotifications": 0
}
```

### Health

- `GET /api/health`

Example:

```json
{
  "status": "UP",
  "database": "UP"
}
```

### SUT Information

- `GET /api/sut-info`

This endpoint is authenticated and returns application metadata plus safe runtime, database, and host information for QA reporting.

### Example Task Request

```json
{
  "title": "Create UI smoke tests",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2030-12-31"
}
```

### Example Issue Request

```json
{
  "title": "Upload validation for issue comments",
  "description": "Verify PNG, JPG and MP4 uploads and reject unsupported files.",
  "startDate": "2026-05-18",
  "endDate": "2026-05-25",
  "assignedUserId": 2,
  "status": "OPEN",
  "priority": "HIGH",
  "labelIds": [1, 2]
}
```

Validation and authorization failures return consistent JSON error responses with `status`, `error`, `message`, `path`, and `validationErrors`.

## Test Data Management

The Settings section is available from the main navigation for admin users. It provides controlled data actions for local setup, exploratory checks, and repeatable automation preparation.

Available UI actions:

- Reset SUT Data: restores the compact default seed data.
- Clear All Data: deletes tasks, issues, labels, comments, attachments, notifications, and non-admin users, then keeps only the default admin account so the app remains accessible.
- Load demodata: loads bundled `src/main/resources/demodata.json`.
- Import Data: imports a controlled `.json` file using the same structure as `demodata.json`.

The import feature intentionally uses JSON instead of arbitrary SQL. The application uses Spring Data JPA with H2 locally and PostgreSQL in Docker, so JSON import keeps the operation portable, validates known fields and enums, and avoids executing uncontrolled SQL from the UI.

The reset endpoint is also available for automation setup and cleanup:

```bash
POST /api/test-data/reset
```

The clear endpoint performs the destructive empty-state setup:

```bash
POST /api/test-data/clear
```

After clearing data, sign in with `admin@example.com / password123`. The system contains one admin user and no tasks, issues, labels, comments, attachments, or notifications.

The demo data endpoint loads the larger deterministic dataset:

```bash
POST /api/test-data/demo
```

The import endpoint accepts a multipart JSON file:

```bash
POST /api/test-data/import
```

These endpoints are restricted to admin users and only registered when the `local` or `test` Spring profile is active. This keeps test utility behavior explicit and easy to audit.

Test data endpoints return deterministic counts:

```json
{
  "message": "Demo data loaded",
  "users": 3,
  "tasks": 25,
  "issues": 25,
  "labels": 8,
  "comments": 4
}
```

Bundled demo data includes:

- `admin@example.com / password123 / ADMIN`
- `user1@example.com / password123 / USER`
- `user2@example.com / password123 / USER`
- 25 deterministic tasks
- 25 deterministic issues
- Labels and comments useful for filtering, sorting, RBAC, validation, and regression checks

Default local H2 database:

| Property | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:automation_sut` |
| Username | `sa` |
| Password | empty |

## Project Structure

```text
src/main/java/com/qataskmanager/automation_sut
  config        Spring Security, OpenAPI, seed data
  controller    REST and web controllers
  dto           Request, response, and error contracts
  model         JPA entities and enums
  repository    Spring Data repositories
  security      JWT and user details services
  service       Business rules and authorization logic

src/main/resources/templates
  login, dashboard, settings, SUT information, task, issue, profile, admin, and shared Thymeleaf fragments

src/test/java
  test-data management and i18n bundle checks
```

## Design Decisions for Testability

- Stable seed data and reset endpoint support reliable setup and cleanup.
- Explicit role checks make RBAC behavior easy to test.
- DTO validation keeps negative tests predictable.
- Consistent JSON errors support API assertions.
- Simple Thymeleaf pages keep UI automation stable.
- `data-testid` attributes are included on important UI controls and messages.
- UI localization keeps display text configurable while preserving stable English API and selector contracts.
- Issue attachments are stored in H2/PostgreSQL as simple BLOB data plus metadata to avoid external storage dependencies.
- The label model is intentionally small so admin CRUD tests stay focused on permissions and validation.
- Issue visibility is explicit: admins see all issues; users see issues they created or are assigned to.
- Layered architecture keeps business rules traceable from controller to service.
- Swagger/OpenAPI supports exploratory testing with Postman and manual API checks.
- The SUT includes focused backend checks for test data management and i18n bundle consistency; broader UI/API automation can be added externally or incrementally.
- `SELECTORS.md` documents stable `data-testid` values for UI automation.
- `RBAC_MATRIX.md` documents expected role-based permissions.

## Access from LAN

The application listens on port `8080` for local network access. To open it from another device on the same local network:

1. On the Windows PC running the SUT, open Command Prompt or PowerShell and run `ipconfig`.
2. Find the active Wi-Fi adapter and copy its IPv4 address.
3. On the other device, open `http://192.168.x.x:8080`, replacing `192.168.x.x` with the PC local IP.

Both devices must be connected to the same LAN. Windows Firewall may need to allow inbound traffic on port `8080`. This is LAN access only, not a public internet deployment.
