# Beauty Center - Complete API Routes Documentation

**Base URL:** `http://localhost:8050`  
**API Version:** 1.0.0  
**Last Updated:** 2026-02-17

---

## 📋 Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Beauty Services](#beauty-services)
4. [Specialties](#specialties)
5. [Employee Management](#employee-management)
6. [Employee Specialties & Services](#employee-specialties--services)
7. [Appointments](#appointments)
8. [Scheduling & Availability](#scheduling--availability)
9. [Client Files](#client-files)
10. [Professional Notes](#professional-notes)
11. [Audit Trail](#audit-trail)
12. [Response Format](#response-format)

---

## 🔐 Authentication

**Base Path:** `/api/auth`  
**Access:** Public (no authentication required for login/refresh)

### POST `/api/auth/login`
**Description:** Authenticate user and receive JWT tokens  
**Access:** Public  
**Request Body:**
```json
{
  "email": "user@beautycenter.com",
  "password": "Password@123"
}
```
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

### POST `/api/auth/refresh`
**Description:** Refresh access token using refresh token  
**Access:** Public  
**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
**Response:** `200 OK` (same as login response)

### GET `/api/auth/me`
**Description:** Get current authenticated user details  
**Access:** Requires authentication  
**Headers:** `Authorization: Bearer {accessToken}`  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@beautycenter.com",
    "fullName": "John Doe",
    "role": "CLIENT",
    "phone": "+1234567890"
  }
}
```

### POST `/api/auth/logout`
**Description:** Invalidate current session tokens  
**Access:** Requires authentication  
**Headers:** `Authorization: Bearer {accessToken}`  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

---

## 👥 User Management

**Base Path:** `/api/users`

### GET `/api/users/profile`
**Description:** Get current user's profile  
**Access:** Requires authentication  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@beautycenter.com",
    "fullName": "John Doe",
    "role": "CLIENT",
    "phone": "+1234567890",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-02-10T14:45:00Z"
  }
}
```

### GET `/api/users/{id}`
**Description:** Get user details by ID  
**Access:** 
  - ADMIN: Can view any user
  - Other roles: Can only view their own profile
**Response:** `200 OK` (same as profile response)

### POST `/api/users`
**Description:** Create new user account  
**Access:** ADMIN only  
**Request Body:**
```json
{
  "email": "newuser@beautycenter.com",
  "fullName": "Jane Smith",
  "phone": "+1987654321",
  "password": "Password@123"
}
```
**Response:** `201 Created` (user details in response)

### POST `/api/users/{id}/activate`
**Description:** Activate a deactivated user account  
**Access:** ADMIN only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User activated successfully",
  "data": null
}
```

### POST `/api/users/{id}/deactivate`
**Description:** Deactivate a user account  
**Access:** ADMIN only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": null
}
```

---

## 💇 Beauty Services

**Base Path:** `/api/services`

### GET `/api/services`
**Description:** Get all beauty services  
**Access:** Requires authentication  
**Query Parameters:**
- `active` (optional): `true` - get only active services, `false` - get all

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Haircut",
      "description": "Professional haircut service",
      "durationMinutes": 30,
      "price": 45.00,
      "isActive": true,
      "specialtyId": "660e8400-e29b-41d4-a716-446655440001",
      "specialtyName": "Hair Care",
      "allowedEmployeeIds": null,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-02-10T14:45:00Z"
    }
  ]
}
```

### GET `/api/services/{id}`
**Description:** Get specific beauty service  
**Access:** Requires authentication  
**Response:** `200 OK` (single service object)

### POST `/api/services`
**Description:** Create new beauty service  
**Access:** ADMIN only  
**Request Body:**
```json
{
  "name": "Hair Styling",
  "description": "Professional hair styling",
  "durationMinutes": 45,
  "price": 60.00,
  "specialtyId": "660e8400-e29b-41d4-a716-446655440001",
  "isActive": true,
  "allowedEmployeeIds": ["550e8400-e29b-41d4-a716-446655440000"]
}
```
**Response:** `201 Created`

### PUT `/api/services/{id}`
**Description:** Update beauty service  
**Access:** ADMIN only  
**Request Body:** Same as POST  
**Response:** `200 OK`

### DELETE `/api/services/{id}`
**Description:** Deactivate (soft delete) a beauty service  
**Access:** ADMIN only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Service deactivated successfully",
  "data": null
}
```

---

## 🏷️ Specialties

**Base Path:** `/api/specialties`

### GET `/api/specialties`
**Description:** Get all specialties  
**Access:** Requires authentication  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Specialties retrieved successfully",
  "data": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "Hair Care",
      "description": "Hair cutting and styling services",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-02-10T14:45:00Z"
    }
  ]
}
```

### GET `/api/specialties/{id}`
**Description:** Get specific specialty  
**Access:** Requires authentication  
**Response:** `200 OK` (single specialty object)

### POST `/api/specialties`
**Description:** Create new specialty  
**Access:** ADMIN only  
**Request Body:**
```json
{
  "name": "Makeup",
  "description": "Professional makeup services"
}
```
**Response:** `201 Created`

### PUT `/api/specialties/{id}`
**Description:** Update specialty  
**Access:** ADMIN only  
**Request Body:** Same as POST  
**Response:** `200 OK`

### DELETE `/api/specialties/{id}`
**Description:** Delete specialty  
**Access:** ADMIN only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Specialty deleted successfully",
  "data": null
}
```

