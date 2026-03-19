# Beauty Center API - Routes Documentation

**Base URL:** `http://localhost:8050`
**API Version:** 1.0.0
**Authentication:** JWT Bearer Token (except public endpoints)

---

## 📋 Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Employee Management](#employee-management)
4. [Beauty Services](#beauty-services)
5. [Appointments](#appointments)
6. [Scheduling & Availability](#scheduling--availability)
7. [Client Files](#client-files)
8. [Professional Notes](#professional-notes)
9. [Audit](#audit)
10. [Test Users](#test-users)
11. [Response Format](#response-format)

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

## 👔 Employee Management

All employee management endpoints require **ADMIN** role.

### POST `/api/admin/employees`
**Description:** Create new employee account with password
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@beautycenter.com",
  "phone": "+1-555-2000",
  "password": "SecurePass@123",
  "specialtyIds": ["uuid-1", "uuid-2"]
}
```
**Response:**
```json
{
  "id": "employee-uuid",
  "fullName": "Jane Smith",
  "email": "jane.smith@beautycenter.com",
  "phone": "+1-555-2000",
  "isActive": true,
  "role": "EMPLOYEE",
  "specialties": [],
  "createdAt": "2024-02-10T10:00:00Z",
  "updatedAt": "2024-02-10T10:00:00Z"
}
```

### GET `/api/admin/employees`
**Description:** Get all employees with optional filters
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `isActive` (optional): Filter by active status (true/false)

**Example:** `/api/admin/employees?isActive=true`

**Response:**
```json
[
  {
    "id": "employee-uuid",
    "fullName": "Jane Smith",
    "email": "jane.smith@beautycenter.com",
    "phone": "+1-555-2000",
    "isActive": true,
    "role": "EMPLOYEE",
    "specialties": [],
    "createdAt": "2024-02-10T10:00:00Z",
    "updatedAt": "2024-02-10T10:00:00Z"
  }
]
```

### GET `/api/admin/employees/{id}`
**Description:** Get employee by ID
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:** Same as employee object above

### PUT `/api/admin/employees/{id}`
**Description:** Update employee information (activate/deactivate, update details)
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "fullName": "Jane Smith Updated",
  "email": "jane.updated@beautycenter.com",
  "phone": "+1-555-3000",
  "isActive": false,
  "specialtyIds": ["uuid-1"]
}
```
**Response:**
```json
{
  "id": "employee-uuid",
  "fullName": "Jane Smith Updated",
  "email": "jane.updated@beautycenter.com",
  "phone": "+1-555-3000",
  "isActive": false,
  "role": "EMPLOYEE",
  "specialties": [],
  "createdAt": "2024-02-10T10:00:00Z",
  "updatedAt": "2024-02-10T11:30:00Z"
}
```

### GET `/api/admin/employees/{id}/working-times`
**Description:** Get employee's weekly working schedule
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
[
  {
    "id": "slot-uuid-1",
    "employeeId": "employee-uuid",
    "dayOfWeek": "MON",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "createdAt": "2024-02-10T10:00:00Z",
    "updatedAt": "2024-02-10T10:00:00Z"
  },
  {
    "id": "slot-uuid-2",
    "employeeId": "employee-uuid",
    "dayOfWeek": "TUE",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "createdAt": "2024-02-10T10:00:00Z",
    "updatedAt": "2024-02-10T10:00:00Z"
  }
]
```

### PUT `/api/admin/employees/{id}/working-times`
**Description:** Replace employee's full weekly schedule (deletes existing, creates new)
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
[
  {
    "dayOfWeek": "MON",
    "startTime": "09:00:00",
    "endTime": "17:00:00"
  },
  {
    "dayOfWeek": "TUE",
    "startTime": "09:00:00",
    "endTime": "17:00:00"
  },
  {
    "dayOfWeek": "WED",
    "startTime": "10:00:00",
    "endTime": "18:00:00"
  }
]
```
**Validation:**
- `endTime` must be after `startTime`
- No overlapping time slots on the same day
- Valid day of week: MON, TUE, WED, THU, FRI, SAT, SUN

**Response:** Array of created working time slots (same format as GET)

### GET `/api/admin/employees/{id}/absences`
**Description:** Get employee's absences (vacations, time off)
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
[
  {
    "id": "absence-uuid",
    "employeeId": "employee-uuid",
    "startAt": "2024-02-15T00:00:00Z",
    "endAt": "2024-02-20T23:59:59Z",
    "reason": "Vacation",
    "createdAt": "2024-02-10T10:00:00Z",
    "updatedAt": "2024-02-10T10:00:00Z"
  }
]
```

### POST `/api/admin/employees/{id}/absences`
**Description:** Create absence for employee
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "startAt": "2024-02-15T00:00:00Z",
  "endAt": "2024-02-20T23:59:59Z",
  "reason": "Vacation"
}
```
**Validation:**
- `endAt` must be after `startAt`
- Cannot overlap with existing absences
- Cannot create if confirmed appointments exist during period (MVP: blocks creation)

**Response:**
```json
{
  "id": "absence-uuid",
  "employeeId": "employee-uuid",
  "startAt": "2024-02-15T00:00:00Z",
  "endAt": "2024-02-20T23:59:59Z",
  "reason": "Vacation",
  "createdAt": "2024-02-10T10:00:00Z",
  "updatedAt": "2024-02-10T10:00:00Z"
}
```

### DELETE `/api/admin/absences/{absenceId}`
**Description:** Delete employee absence
**Access:** ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:** 204 No Content

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

All appointment endpoints implement role-based access control:
- **CLIENT**: Can view own appointments, create appointments for themselves, cancel own appointments
- **EMPLOYEE**: Can view own calendar, update/reschedule appointments, complete appointments
- **ADMIN**: Full access to all appointments and operations

### GET `/api/appointments`
**Description:** Get appointments with filters (role-based filtering applied automatically)
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `clientId` (optional): Filter by client UUID (ADMIN only, ignored for CLIENT/EMPLOYEE)
- `employeeId` (optional): Filter by employee UUID (ADMIN only, auto-set for EMPLOYEE)
- `status` (optional): CONFIRMED, CANCELED, COMPLETED
- `from` (optional): Start date/time (ISO 8601 with timezone)
- `to` (optional): End date/time (ISO 8601 with timezone)

**Example:** `/api/appointments?status=CONFIRMED&from=2024-02-15T00:00:00Z&to=2024-02-20T23:59:59Z`

**Response:**
```json
{
  "success": true,
  "message": "Appointments retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "clientId": "client-uuid",
      "employeeId": "employee-uuid",
      "serviceId": "service-uuid",
      "startAt": "2024-02-15T14:00:00+01:00",
      "endAt": "2024-02-15T15:00:00+01:00",
      "status": "CONFIRMED",
      "cancellationReason": null,
      "createdAt": "2024-02-10T10:00:00+01:00",
      "updatedAt": "2024-02-10T10:00:00+01:00"
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
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "client-uuid",
    "employeeId": "employee-uuid",
    "serviceId": "service-uuid",
    "startAt": "2024-02-15T14:00:00+01:00",
    "endAt": "2024-02-15T15:00:00+01:00",
    "status": "CONFIRMED",
    "cancellationReason": null,
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-10T10:00:00+01:00"
  }
}
```

### POST `/api/appointments`
**Description:** Create new appointment with availability validation and conflict detection
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "clientId": "client-uuid",
  "serviceId": "service-uuid",
  "employeeId": "employee-uuid",
  "startAt": "2024-02-15T14:00:00+01:00",
  "notes": "First time client"
}
```
**Notes:**
- `clientId` is optional. If not provided, uses current user's ID (for CLIENT role)
- Only ADMIN can specify a different `clientId` to book for another client
- `endAt` is calculated automatically based on service duration
- Returns 409 Conflict if slot is not available or overlaps with existing appointment

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Appointment created successfully",
  "data": {
    "id": "new-appointment-uuid",
    "clientId": "client-uuid",
    "employeeId": "employee-uuid",
    "serviceId": "service-uuid",
    "startAt": "2024-02-15T14:00:00+01:00",
    "endAt": "2024-02-15T15:00:00+01:00",
    "status": "CONFIRMED",
    "cancellationReason": null,
    "createdAt": "2024-02-15T13:00:00+01:00",
    "updatedAt": "2024-02-15T13:00:00+01:00"
  }
}
```

**Conflict Response (409 Conflict):**
```json
{
  "success": false,
  "message": "Appointment slot is not available or conflicts with existing appointment",
  "errorCode": 409,
  "timestamp": "2024-02-15T13:00:00+01:00"
}
```

### PUT `/api/appointments/{id}`
**Description:** Update/reschedule existing appointment
**Access:** ADMIN or EMPLOYEE only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "employeeId": "employee-uuid",
  "serviceId": "service-uuid",
  "startAt": "2024-02-16T10:00:00+01:00"
}
```
**Notes:**
- Cannot update COMPLETED or CANCELED appointments
- Returns 409 Conflict if new slot is not available
- `endAt` is recalculated based on service duration

**Response:**
```json
{
  "success": true,
  "message": "Appointment rescheduled successfully",
  "data": {
    "id": "appointment-uuid",
    "clientId": "client-uuid",
    "employeeId": "employee-uuid",
    "serviceId": "service-uuid",
    "startAt": "2024-02-16T10:00:00+01:00",
    "endAt": "2024-02-16T11:00:00+01:00",
    "status": "CONFIRMED",
    "cancellationReason": null,
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-15T14:30:00+01:00"
  }
}
```

### POST `/api/appointments/{id}/cancel`
**Description:** Cancel appointment with reason
**Access:** Authenticated users (own appointments or ADMIN/EMPLOYEE)
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "cancellationReason": "Client requested reschedule"
}
```
**Notes:**
- CLIENT can cancel own appointments
- EMPLOYEE and ADMIN can cancel any appointment
- Cannot cancel already COMPLETED or CANCELED appointments

**Response:**
```json
{
  "success": true,
  "message": "Appointment canceled successfully",
  "data": null
}
```

### POST `/api/appointments/{id}/complete`
**Description:** Mark appointment as completed
**Access:** ADMIN or EMPLOYEE only
**Headers:** `Authorization: Bearer {accessToken}`
**Notes:**
- Can only complete CONFIRMED appointments
- Cannot complete CANCELED appointments

**Response:**
```json
{
  "success": true,
  "message": "Appointment marked as completed",
  "data": null
}
```

---

## 🗓️ Scheduling & Availability

### GET `/api/availability`
**Description:** Get available time slots for an employee and service
**Access:** Authenticated users
**Headers:** `Authorization: Bearer {accessToken}`
**Query Parameters:**
- `employeeId` (required): UUID of the employee
- `serviceId` (required): UUID of the service
- `date` (required): Start date in ISO format (YYYY-MM-DD)
- `days` (optional): Number of days to check (default: 1, min: 1, max: 30)

**Example:** `/api/availability?employeeId=550e8400-e29b-41d4-a716-446655440000&serviceId=660e8400-e29b-41d4-a716-446655440001&date=2024-02-15&days=7`

**Algorithm:**
- Calculates available slots based on employee's weekly working schedule
- Excludes time slots during employee absences
- Excludes time slots overlapping with existing CONFIRMED appointments
- Ignores CANCELED appointments (they don't block availability)
- Uses 15-minute granularity for slot generation
- Validates that service is active and exists
- Returns slots within working hours only

**Response:**
```json
{
  "success": true,
  "message": "Availability retrieved successfully",
  "data": {
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "serviceId": "660e8400-e29b-41d4-a716-446655440001",
    "startDate": "2024-02-15",
    "endDate": "2024-02-21",
    "serviceDurationMinutes": 60,
    "availableSlots": [
      {
        "startAt": "2024-02-15T09:00:00+01:00",
        "endAt": "2024-02-15T10:00:00+01:00"
      },
      {
        "startAt": "2024-02-15T09:15:00+01:00",
        "endAt": "2024-02-15T10:15:00+01:00"
      },
      {
        "startAt": "2024-02-15T10:00:00+01:00",
        "endAt": "2024-02-15T11:00:00+01:00"
      },
      {
        "startAt": "2024-02-16T09:00:00+01:00",
        "endAt": "2024-02-16T10:00:00+01:00"
      }
    ]
  }
}
```

**Error Responses:**

Service not found (400 Bad Request):
```json
{
  "success": false,
  "message": "Service not found: 660e8400-e29b-41d4-a716-446655440001",
  "errorCode": 400,
  "timestamp": "2024-02-15T10:00:00+01:00"
}
```

Service inactive (400 Bad Request):
```json
{
  "success": false,
  "message": "Service is not active: 660e8400-e29b-41d4-a716-446655440001",
  "errorCode": 400,
  "timestamp": "2024-02-15T10:00:00+01:00"
}
```

Invalid days parameter (400 Bad Request):
```json
{
  "success": false,
  "message": "Days parameter must be between 1 and 30",
  "errorCode": 400,
  "timestamp": "2024-02-15T10:00:00+01:00"
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

## 📋 Client Files (Medical Dossier)

**IMPORTANT:** Client files contain sensitive medical and personal information. All operations are logged to the audit trail for compliance.

### GET `/api/client/me/file`
**Description:** Get current client's own medical file (creates if doesn't exist)
**Access:** CLIENT only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Client file retrieved successfully",
  "data": {
    "id": "file-uuid",
    "clientId": "client-uuid",
    "intake": {
      "howDidYouHearAboutUs": "Google",
      "consultationReason": "Acne treatment",
      "objective": "Clear skin",
      "careType": "Facial",
      "skincareRoutine": "Morning cleanser, night moisturizer",
      "habits": "Drinks 8 glasses of water daily"
    },
    "medicalHistory": {
      "medicalBackground": "Healthy, no chronic conditions",
      "currentTreatments": "None",
      "allergiesAndReactions": "Penicillin, latex"
    },
    "aestheticProcedureHistory": {
      "procedures": "Chemical peel (2023), microdermabrasion (2022)"
    },
    "photoConsentForFollowup": true,
    "photoConsentForMarketing": false,
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-11T14:30:00+01:00"
  }
}
```

### PUT `/api/client/me/file`
**Description:** Update current client's own medical file (client-provided data only)
**Access:** CLIENT only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "intake": {
    "howDidYouHearAboutUs": "Instagram",
    "consultationReason": "Anti-aging treatment",
    "objective": "Reduce fine lines",
    "careType": "Botox",
    "skincareRoutine": "Retinol serum, SPF 50",
    "habits": "Exercises 3x/week, no smoking"
  },
  "medicalHistory": {
    "medicalBackground": "Hypertension (controlled)",
    "currentTreatments": "Lisinopril 10mg daily",
    "allergiesAndReactions": "Penicillin, aspirin"
  },
  "aestheticProcedureHistory": {
    "procedures": "Botox forehead (2024), filler lips (2023)"
  },
  "photoConsentForFollowup": true,
  "photoConsentForMarketing": false
}
```
**Notes:**
- All fields are optional (partial updates supported)
- Only the client who owns the file can update it
- Employees/Admins cannot modify client-provided data
- All updates are logged to audit trail

**Response:**
```json
{
  "success": true,
  "message": "Client file updated successfully",
  "data": {
    "id": "file-uuid",
    "clientId": "client-uuid",
    "intake": { ... },
    "medicalHistory": { ... },
    "aestheticProcedureHistory": { ... },
    "photoConsentForFollowup": true,
    "photoConsentForMarketing": false,
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-11T15:00:00+01:00"
  }
}
```

### GET `/api/clients/{clientId}/file`
**Description:** Get client file by ID (Employee/Admin read-only access)
**Access:** EMPLOYEE or ADMIN only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Client file retrieved successfully",
  "data": {
    "id": "file-uuid",
    "clientId": "client-uuid",
    "intake": { ... },
    "medicalHistory": { ... },
    "aestheticProcedureHistory": { ... },
    "photoConsentForFollowup": true,
    "photoConsentForMarketing": false,
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-11T14:30:00+01:00"
  }
}
```
**Notes:**
- Employees and admins can read any client's file
- Access is logged to audit trail for compliance
- Returns 404 if client file doesn't exist

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Client file not found for client: {clientId}",
  "errorCode": 404,
  "timestamp": "2024-02-11T15:00:00+01:00"
}
```

---

## 🔔 Client Consent (SMS Notifications)

### GET `/api/client/me/consent`
**Description:** Get current client's SMS notification consent preferences
**Access:** CLIENT only
**Headers:** `Authorization: Bearer {accessToken}`
**Response:**
```json
{
  "success": true,
  "message": "Consent preferences retrieved successfully",
  "data": {
    "id": "consent-uuid",
    "clientId": "client-uuid",
    "smsOptIn": true,
    "smsUnsubToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-10T10:00:00+01:00"
  }
}
```
**Notes:**
- Consent is created automatically on first access with default opt-in
- Unsubscribe token is auto-generated (UUID format)
- Token persists across consent updates

### PUT `/api/client/me/consent`
**Description:** Update current client's SMS notification consent preferences
**Access:** CLIENT only
**Headers:** `Authorization: Bearer {accessToken}`
**Request Body:**
```json
{
  "smsOptIn": false
}
```
**Response:**
```json
{
  "success": true,
  "message": "Consent preferences updated successfully",
  "data": {
    "id": "consent-uuid",
    "clientId": "client-uuid",
    "smsOptIn": false,
    "smsUnsubToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "createdAt": "2024-02-10T10:00:00+01:00",
    "updatedAt": "2024-02-11T15:30:00+01:00"
  }
}
```
**Notes:**
- Only `smsOptIn` field can be updated
- Unsubscribe token remains unchanged
- All updates are logged to audit trail

### POST `/api/consent/unsubscribe/{token}`
**Description:** Public unsubscribe endpoint using SMS unsubscribe token
**Access:** Public (no authentication required)
**Example:** `/api/consent/unsubscribe/a1b2c3d4-e5f6-7890-abcd-ef1234567890`
**Response:**
```json
{
  "success": true,
  "message": "Successfully unsubscribed from SMS notifications",
  "data": null
}
```
**Notes:**
- No authentication required (public endpoint)
- Sets `smsOptIn` to `false` for the client associated with the token
- Operation is logged to audit trail (no actor since public)
- Typically used in SMS messages: "Reply STOP or visit: https://beautycenter.com/api/consent/unsubscribe/{token}"

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Invalid or expired unsubscribe token",
  "errorCode": 404,
  "timestamp": "2024-02-11T15:30:00+01:00"
}
```

---

## 🔍 Audit Trail

All sensitive operations on client files and consent are logged to an immutable audit trail for compliance (GDPR/HIPAA).

**Audit Entry Structure:**
- `entityType`: "ClientFile" or "ClientConsent"
- `entityId`: UUID of the entity
- `action`: "CREATE", "UPDATE", "DELETE", or "READ"
- `actorId`: UUID of the user who performed the action
- `at`: Timestamp of the action
- `beforeJson`: JSON snapshot before the change (for UPDATE/DELETE)
- `afterJson`: JSON snapshot after the change (for CREATE/UPDATE)

**Logged Operations:**
- Client file: CREATE, UPDATE, READ
- Client consent: CREATE, UPDATE
- Public unsubscribe: UPDATE (no actor)

---

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
| **200** | OK | Successful GET, PUT, PATCH, POST (non-creation) |
| **201** | Created | Successful POST (resource created) |
| **400** | Bad Request | Invalid request body or parameters |
| **401** | Unauthorized | Missing or invalid authentication token |
| **403** | Forbidden | Authenticated but insufficient permissions |
| **404** | Not Found | Resource doesn't exist |
| **409** | Conflict | Appointment booking conflict, overlapping time slots |
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
curl -X POST http://localhost:8050/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@beautycenter.com","password":"Admin@123"}'
```

### Authenticated Request Example
```bash
curl -X GET http://localhost:8050/api/users/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Check Availability Example
```bash
curl -X GET "http://localhost:8050/api/availability?employeeId=550e8400-e29b-41d4-a716-446655440000&serviceId=660e8400-e29b-41d4-a716-446655440001&date=2024-02-15&days=7" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### Create Appointment Example
```bash
curl -X POST http://localhost:8050/api/appointments \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "660e8400-e29b-41d4-a716-446655440001",
    "employeeId": "550e8400-e29b-41d4-a716-446655440000",
    "startAt": "2024-02-15T14:00:00+01:00",
    "notes": "First time client"
  }'
```

### Cancel Appointment Example
```bash
curl -X POST http://localhost:8050/api/appointments/appointment-uuid/cancel \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "cancellationReason": "Client requested reschedule"
  }'
```

---

## 📚 Additional Resources

- **Swagger UI:** `http://localhost:8050/swagger-ui.html` (when application is running)
- **OpenAPI Spec:** `http://localhost:8050/v3/api-docs`
- **Postman Collection:** Import `postman_auth_collection.json` for pre-configured requests

---

## 📝 Notes

- All timestamps use **ISO 8601 format** with timezone (e.g., `2024-02-15T14:00:00+01:00` for Tunisia UTC+1)
- All IDs are **UUIDs** (e.g., `550e8400-e29b-41d4-a716-446655440000`)
- **JWT tokens expire** after 1 hour (3600 seconds)
- **Refresh tokens** can be used to obtain new access tokens without re-login
- **Appointment slots** use 15-minute granularity for availability calculation
- **Timezone**: System uses Tunisia timezone (UTC+1) with OffsetDateTime
- **Conflict handling**: Database-level GiST exclusion constraints prevent overlapping appointments
- **Status transitions**: CONFIRMED → COMPLETED or CANCELED (cannot edit completed appointments)
- **Canceled appointments** do not block availability slots

---

**Last Updated:** 2024-02-10
**API Version:** 1.0.0
  "message": "User activated successfully",
  "data": null
}
```

