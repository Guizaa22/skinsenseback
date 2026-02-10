# Beauty Center API - Routes Documentation

**Base URL:** `http://localhost:8080`
**API Version:** 1.0.0
**Authentication:** JWT Bearer Token (except public endpoints)

---

## 📋 Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Beauty Services](#beauty-services)
4. [Appointments](#appointments)
5. [Scheduling & Availability](#scheduling--availability)
6. [Client Files](#client-files)
7. [Professional Notes](#professional-notes)
8. [Audit](#audit)
9. [Test Users](#test-users)
10. [Response Format](#response-format)

---

## 🔐 Authentication

All authentication endpoints are **public** (no token required).

### POST `/api/auth/login`
**Description:** Authenticate user and receive JWT tokens
**Access:** Public
**Request Body:**
```json
{
  "email": "admin@beautycenter.com",
  "password": "Admin@123"
}
```
**Response:**
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

### GET `/api/auth/me`
**Description:** Get current authenticated user details
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "admin@beautycenter.com",
    "fullName": "Admin User",
    "role": "ADMIN"
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
**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

### POST `/api/auth/logout`
**Description:** Logout and invalidate tokens
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

---

## 👥 User Management

### GET `/api/users/profile`
**Description:** Get current user's profile
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Admin User",
    "email": "admin@beautycenter.com",
    "phone": "+1-555-0001",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### GET `/api/users/{id}`
**Description:** Get user by ID
**Access:** ADMIN or own user
**Headers:** `Authorization: Bearer {accessToken}`
**Response:** Same as profile

### POST `/api/users`
**Description:** Create new user account
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "fullName": "New Employee",
  "email": "newemployee@beautycenter.com",
  "phone": "+1-555-0010",
  "role": "EMPLOYEE"
}
```
**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "uuid-here",
    "fullName": "New Employee",
    "email": "newemployee@beautycenter.com",
    "role": "EMPLOYEE",
    "isActive": true
  }
}
```

### POST `/api/users/{id}/deactivate`
**Description:** Deactivate user account
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": null
}
```

### POST `/api/users/{id}/activate`
**Description:** Activate user account
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`

### GET `/api/users/employees`
**Description:** List all employees
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": "Employee list endpoint"
}
```

---

## 💅 Beauty Services

### GET `/api/services`
**Description:** Get all active beauty services
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": "uuid-here",
      "name": "Facial Treatment",
      "description": "Deep cleansing facial",
      "durationMinutes": 60,
      "price": 85.00,
      "isActive": true
    }
  ]
}
```

### GET `/api/services/{id}`
**Description:** Get beauty service by ID
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Service retrieved successfully",
  "data": {
    "id": "uuid-here",
    "name": "Facial Treatment",
    "description": "Deep cleansing facial",
    "durationMinutes": 60,
    "price": 85.00,
    "isActive": true,
    "employees": [
      {
        "id": "employee-uuid",
        "fullName": "Jane Smith"
      }
    ]
  }
}
```

---

## 📅 Appointments

### GET `/api/appointments`
**Description:** Get appointments with filters (client/employee/status)
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `clientId` (optional): Filter by client UUID
- `employeeId` (optional): Filter by employee UUID
- `status` (optional): CONFIRMED, CANCELED, COMPLETED
- `from` (optional): Start date (ISO 8601)
- `to` (optional): End date (ISO 8601)

**Response:**
```json
{
  "success": true,
  "message": "Appointments retrieved successfully",
  "data": [
    {
      "id": "uuid-here",
      "clientId": "client-uuid",
      "employeeId": "employee-uuid",
      "serviceId": "service-uuid",
      "startAt": "2024-02-15T14:00:00Z",
      "endAt": "2024-02-15T15:00:00Z",
      "status": "CONFIRMED",
      "notes": "First time client"
    }
  ]
}
```

### GET `/api/appointments/{id}`
**Description:** Get appointment by ID
**Access:** Authenticated users (own appointments or ADMIN/EMPLOYEE)
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Appointment retrieved successfully",
  "data": {
    "id": "uuid-here",
    "client": {
      "id": "client-uuid",
      "fullName": "John Doe",
      "email": "client@beautycenter.com"
    },
    "employee": {
      "id": "employee-uuid",
      "fullName": "Jane Smith"
    },
    "service": {
      "id": "service-uuid",
      "name": "Facial Treatment",
      "price": 85.00
    },
    "startAt": "2024-02-15T14:00:00Z",
    "endAt": "2024-02-15T15:00:00Z",
    "status": "CONFIRMED",
    "notes": "First time client"
  }
}
```

### POST `/api/appointments`
**Description:** Create new appointment with availability validation
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "clientId": "client-uuid",
  "employeeId": "employee-uuid",
  "serviceId": "service-uuid",
  "startAt": "2024-02-15T14:00:00Z",
  "endAt": "2024-02-15T15:00:00Z",
  "notes": "First time client"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Appointment created",
  "data": {
    "id": "new-uuid",
    "status": "CONFIRMED",
    ...
  }
}
```

### PUT `/api/appointments/{id}`
**Description:** Update existing appointment
**Access:** Authenticated users (own appointments or ADMIN/EMPLOYEE)
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:** Same as POST
**Response:**
```json
{
  "success": true,
  "message": "Appointment updated",
  "data": { ... }
}
```

### PATCH `/api/appointments/{id}/cancel`
**Description:** Cancel appointment
**Access:** Authenticated users (own appointments or ADMIN/EMPLOYEE)
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Appointment canceled",
  "data": null
}
```

---

## 🗓️ Scheduling & Availability

### GET `/api/scheduling/availability/{employeeId}`
**Description:** Check employee availability for a time slot
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `startAt` (required): Start time (ISO 8601)
- `endAt` (required): End time (ISO 8601)

**Example:** `/api/scheduling/availability/employee-uuid?startAt=2024-02-15T14:00:00Z&endAt=2024-02-15T15:00:00Z`

**Response:**
```json
{
  "success": true,
  "message": "Availability checked",
  "data": {
    "available": true,
    "employeeId": "employee-uuid",
    "requestedSlot": {
      "startAt": "2024-02-15T14:00:00Z",
      "endAt": "2024-02-15T15:00:00Z"
    }
  }
}
```

### GET `/api/scheduling/next-slot/{employeeId}`
**Description:** Find next available time slot for employee
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `from` (optional): Start searching from this time (ISO 8601, defaults to now)
- `durationMinutes` (optional): Duration needed (defaults to 60)

**Example:** `/api/scheduling/next-slot/employee-uuid?from=2024-02-15T10:00:00Z&durationMinutes=90`

**Response:**
```json
{
  "success": true,
  "message": "Next slot found",
  "data": {
    "employeeId": "employee-uuid",
    "nextAvailableSlot": {
      "startAt": "2024-02-15T14:00:00Z",
      "endAt": "2024-02-15T15:30:00Z"
    }
  }
}
```

---

## 📋 Client Files

### GET `/api/client-files/{clientId}`
**Description:** Get client file (medical history, consent forms, etc.)
**Access:** ADMIN, EMPLOYEE, or own client file
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Client file retrieved successfully",
  "data": {
    "id": "uuid-here",
    "clientId": "client-uuid",
    "allergies": "Latex, Penicillin",
    "medicalConditions": "Hypertension",
    "currentMedications": "Lisinopril 10mg daily",
    "skinType": "Combination",
    "skinConcerns": "Acne, hyperpigmentation",
    "previousTreatments": "Chemical peel (2023)",
    "contraindications": "None",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+1-555-9999",
    "consentGiven": true,
    "consentDate": "2024-01-15T10:00:00Z",
    "lastUpdated": "2024-02-01T14:30:00Z"
  }
}
```

### PUT `/api/client-files/{clientId}`
**Description:** Update client file with audit logging
**Access:** ADMIN or EMPLOYEE only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "allergies": "Latex, Penicillin, Aspirin",
  "medicalConditions": "Hypertension, Diabetes Type 2",
  "currentMedications": "Lisinopril 10mg, Metformin 500mg",
  "skinType": "Combination",
  "skinConcerns": "Acne, hyperpigmentation, fine lines"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Client file updated",
  "data": { ... }
}
```

---

## 📝 Professional Notes

### GET `/api/notes/appointment/{appointmentId}`
**Description:** Get professional notes for an appointment
**Access:** ADMIN, EMPLOYEE, or client who owns the appointment
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Notes retrieved successfully",
  "data": [
    {
      "id": "note-uuid",
      "appointmentId": "appointment-uuid",
      "authorId": "employee-uuid",
      "authorName": "Jane Smith",
      "noteText": "Client responded well to treatment. Slight redness observed, advised to use SPF 50.",
      "createdAt": "2024-02-15T15:30:00Z"
    }
  ]
}
```

### POST `/api/notes`
**Description:** Create professional note with authorization check
**Access:** ADMIN or EMPLOYEE only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "appointmentId": "appointment-uuid",
  "noteText": "Client responded well to treatment. Slight redness observed, advised to use SPF 50."
}
```
**Response:**
```json
{
  "success": true,
  "message": "Note created",
  "data": {
    "id": "new-note-uuid",
    "appointmentId": "appointment-uuid",
    "authorId": "employee-uuid",
    "noteText": "...",
    "createdAt": "2024-02-15T15:30:00Z"
  }
}
```