---

## 👔 Employee Management

**Base Path:** `/api/admin`  
**Access:** ADMIN only for all endpoints

### POST `/api/admin/employees`
**Description:** Create new employee account  
**Request Body:**
```json
{
  "email": "employee@beautycenter.com",
  "fullName": "Jane Employee",
  "phone": "+1234567890",
  "password": "Password@123"
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "employee@beautycenter.com",
    "fullName": "Jane Employee",
    "phone": "+1234567890",
    "isActive": true,
    "createdAt": "2024-02-17T10:00:00Z",
    "updatedAt": "2024-02-17T10:00:00Z"
  }
}
```

### GET `/api/admin/employees`
**Description:** Get all employees with optional filters  
**Query Parameters:**
- `isActive` (optional): `true`/`false` - filter by active status

**Response:** `200 OK` (list of employee objects)

### GET `/api/admin/employees/{id}`
**Description:** Get specific employee details  
**Response:** `200 OK` (employee object)

### PUT `/api/admin/employees/{id}`
**Description:** Update employee information  
**Request Body:**
```json
{
  "fullName": "Jane Updated",
  "phone": "+1987654321",
  "isActive": true
}
```
**Response:** `200 OK` (updated employee object)

### GET `/api/admin/employees/{id}/working-times`
**Description:** Get employee's working time slots  
**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "employeeId": "550e8400-e29b-41d4-a716-446655440001",
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-02-10T14:45:00Z"
  }
]
```

### PUT `/api/admin/employees/{id}/working-times`
**Description:** Replace entire weekly working schedule  
**Request Body:**
```json
[
  {
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "17:00:00"
  },
  {
    "dayOfWeek": "TUESDAY",
    "startTime": "09:00:00",
    "endTime": "17:00:00"
  }
]
```
**Response:** `200 OK` (updated schedule)

### GET `/api/admin/employees/{id}/absences`
**Description:** Get employee's absences  
**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "employeeId": "550e8400-e29b-41d4-a716-446655440001",
    "startDate": "2024-02-20",
    "endDate": "2024-02-22",
    "reason": "Sick leave",
    "createdAt": "2024-02-17T10:00:00Z",
    "updatedAt": "2024-02-17T10:00:00Z"
  }
]
```

### POST `/api/admin/employees/{id}/absences`
**Description:** Create absence for employee  
**Request Body:**
```json
{
  "startDate": "2024-02-20",
  "endDate": "2024-02-22",
  "reason": "Vacation"
}
```
**Response:** `201 Created` (absence object)

### DELETE `/api/admin/absences/{absenceId}`
**Description:** Delete absence  
**Response:** `204 No Content`

