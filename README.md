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

### Access URLs

| Area | URL |
| --- | --- |
| UI | `http://localhost:8080` |
| Swagger/OpenAPI | `http://localhost:8080/swagger-ui/index.html` |
| H2 Console | `http://localhost:8080/h2-console` |

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

### Issue CRUD

- `GET /api/issues`
- `POST /api/issues`
- `GET /api/issues/{id}`
- `PUT /api/issues/{id}`
- `DELETE /api/issues/{id}`

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

The reset endpoint is designed for automation setup and cleanup:

```bash
POST /api/test-data/reset
```

It is restricted to admin users and only registered when the `local` or `test` Spring profile is active. This keeps test utility behavior explicit and easy to audit.

Default local H2 database:

| Property | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:automation_sut` |
| Username | `sa` |
| Password | empty |

## Project Structure

```text
src/main/java/com/portfolio/automation_sut
  config        Spring Security, OpenAPI, seed data
  controller    REST and web controllers
  dto           Request, response, and error contracts
  model         JPA entities and enums
  repository    Spring Data repositories
  security      JWT and user details services
  service       Business rules and authorization logic

src/main/resources/templates
  login, dashboard, task, issue, profile, and admin Thymeleaf pages

src/test/java
  empty by design so tests can be created externally or incrementally
```

## Design Decisions for Testability

- Stable seed data and reset endpoint support reliable setup and cleanup.
- Explicit role checks make RBAC behavior easy to test.
- DTO validation keeps negative tests predictable.
- Consistent JSON errors support API assertions.
- Simple Thymeleaf pages keep UI automation stable.
- `data-testid` attributes are included on important UI controls and messages.
- Issue attachments are stored in H2/PostgreSQL as simple BLOB data plus metadata to avoid external storage dependencies.
- The label model is intentionally small so admin CRUD tests stay focused on permissions and validation.
- Issue visibility is explicit: admins see all issues; users see issues they created or are assigned to.
- Layered architecture keeps business rules traceable from controller to service.
- Swagger/OpenAPI supports exploratory testing with Postman and manual API checks.
- The SUT does not include an automation test suite by design.

## Access from LAN

The application listens on port `8080` for local network access. To open it from another device on the same local network:

1. On the Windows PC running the SUT, open Command Prompt or PowerShell and run `ipconfig`.
2. Find the active Wi-Fi adapter and copy its IPv4 address.
3. On the other device, open `http://192.168.x.x:8080`, replacing `192.168.x.x` with the PC local IP.

Both devices must be connected to the same LAN. Windows Firewall may need to allow inbound traffic on port `8080`. This is LAN access only, not a public internet deployment.