---

## 🔍 Audit

### GET `/api/audit`
**Description:** Get audit entries with filters
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `entityType` (optional): Type of entity (e.g., "UserAccount", "Appointment")
- `entityId` (optional): UUID of specific entity
- `action` (optional): Action type (e.g., "CREATE", "UPDATE", "DELETE")

**Example:** `/api/audit?entityType=Appointment&action=UPDATE`

**Response:**
```json
{
  "success": true,
  "message": "Audit entries retrieved successfully",
  "data": [
    {
      "id": "audit-uuid",
      "entityType": "Appointment",
      "entityId": "appointment-uuid",
      "action": "UPDATE",
      "performedBy": "admin@beautycenter.com",
      "performedAt": "2024-02-15T10:30:00Z",
      "changes": {
        "status": {
          "old": "CONFIRMED",
          "new": "CANCELED"
        }
      }
    }
  ]
}
```

### GET `/api/audit/{id}`
**Description:** Get specific audit entry
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Audit entry retrieved successfully",
  "data": {
    "id": "audit-uuid",
    "entityType": "ClientFile",
    "entityId": "client-file-uuid",
    "action": "UPDATE",
    "performedBy": "employee@beautycenter.com",
    "performedAt": "2024-02-15T14:20:00Z",
    "changes": {
      "allergies": {
        "old": "Latex",
        "new": "Latex, Penicillin"
      }
    }
  }
}
```

---

## 👤 Test Users

The following test users are available for testing (created on application startup):

| Role | Email | Password | Description |
|------|-------|----------|-------------|
| **ADMIN** | `admin@beautycenter.com` | `Admin@123` | Full system access |
| **EMPLOYEE** | `employee@beautycenter.com` | `Employee@123` | Staff member access |
| **CLIENT** | `client@beautycenter.com` | `Client@123` | Customer access |

---

## 📦 Response Format

All API responses follow a standardized format using the `ApiResponse` wrapper:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-02-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": 400,
  "timestamp": "2024-02-15T10:30:00Z"
}
```