---

## 👨‍💼 Employee Specialties & Services

**Base Path:** `/api/employees`

### GET `/api/employees/{employeeId}/specialties`
**Description:** Get all specialties for an employee  
**Access:** Requires authentication  
**Response:** `200 OK` (list of specialty objects)

### POST `/api/employees/{employeeId}/specialties`
**Description:** Assign specialty to employee  
**Access:** ADMIN only  
**Request Body:**
```json
{
  "specialtyId": "660e8400-e29b-41d4-a716-446655440001"
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Specialty assigned to employee successfully",
  "data": null
}
```

### DELETE `/api/employees/{employeeId}/specialties/{specialtyId}`
**Description:** Remove specialty from employee  
**Access:** ADMIN only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Specialty removed from employee successfully",
  "data": null
}
```

### GET `/api/employees/{employeeId}/services`
**Description:** Get services that an employee can perform  
**Access:** Requires authentication  
**Response:** `200 OK` (list of service objects)

---

## 📅 Appointments

**Base Path:** `/api/appointments`

### GET `/api/appointments`
**Description:** Get appointments with optional filters  
**Access:** Requires authentication  
**Query Parameters:**
- `clientId` (optional): Filter by client ID
- `employeeId` (optional): Filter by employee ID
- `status` (optional): Filter by status (PENDING, CONFIRMED, COMPLETED, CANCELLED)
- `from` (optional): Start date-time (ISO 8601 format)
- `to` (optional): End date-time (ISO 8601 format)

**Role-based Behavior:**
- CLIENT: Sees only their own appointments
- EMPLOYEE: Sees their own calendar (assigned appointments)
- ADMIN: Sees all appointments

**Response:** `200 OK`
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

### GET `/api/appointments/{id}`
**Description:** Get specific appointment  
**Access:** 
  - Own appointment (as client or employee) or ADMIN
**Response:** `200 OK` (single appointment object)

### POST `/api/appointments`
**Description:** Create new appointment  
**Access:** Requires authentication  
**Request Body:**
```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440001",
  "serviceId": "550e8400-e29b-41d4-a716-446655440003",
  "startAt": "2024-02-20T14:00:00Z",
  "notes": "Optional notes for the appointment"
}
```
**Note:** 
- CLIENT: `clientId` is ignored (uses their own ID)
- ADMIN: Can specify `clientId` for any client
- Employee is auto-assigned based on availability

**Response:** `201 Created`

### PUT `/api/appointments/{id}`
**Description:** Reschedule appointment  
**Access:** 
  - Assigned EMPLOYEE or ADMIN only
**Request Body:**
```json
{
  "employeeId": "550e8400-e29b-41d4-a716-446655440002",
  "serviceId": "550e8400-e29b-41d4-a716-446655440003",
  "startAt": "2024-02-21T14:00:00Z"
}
```
**Response:** `200 OK` (updated appointment)

### POST `/api/appointments/{id}/cancel`
**Description:** Cancel appointment  
**Access:** 
  - CLIENT: Own appointment
  - EMPLOYEE: Own assigned appointment
  - ADMIN: Any appointment
**Request Body:**
```json
{
  "cancellationReason": "Client request"
}
```
**Response:** `200 OK`

### POST `/api/appointments/{id}/reassign`
**Description:** Reassign appointment to different employee  
**Access:** ADMIN only  
**Request Body:**
```json
{
  "employeeId": "550e8400-e29b-41d4-a716-446655440002"
}
```
**Response:** `200 OK` (updated appointment)

### POST `/api/appointments/{id}/complete`
**Description:** Mark appointment as completed  
**Access:** 
  - Assigned EMPLOYEE or ADMIN
**Response:** `200 OK`

---

## 🗓️ Scheduling & Availability

**Base Path:** `/api`

### GET `/api/availability`
**Description:** Get available time slots for service/employee  
**Access:** Requires authentication  
**Query Parameters:**
- `serviceId` (required): UUID of the service
- `employeeId` (optional): UUID of specific employee
- `date` (required): Start date (YYYY-MM-DD)
- `days` (optional): Number of days to check (default: 1, max: 30)

**Example:** `/api/availability?serviceId=xxx&employeeId=yyy&date=2024-02-20&days=7`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Availability retrieved successfully",
  "data": {
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "serviceId": "550e8400-e29b-41d4-a716-446655440001",
    "startDate": "2024-02-20",
    "endDate": "2024-02-26",
    "availableSlots": [
      {
        "date": "2024-02-20",
        "startTime": "09:00:00",
        "endTime": "09:30:00"
      },
      {
        "date": "2024-02-20",
        "startTime": "09:30:00",
        "endTime": "10:00:00"
      }
    ]
  }
}
```

