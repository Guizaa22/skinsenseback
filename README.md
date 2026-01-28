# Beauty Center Backend

Production-ready Spring Boot 3.5.x backend for Beauty Center management system.

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 17 (via Docker)

## Quick Start

### 1. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `beauty_center`
- User: `beauty`
- Password: `beauty`

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
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center
export SPRING_DATASOURCE_USERNAME=beauty
export SPRING_DATASOURCE_PASSWORD=beauty
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

- PostgreSQL 17
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

## Development Notes

- Package-by-feature structure for parallel development
- Constructor injection only (no field injection)
- DTOs use Jakarta validation annotations
- Entities include createdAt/updatedAt with @PrePersist/@PreUpdate
- All code is compiling skeleton with TODO comments (no pseudo-code)
