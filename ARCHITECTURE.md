# Architecture Beauty Center Backend

## Overview

Production-ready Spring Boot 3.5.x backend for a beauty center management system using **package-by-feature** architecture. Designed for two students to work in parallel without conflicts.

## Architecture Principles

### 1. Package-by-Feature (Not Package-by-Layer)

Each feature module is **completely self-contained**:

```
modules/
├── auth/          (Feature complete: controller + dto)
├── users/         (Feature complete: entity + repo + service + controller + dto)
├── appointments/  (Feature complete: entity + repo + service + controller + dto)
└── ...
```

**Benefits:**
- Easy for parallel development (no conflicts on same packages)
- Clear responsibility boundaries
- Easy to add/remove features
- Testable in isolation

### 2. Layered Within Each Module

Each feature follows standard layering:

```
feature/
├── entity/        (JPA entities with UUID + timestamps)
├── repository/    (Spring Data JPA repositories)
├── service/       (Business logic, @Transactional)
├── controller/    (REST endpoints, ResponseEntity<ApiResponse<T>>)
└── dto/           (Request/Response DTOs with validation)
```

### 3. Shared Infrastructure (common + config)

```
common/
├── api/          (ApiResponse<T> wrapper)
├── error/        (GlobalExceptionHandler, ApiError)
└── util/         (TimeUtil helpers)

config/
├── CorsConfig    (CORS setup)
└── OpenApiConfig (Swagger/OpenAPI)

security/
├── SecurityConfig    (Spring Security filter chain)
├── JwtService        (Token generation/validation)
├── JwtAuthFilter     (JWT filter)
├── JwtProperties     (Configuration)
└── CurrentUser       (SecurityContext helpers)
```

## Security Architecture

### JWT Stateless Authentication

```
Request with "Authorization: Bearer <token>"
    ↓
JwtAuthFilter
    ↓
JwtService.isTokenValid()
    ↓
Extract username
    ↓
Load user (TODO: from database with roles)
    ↓
SecurityContextHolder.setAuthentication()
    ↓
@PreAuthorize/@Secured annotations ready
```

### Role-Based Access Control (RBAC)

Three roles defined:
- **ADMIN**: Full access, user/staff management
- **EMPLOYEE**: Service provider, can create notes, manage own schedule
- **CLIENT**: Booking client, owns appointments

### Public Endpoints

```
GET    /auth/**                    (login, logout, refresh)
GET    /v3/api-docs/**            (Swagger docs)
GET    /swagger-ui/**             (Swagger UI)
GET    /swagger-ui.html           (Swagger UI)
GET    /actuator/health           (Health check)
```

All other endpoints: **requires authentication**

## Data Model

### Core Entities

| Entity | Purpose | Key Fields |
|--------|---------|-----------|
| UserAccount | User base (polymorphic) | id (UUID), email, role, isActive |
| BeautyService | Service catalog | id, name, durationMin, price |
| Appointment | Bookings | id, clientId, employeeId, serviceId, status |
| WorkingTimeSlot | Employee weekly schedule | employeeId, dayOfWeek, startTime, endTime |
| Absence | Time off/vacation | employeeId, startAt, endAt |
| ClientFile | Medical/personal data | clientId, allergies, medicalHistory |
| ProfessionalNote | Post-appointment docs | appointmentId, diagnostic, care |
| NotificationMessage | Email/SMS tracking | recipient, type, status, sentAt |
| AuditEntry | Immutable audit trail | entityType, entityId, action, beforeJson, afterJson |

### Entity Patterns

All entities follow:
- **UUID primary key** (not auto-increment)
- **Timestamps**: `createdAt`, `updatedAt`
- **@PrePersist/@PreUpdate**: Automatic timestamp management
- **No circular dependencies**
- **Soft deletes** where needed (isActive)

## API Response Format

All endpoints return standardized wrapper:

```json
{
  "success": true,
  "message": "Success",
  "data": { /* payload */ },
  "timestamp": "2026-01-28T10:30:00",
  "path": "/api/users/123"
}
```

Error responses:

```json
{
  "status": 400,
  "message": "Validation failed",
  "error": "VALIDATION_ERROR",
  "timestamp": "2026-01-28T10:30:00",
  "path": "/api/users",
  "details": ["Email is required", "Password too short"]
}
```

## Database

### PostgreSQL 17

- **Docker Compose**: Pre-configured `compose.yaml`
- **Migrations**: Flyway (DDL in `src/main/resources/db/migration/`)
- **ORM**: Hibernate via Spring Data JPA
- **Encrypted fields**: Configure for sensitive data (medical history, etc.)

### Connection

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

Environment variables:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center
SPRING_DATASOURCE_USERNAME=beauty
SPRING_DATASOURCE_PASSWORD=beauty
```

## Configuration

### Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| SPRING_DATASOURCE_URL | localhost:5432 | DB connection |
| SPRING_DATASOURCE_USERNAME | beauty | DB user |
| SPRING_DATASOURCE_PASSWORD | beauty | DB password |
| JWT_SECRET | (required) | JWT signing key (min 32 chars) |
| JWT_EXP_MINUTES | 60 | Access token expiration |
| JWT_REFRESH_EXP_DAYS | 7 | Refresh token expiration |
| SERVER_PORT | 8080 | Application port |

### YAML Configuration

All configuration in `application.yml` with environment variable substitution:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/beauty_center}
  jpa:
    hibernate.ddl-auto: validate
  flyway:
    baseline-on-migrate: true
```

