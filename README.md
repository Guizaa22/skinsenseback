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

---

### Section 5: Scheduling & Appointment Management ✅

Complete implementation of availability calculation and appointment booking system:

**5.1 Availability Calculation (SchedulingService):**
- Deterministic algorithm for calculating available time slots
- 15-minute slot granularity for precise scheduling
- Considers employee weekly schedules, absences, and existing appointments
- Multi-day availability queries (1-30 days)
- Timezone-aware with OffsetDateTime (Tunisia UTC+1)
- Filters out canceled appointments from availability calculation
- Service duration validation and retrieval

**5.2 Appointment Creation (BookingService):**
- Transactional appointment booking with availability validation
- Database-level conflict prevention using GiST exclusion constraints
- Race condition handling with 409 Conflict responses
- Automatic end time calculation based on service duration
- Client, employee, and service existence validation
- Comprehensive logging for debugging and audit trails

**5.3 Appointment Management:**
- Full CRUD operations with role-based access control
- Status transitions: CONFIRMED → COMPLETED or CANCELED
- Cancellation tracking with reason field
- Reschedule functionality (ADMIN/EMPLOYEE only)
- Complete appointment marking (EMPLOYEE/ADMIN only)
- Filtering by client, employee, status, and date range
- Access control: clients see own appointments, employees see own calendar, admins see all

**Security & Access Control:**
- `GET /api/availability` - Authenticated users (check availability)
- `GET /api/appointments` - Role-based filtering (CLIENT: own, EMPLOYEE: calendar, ADMIN: all)
- `POST /api/appointments` - Authenticated users (CLIENT: self-booking, ADMIN: book for others)
- `PUT /api/appointments/{id}` - ADMIN/EMPLOYEE only (reschedule)
- `POST /api/appointments/{id}/cancel` - Own appointments or ADMIN/EMPLOYEE
- `POST /api/appointments/{id}/complete` - ADMIN/EMPLOYEE only

**API Endpoints:**
- `GET /api/availability` - Get available time slots for employee and service
- `GET /api/appointments` - List appointments with filters
- `GET /api/appointments/{id}` - Get appointment details
- `POST /api/appointments` - Create new appointment
- `PUT /api/appointments/{id}` - Update/reschedule appointment
- `POST /api/appointments/{id}/cancel` - Cancel appointment
- `POST /api/appointments/{id}/complete` - Mark appointment as completed

**Testing:**
- 17 comprehensive unit tests for AvailabilityService
- Edge case coverage: boundary times, exact fit, overlaps, absences
- Multi-day availability testing
- Timezone handling verification
- Canceled appointment filtering
- Service validation (not found, inactive)

See `API_ROUTES_DOCUMENTATION.md` for detailed API documentation.

---

### Section 6: Client File & Consent Management ✅

Complete implementation of sensitive medical data management and SMS notification consent:

**6.1 Client File (Medical Dossier):**
- Comprehensive client medical and personal information management
- Structured sections: Intake, Medical History, Aesthetic Procedure History
- Photo consent tracking (follow-up and marketing)
- Timezone-aware timestamps (OffsetDateTime)
- Automatic file creation on first access

**6.2 Strict Access Control:**
- **Clients**: Full read/write access to their own declarative sections only
- **Employees/Admins**: Read-only access to any client file (cannot modify client-provided data)
- Separate endpoints for client self-service vs. staff access
- Role-based authorization with `@PreAuthorize` annotations
- Access violations return 403 Forbidden

**6.3 SMS Consent Management:**
- SMS opt-in/opt-out preferences per client
- Auto-generated UUID-based unsubscribe tokens
- Public unsubscribe endpoint (no authentication required)
- Token persistence across consent updates
- Default opt-in for new clients

**6.4 Comprehensive Audit Logging:**
- All sensitive data operations logged to audit trail
- CREATE, UPDATE, DELETE, and READ operations tracked
- JSON serialization of entity snapshots (before/after states)
- Actor tracking (who performed the action)
- Timezone-aware timestamps for compliance

**Security & Compliance:**
- GDPR/HIPAA-ready audit trail for sensitive medical data
- Immutable audit entries for legal compliance
- Access control enforced at service layer
- All endpoints require JWT authentication (except public unsubscribe)

**API Endpoints:**
- `GET /api/client/me/file` - Client reads own file
- `PUT /api/client/me/file` - Client updates own file
- `GET /api/clients/{id}/file` - Employee/Admin reads any client file
- `GET /api/client/me/consent` - Client reads own consent
- `PUT /api/client/me/consent` - Client updates consent preferences
- `POST /api/consent/unsubscribe/{token}` - Public SMS unsubscribe

**Testing:**
- Integration tests for access control scenarios
- Role-based authorization validation
- Audit logging verification
- Public unsubscribe token functionality

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