---

## 📋 Client Files

**Base Path:** `/api/client` and `/api/clients`

### GET `/api/client/me/file`
**Description:** Get current client's own file  
**Access:** CLIENT only  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Client file retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "550e8400-e29b-41d4-a716-446655440001",
    "allergies": "Peanut allergic",
    "medicalConditions": "Sensitive skin",
    "preferredProducts": "Organic products",
    "notes": "Client notes",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-02-10T14:45:00Z"
  }
}
```

### PUT `/api/client/me/file`
**Description:** Update current client's own file  
**Access:** CLIENT only  
**Request Body:**
```json
{
  "allergies": "Peanut allergic",
  "medicalConditions": "Sensitive skin",
  "preferredProducts": "Organic products",
  "notes": "Client notes"
}
```
**Response:** `200 OK` (updated file)

### GET `/api/clients/{clientId}/file`
**Description:** Get client file (staff access)  
**Access:** EMPLOYEE or ADMIN  
**Response:** `200 OK` (client file object)

---

## 📝 Professional Notes

**Base Path:** `/api/appointments/{appointmentId}/notes`

### GET `/api/appointments/{appointmentId}/notes`
**Description:** Get professional notes for appointment  
**Access:** 
  - EMPLOYEE/ADMIN: Always allowed
  - CLIENT: Only if config flag enabled
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Notes retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "appointmentId": "550e8400-e29b-41d4-a716-446655440001",
      "authorId": "550e8400-e29b-41d4-a716-446655440002",
      "content": "Note content",
      "createdAt": "2024-02-17T10:00:00Z",
      "updatedAt": "2024-02-17T10:00:00Z"
    }
  ]
}
```

### POST `/api/appointments/{appointmentId}/notes`
**Description:** Create professional note  
**Access:** EMPLOYEE or ADMIN only  
**Request Body:**
```json
{
  "content": "Professional observation about the appointment"
}
```
**Response:** `201 Created`

### PUT `/api/appointments/{appointmentId}/notes/{noteId}`
**Description:** Update professional note  
**Access:** 
  - Original author (employee) or ADMIN
**Request Body:**
```json
{
  "content": "Updated note content"
}
```
**Response:** `200 OK` (updated note)

---

## 🔍 Audit Trail

**Base Path:** `/api/admin/audit`  
**Access:** ADMIN only

### GET `/api/admin/audit`
**Description:** Get audit entries with optional filters  
**Query Parameters:**
- `entityType` (optional): Type of entity (e.g., "Appointment", "User")
- `entityId` (optional): ID of the entity
- `action` (optional): Action performed (e.g., "CREATE", "UPDATE", "DELETE")

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Audit entries retrieved",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "entityType": "Appointment",
      "entityId": "550e8400-e29b-41d4-a716-446655440001",
      "action": "CREATE",
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "changes": {...},
      "timestamp": "2024-02-17T10:00:00Z"
    }
  ]
}
```

### GET `/api/admin/audit/{id}`
**Description:** Get specific audit entry  
**Response:** `200 OK` (single audit entry)

---

## 📊 Response Format

All API responses follow a standard format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "statusCode": 400
}
```

### Status Codes
- `200 OK` - Successful GET, PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `422 Unprocessable Entity` - Business logic violation
- `500 Internal Server Error` - Server error

---

## 🔐 Authentication Header