## Validation

### Request Validation

DTOs use Jakarta validation annotations:

```java
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Minimum 6 characters")
    private String password;
}
```

### Exception Handling

Global exception handler (`GlobalExceptionHandler`) converts exceptions to `ApiError`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> handleValidationException(...) {
    // Returns 400 with details
}
```

## Module Reference

### auth
- Login/logout/refresh tokens
- No database queries (TODO: implement)
- Response: `LoginResponse` with `accessToken` + `refreshToken`

### users
- CRUD user accounts
- Password hashing with BCrypt
- Email uniqueness validation
- Roles: ADMIN, EMPLOYEE, CLIENT

### services
- Beauty service catalog (facial, massage, etc.)
- Duration + price management
- Active/inactive toggle

### scheduling
- Weekly work schedules (WorkingTimeSlot)
- Absences/vacations (Absence)
- **AvailabilityService**: Rule engine for slot availability
  - Checks working hours
  - Checks no overlapping appointments
  - Checks no absences

### appointments
- Booking management
- Status: PENDING → CONFIRMED → COMPLETED
- Availability validation before create
- Cancellation with reason tracking

### clientfile
- Medical/personal data (sensitive)
- Allergies, treatments, medical history
- Access control: client can edit own, staff read-only
- **Audit logging mandatory**

### notes
- Staff-only post-appointment documentation
- Diagnostic, care performed, reactions
- Linked to appointment
- Next appointment suggestions

### notifications
- Email/SMS sending (interface + SMTP stub)
- Notification scheduling
- Status tracking: SCHEDULED → SENT / FAILED
- Unsubscribe/consent handling

### audit
- Immutable audit trail
- Logs: CREATE, UPDATE, DELETE, READ_SENSITIVE
- Captures: actor, timestamp, before/after JSON
- Filter by entity type, action, date range

## Testing

### Test Structure

```
src/test/java/beauty_center/
└── (mirror src/main structure)
```

### Example Test

```java
@SpringBootTest
class AppointmentServiceTest {
    
    @MockBean
    AppointmentRepository appointmentRepository;
    
    @Autowired
    AppointmentService appointmentService;
    
    @Test
    void testCreateAppointment() {
        // TODO: Implement tests
    }
}
```

Run tests:
```bash
mvn test
```

## CI/CD Pipeline

GitHub Actions workflow: `.github/workflows/ci.yml`

Triggers: Push to `main`/`develop`, Pull Requests

Steps:
1. Checkout code
2. Setup JDK 17
3. Maven compile
4. Run tests (with PostgreSQL service container)
5. Package JAR

## Deployment

### Local Development

```bash
# Start database
docker-compose up -d

# Build
mvn clean install

# Run
mvn spring-boot:run
```

Access:
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### Production

```bash
# Build JAR
mvn clean package

# Run with environment variables
export JWT_SECRET=<production-secret>
java -jar target/beauty_center-0.0.1-SNAPSHOT.jar
```

## Code Quality Standards

### Constructor Injection Only

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

### No Pseudo-Code

All classes are **compiling stubs** with TODO comments:

```java
public UserAccount createUser(UserAccount user) {
    // TODO: Validate email uniqueness
    // TODO: Hash password
    return userAccountRepository.save(user);
}
```

### DTOs for I/O

Controllers accept/return DTOs, not entities:

```java
@PostMapping
public ResponseEntity<ApiResponse<UserResponse>> createUser(
    @Valid @RequestBody UserCreateRequest request) {
    // TODO: Map DTO to entity
    // TODO: Save and return response
}
```

### Transactional Services

```java
@Service
@Transactional
public class AppointmentService {
    // All @Transactional methods
    // Rollback on unchecked exceptions
}
```

## Future Modules (Phase 2+)

- **loyalty**: Points, VIP tiers, rewards
- **packages**: Service bundles, pricing tiers
- **waitlist**: Cancellation alerts, waiting list automation
- **payment**: Stripe/PayPal integration
- **chatbot**: FAQ, booking assistance
- **inventory**: Product inventory management

Each can be added without modifying core modules.

## Team Collaboration

### Parallel Development

Two students can work simultaneously on different modules:

- Student 1: `appointments` + `clientfile`
- Student 2: `scheduling` + `notifications`

**No package conflicts** (package-by-feature isolation)

### Integration Points

Only touch when needed:
- Common DTOs in `common/`
- Repositories via interfaces
- Services via dependency injection

### Code Review Checklist

- [ ] Constructor injection only
- [ ] No null pointer exceptions
- [ ] Proper error handling with ApiError
- [ ] DTOs with validation
- [ ] Transactional service methods
- [ ] No circular dependencies
- [ ] TODO comments for unfinished logic

## Support & Troubleshooting

### Common Issues

**"Cannot resolve symbol 'Jwts'"**
- Run `mvn clean install` to fetch JWT dependencies

**"Database connection refused"**
- Start database: `docker-compose up -d`
- Check connection strings in `application.yml`

**"Port 8080 already in use"**
- Set `SERVER_PORT=8081` environment variable

**"JWT token validation fails"**
- Check `JWT_SECRET` length (minimum 32 characters)
- Verify token format: `Authorization: Bearer <token>`

---

**Status**: Production-ready skeleton ✓  
**Java Version**: 17  
**Spring Boot**: 3.5.0  
**Database**: PostgreSQL 17  
**Architecture**: Package-by-Feature with layering within modules
