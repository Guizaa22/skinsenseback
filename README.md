# Beauty Center Backend

Production-ready Spring Boot 3.5.x backend for Beauty Center management system.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 16.11 (via Docker)

## Quick Start

### 1. Start PostgreSQL Database

```bash
docker compose up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `beauty_center_db`
- User: `postgres`
- Password: `mouadhmb12`

Verify with:
```bash
docker ps
```

### 2. Run Backend

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 3. Access API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

API Docs JSON: `http://localhost:8080/v3/api-docs`

## Configuration

All configuration is environment-based via `application.yml`:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=mouadhmb12
export JWT_SECRET=your-secret-key-minimum-32-characters
export JWT_EXP_MINUTES=60
export JWT_REFRESH_EXP_DAYS=7
export SERVER_PORT=8080
```

## Project Structure

```
src/main/java/beauty_center/
├── BeautyCenterApplication.java
├── common/                          # Shared utilities & responses
│   ├── api/ApiResponse.java
│   ├── error/
│   │   ├── ApiError.java
│   │   └── GlobalExceptionHandler.java
│   └── util/TimeUtil.java
├── config/                          # Application configuration
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── security/                        # JWT & Auth security
│   ├── SecurityConfig.java
│   ├── JwtAuthFilter.java
│   ├── JwtService.java
│   └── CurrentUser.java
└── modules/                         # Feature modules (package-by-feature)
    ├── auth/
    ├── users/
    ├── services/
    ├── scheduling/
    ├── appointments/
    ├── clientfile/
    ├── notes/
    ├── notifications/
    └── audit/
```

## Security

- Stateless JWT authentication
- Role-based access control (ADMIN, EMPLOYEE, CLIENT)
- Spring Security filter chain
- CORS configured for frontend integration

## Database

- PostgreSQL 16.11
- Flyway migrations in `src/main/resources/db/migration/`
- JPA/Hibernate ORM
- UUID primary keys

## Testing

```bash
mvn test
```

## Build

```bash
mvn clean package
```

## CI/CD

GitHub Actions workflow: `.github/workflows/ci.yml`

Runs tests on every push and pull request.

## Features

### Section 4: Employee Management ✅

Complete implementation of employee management system with:

**Employee Accounts:**
- Create employee accounts with secure password hashing (BCrypt)
- Update employee information (name, email, phone)
- Activate/deactivate employee accounts
- List and filter employees by active status
- Email uniqueness validation
- Role enforcement (EMPLOYEE role only)

**Working Time Slots:**
- Define employee weekly schedules (day of week + time ranges)
- Full replacement strategy for schedule updates
- Validation: end time > start time
- Overlap detection for same-day time slots
- Support for all days of week (MON-SUN)

**Absences Management:**
- Create and delete employee absences (vacations, time off)
- Date range validation (end > start)
- Overlap detection for existing absences
- Appointment conflict detection (MVP: blocks absence if confirmed appointments exist)
- Timezone-aware timestamps (OffsetDateTime)

**Security:**
- All endpoints require ADMIN role (`@PreAuthorize("hasRole('ADMIN')")`)
- JWT authentication required
- Comprehensive authorization checks

**API Endpoints:**
- `POST /api/admin/employees` - Create employee
- `GET /api/admin/employees` - List/filter employees
- `GET /api/admin/employees/{id}` - Get employee details
- `PUT /api/admin/employees/{id}` - Update employee
- `PUT /api/admin/employees/{id}/working-times` - Replace weekly schedule
- `GET /api/admin/employees/{id}/working-times` - Get schedule
- `POST /api/admin/employees/{id}/absences` - Create absence
- `GET /api/admin/employees/{id}/absences` - Get absences
- `DELETE /api/admin/absences/{absenceId}` - Delete absence

**Testing:**
- Comprehensive integration tests covering all endpoints
- Validation scenario testing (overlaps, invalid dates, etc.)
- Role-based access control testing
- Test coverage for business logic in services

See `API_ROUTES_DOCUMENTATION.md` for detailed API documentation.

---

## Development Notes

- Package-by-feature structure for parallel development
- Constructor injection only (no field injection)
- DTOs use Jakarta validation annotations
- Entities include createdAt/updatedAt with @PrePersist/@PreUpdate
- Transactional service methods with `@Transactional`
- Comprehensive logging with SLF4J
- Integration tests using MockMvc and H2 in-memory database