### Common HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| **200** | OK | Successful GET, PUT, PATCH |
| **201** | Created | Successful POST (resource created) |
| **400** | Bad Request | Invalid request body or parameters |
| **401** | Unauthorized | Missing or invalid authentication token |
| **403** | Forbidden | Authenticated but insufficient permissions |
| **404** | Not Found | Resource doesn't exist |
| **500** | Internal Server Error | Server-side error |

---

## 🔐 Authentication Flow

1. **Login** - POST `/api/auth/login` with credentials
2. **Receive Tokens** - Get `accessToken` and `refreshToken`
3. **Use Access Token** - Include in `Authorization: Bearer {accessToken}` header
4. **Token Expires** - Use POST `/api/auth/refresh` with `refreshToken` to get new `accessToken`
5. **Logout** - POST `/api/auth/logout` to invalidate tokens

---

## 🧪 Testing with cURL

### Login Example
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@beautycenter.com","password":"Admin@123"}'
```

### Authenticated Request Example
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Create Appointment Example
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "client-uuid",
    "employeeId": "employee-uuid",
    "serviceId": "service-uuid",
    "startAt": "2024-02-15T14:00:00Z",
    "endAt": "2024-02-15T15:00:00Z",
    "notes": "First time client"
  }'
```

---

## 📚 Additional Resources

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (when application is running)
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`
- **Postman Collection:** Import `postman_auth_collection.json` for pre-configured requests

---

## 📝 Notes

- All timestamps use **ISO 8601 format** with timezone (e.g., `2024-02-15T14:00:00Z`)
- All IDs are **UUIDs** (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- **JWT tokens expire** after 1 hour (3600 seconds)
- **Refresh tokens** can be used to obtain new access tokens without re-login
- Some endpoints are **TODO** (not fully implemented yet) - they return placeholder responses

---

**Last Updated:** 2024-02-10
**API Version:** 1.0.0
  "message": "User activated successfully",
  "data": null
}
```

