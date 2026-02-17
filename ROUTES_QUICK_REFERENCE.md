# Beauty Center - Quick Routes Reference

## 🚀 Quick Access Guide

### Authentication Routes
```
POST   /api/auth/login          → Authenticate & get tokens
POST   /api/auth/refresh        → Refresh access token
GET    /api/auth/me             → Get current user info
POST   /api/auth/logout         → Logout
```

### User Management Routes
```
GET    /api/users/profile       → Get my profile
GET    /api/users/{id}          → Get user by ID
POST   /api/users               → Create new user (ADMIN)
POST   /api/users/{id}/activate → Activate user (ADMIN)
POST   /api/users/{id}/deactivate → Deactivate user (ADMIN)
```

### Beauty Services & Specialties
```
GET    /api/services            → List all services
GET    /api/services/{id}       → Get service details
POST   /api/services            → Create service (ADMIN)
PUT    /api/services/{id}       → Update service (ADMIN)
DELETE /api/services/{id}       → Delete service (ADMIN)

GET    /api/specialties         → List all specialties
GET    /api/specialties/{id}    → Get specialty details
POST   /api/specialties         → Create specialty (ADMIN)
PUT    /api/specialties/{id}    → Update specialty (ADMIN)
DELETE /api/specialties/{id}    → Delete specialty (ADMIN)
```

### Employee Management (ADMIN Only)
```
POST   /api/admin/employees     → Create employee
GET    /api/admin/employees     → List all employees
GET    /api/admin/employees/{id} → Get employee details
PUT    /api/admin/employees/{id} → Update employee

GET    /api/admin/employees/{id}/working-times
PUT    /api/admin/employees/{id}/working-times
GET    /api/admin/employees/{id}/absences
POST   /api/admin/employees/{id}/absences
DELETE /api/admin/absences/{absenceId}
```

### Employee Specialties & Services
```
GET    /api/employees/{id}/specialties → Get employee specialties
POST   /api/employees/{id}/specialties → Assign specialty (ADMIN)
DELETE /api/employees/{id}/specialties/{specId} → Remove specialty (ADMIN)
GET    /api/employees/{id}/services    → Get employee services
```

### Appointment Management
```
GET    /api/appointments        → List appointments (filtered by role)
GET    /api/appointments/{id}   → Get appointment details
POST   /api/appointments        → Create appointment
PUT    /api/appointments/{id}   → Reschedule (EMPLOYEE/ADMIN)
POST   /api/appointments/{id}/cancel → Cancel appointment
POST   /api/appointments/{id}/reassign → Reassign employee (ADMIN)
POST   /api/appointments/{id}/complete → Mark completed (EMPLOYEE/ADMIN)
```

### Scheduling & Availability
```
GET    /api/availability        → Get available time slots
         Query params:
         - serviceId (required)
         - employeeId (optional)
         - date (required): YYYY-MM-DD
         - days (optional): 1-30
```

### Client Files
```
GET    /api/client/me/file      → Get my file (CLIENT)
PUT    /api/client/me/file      → Update my file (CLIENT)
GET    /api/clients/{id}/file   → Get client file (EMPLOYEE/ADMIN)
```

### Professional Notes
```
GET    /api/appointments/{id}/notes → List appointment notes
POST   /api/appointments/{id}/notes → Create note (EMPLOYEE/ADMIN)
PUT    /api/appointments/{id}/notes/{noteId} → Update note (author/ADMIN)
```

### Audit & Security
```
GET    /api/admin/audit         → Get audit entries (ADMIN)
GET    /api/admin/audit/{id}    → Get audit entry (ADMIN)
```

---

## 📊 Role-Based Access

### ADMIN
- Create/update/delete services and specialties
- Manage all employees and schedules
- Create/assign/remove employee specialties
- View/manage all appointments
- Reassign appointments
- Access all client files
- View audit logs

### EMPLOYEE
- View own appointments and schedule
- Update/complete assigned appointments
- Create and update professional notes
- View client files
- Access services based on specialties

### CLIENT
- View own profile
- Book appointments
- Cancel own appointments
- Update own client file
- View own appointment details
- View professional notes (if enabled)

---

## 🔐 Authentication Header Format

```
Authorization: Bearer {accessToken}
```

All endpoints except `/api/auth/login` and `/api/auth/refresh` require this header.

---

## 📋 Common Query Parameters

### Appointments List
```
GET /api/appointments?clientId={uuid}&employeeId={uuid}&status=PENDING&from=2024-02-20T00:00:00Z&to=2024-02-27T23:59:59Z
```

**Status values:** PENDING, CONFIRMED, COMPLETED, CANCELLED

### Services List
```
GET /api/services?active=true
```

### Employees List
```
GET /api/admin/employees?isActive=true
```

### Audit Entries
```
GET /api/admin/audit?entityType=Appointment&entityId={uuid}&action=CREATE
```

---

## ⚠️ Important Notes

1. **Client ID in Appointments**: 
   - CLIENTs cannot book for others
   - ADMIN can specify clientId for any client
   - Employee is auto-assigned based on availability

2. **Availability Endpoint**:
   - Maximum 30 days per request
   - Can query for specific employee or all available employees

3. **Working Hours**:
   - Format: HH:MM:SS
   - Day of week: MONDAY-SUNDAY

4. **Date Formats**:
   - Date: YYYY-MM-DD
   - DateTime: ISO 8601 (2024-02-20T14:00:00Z)

5. **Soft Delete**:
   - Services and users can be deactivated
   - Specialties can be hard deleted

---

## 🔄 Example Request & Response

### Login
**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@beautycenter.com",
    "password": "Password@123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

### Get Appointments
**Request:**
```bash
curl -X GET "http://localhost:8080/api/appointments?status=CONFIRMED" \
  -H "Authorization: Bearer {accessToken}"
```

**Response:**
```json
{
  "success": true,
  "message": "Appointments retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "clientId": "550e8400-e29b-41d4-a716-446655440001",
      "employeeId": "550e8400-e29b-41d4-a716-446655440002",
      "serviceId": "550e8400-e29b-41d4-a716-446655440003",
      "startAt": "2024-02-20T14:00:00Z",
      "endAt": "2024-02-20T14:30:00Z",
      "status": "CONFIRMED",
      "cancellationReason": null,
      "createdAt": "2024-02-17T10:00:00Z",
      "updatedAt": "2024-02-17T10:00:00Z"
    }
  ]
}
```

---

## 🎯 Useful Postman Collections

Most endpoints are compatible with Postman. Check the included `postman_auth_collection.json` for a pre-configured collection.

---

## 📞 Support

For detailed API documentation, refer to `PROJECT_ROUTES.md` or check the swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

---

**Last Updated:** 2026-02-17  
**API Version:** 1.0.0