All authenticated endpoints require:
```
Authorization: Bearer {accessToken}
```

## 🔑 User Roles

- **ADMIN** - Full system access
- **EMPLOYEE** - Can manage appointments, create notes, view schedules
- **CLIENT** - Can book appointments, view own profile and file

---

## ✅ Complete Route Summary

| Method | Path | Auth | Role |
|--------|------|------|------|
| POST | `/api/auth/login` | ❌ | - |
| POST | `/api/auth/refresh` | ❌ | - |
| GET | `/api/auth/me` | ✅ | Any |
| POST | `/api/auth/logout` | ✅ | Any |
| GET | `/api/users/profile` | ✅ | Any |
| GET | `/api/users/{id}` | ✅ | Any* |
| POST | `/api/users` | ✅ | ADMIN |
| POST | `/api/users/{id}/activate` | ✅ | ADMIN |
| POST | `/api/users/{id}/deactivate` | ✅ | ADMIN |
| GET | `/api/services` | ✅ | Any |
| GET | `/api/services/{id}` | ✅ | Any |
| POST | `/api/services` | ✅ | ADMIN |
| PUT | `/api/services/{id}` | ✅ | ADMIN |
| DELETE | `/api/services/{id}` | ✅ | ADMIN |
| GET | `/api/specialties` | ✅ | Any |
| GET | `/api/specialties/{id}` | ✅ | Any |
| POST | `/api/specialties` | ✅ | ADMIN |
| PUT | `/api/specialties/{id}` | ✅ | ADMIN |
| DELETE | `/api/specialties/{id}` | ✅ | ADMIN |
| POST | `/api/admin/employees` | ✅ | ADMIN |
| GET | `/api/admin/employees` | ✅ | ADMIN |
| GET | `/api/admin/employees/{id}` | ✅ | ADMIN |
| PUT | `/api/admin/employees/{id}` | ✅ | ADMIN |
| GET | `/api/admin/employees/{id}/working-times` | ✅ | ADMIN |
| PUT | `/api/admin/employees/{id}/working-times` | ✅ | ADMIN |
| GET | `/api/admin/employees/{id}/absences` | ✅ | ADMIN |
| POST | `/api/admin/employees/{id}/absences` | ✅ | ADMIN |
| DELETE | `/api/admin/absences/{absenceId}` | ✅ | ADMIN |
| GET | `/api/employees/{id}/specialties` | ✅ | Any |
| POST | `/api/employees/{id}/specialties` | ✅ | ADMIN |
| DELETE | `/api/employees/{id}/specialties/{specId}` | ✅ | ADMIN |
| GET | `/api/employees/{id}/services` | ✅ | Any |
| GET | `/api/appointments` | ✅ | Any |
| GET | `/api/appointments/{id}` | ✅ | Any* |
| POST | `/api/appointments` | ✅ | Any |
| PUT | `/api/appointments/{id}` | ✅ | EMP/ADM |
| POST | `/api/appointments/{id}/cancel` | ✅ | Any* |
| POST | `/api/appointments/{id}/reassign` | ✅ | ADMIN |
| POST | `/api/appointments/{id}/complete` | ✅ | EMP/ADM |
| GET | `/api/availability` | ✅ | Any |
| GET | `/api/client/me/file` | ✅ | CLIENT |
| PUT | `/api/client/me/file` | ✅ | CLIENT |
| GET | `/api/clients/{id}/file` | ✅ | EMP/ADM |
| GET | `/api/appointments/{id}/notes` | ✅ | Any* |
| POST | `/api/appointments/{id}/notes` | ✅ | EMP/ADM |
| PUT | `/api/appointments/{id}/notes/{noteId}` | ✅ | EMP/ADM |
| GET | `/api/admin/audit` | ✅ | ADMIN |
| GET | `/api/admin/audit/{id}` | ✅ | ADMIN |

**Legend:**
- ✅ = Requires authentication
- ❌ = Public endpoint
- Any* = Role-based access control
- EMP/ADM = EMPLOYEE or ADMIN

