#!/bin/bash
# Script to verify all changes have been applied correctly

echo "=== Beauty Center MVP Fixes - Verification Script ==="
echo ""

echo "✓ Checking modified files..."
FILES_MODIFIED=(
  "src/main/java/beauty_center/modules/appointments/service/AppointmentService.java"
  "src/main/java/beauty_center/modules/appointments/controller/AppointmentController.java"
  "src/main/java/beauty_center/modules/users/service/UserAccountService.java"
  "src/main/java/beauty_center/modules/users/controller/UserController.java"
  "src/main/java/beauty_center/security/SecurityConfig.java"
  "src/main/java/beauty_center/security/JwtAuthenticationEntryPoint.java"
  "src/main/java/beauty_center/security/JwtAccessDeniedHandler.java"
  "src/main/java/beauty_center/common/api/ApiResponse.java"
  "src/main/java/beauty_center/common/error/GlobalExceptionHandler.java"
)

for file in "${FILES_MODIFIED[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✓ $file"
  else
    echo "  ✗ MISSING: $file"
  fi
done

echo ""
echo "✓ Checking new migration files..."
MIGRATIONS=(
  "src/main/resources/db/migration/V4__employee_service_authorization.sql"
  "src/main/resources/db/migration/V6__appointment_authorization_enforcement.sql"
)

for file in "${MIGRATIONS[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✓ $file"
  else
    echo "  ✗ MISSING: $file"
  fi
done

echo ""
echo "✓ Checking new test files..."
TESTS=(
  "src/test/java/beauty_center/modules/appointments/AppointmentBackToBackIntegrationTest.java"
  "src/test/java/beauty_center/modules/appointments/AppointmentAuthorizationIntegrationTest.java"
  "src/test/java/beauty_center/modules/appointments/EmployeeServiceValidationIntegrationTest.java"
  "src/test/java/beauty_center/common/api/ApiResponseTest.java"
  "src/test/java/beauty_center/modules/users/service/UserAccountServiceTest.java"
)

for file in "${TESTS[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✓ $file"
  else
    echo "  ✗ MISSING: $file"
  fi
done

echo ""
echo "=== Next Steps ==="
echo "1. Run: mvn clean compile"
echo "2. Run: mvn test"
echo "3. Verify all tests pass"
echo ""

