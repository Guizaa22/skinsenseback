# Beauty Center API – Endpoints for Frontend

**Base URL:** `http://localhost:8050`  
**All API paths are prefixed with:** `/api`

**Standard response wrapper:** All endpoints return JSON in this shape (unless noted):

```json
{
  "success": true,
  "message": "Success message",
  "data": { ... }
}
```

Errors:

```json
{
  "success": false,
  "message": "Error description"
}
```

**Authentication:** Send JWT in header: `Authorization: Bearer <accessToken>`

---

## 1. Auth – `/api/auth`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| POST | `/api/auth/register` | No | `{ "fullName", "email", "phone?", "password" }` | `LoginResponse` |
| POST | `/api/auth/login` | No | `{ "email", "password" }` | `LoginResponse` |
| GET | `/api/auth/me` | Yes | — | `UserPrincipalDto` |
| POST | `/api/auth/refresh` | No | `{ "refreshToken" }` | `LoginResponse` |
| POST | `/api/auth/logout` | Yes | — | `null` |

**LoginResponse:** `{ "accessToken", "refreshToken", "tokenType": "Bearer", "expiresIn", "newUser" }` — `newUser` is `true` only after registration (first signup); use it to show a welcome message on the dashboard.
**UserPrincipalDto:** `{ "id", "fullName", "email", "phone", "role", "active", "createdAt" }` — `createdAt` is the account creation time (ISO-8601); use it for “Welcome, you joined on …” or a first-time banner.

---

## 2. Users – `/api/users`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|--------------|-----------------|
| GET | `/api/users/profile` | Yes | — | `UserResponse` |
| PUT | `/api/users/profile` | Yes | `{ "fullName?", "phone?" }` | `UserResponse` |
| GET | `/api/users/{id}` | Yes | — | `UserResponse` |
| GET | `/api/users` | ADMIN | — | `List<UserResponse>` |
| GET | `/api/users/employees` | ADMIN | — | `List<UserResponse>` |
| GET | `/api/users/clients` | ADMIN | — | `List<UserResponse>` |
| POST | `/api/users` | ADMIN | `{ "fullName", "email", "phone?", "password", "role" }` | `UserResponse` |
| PUT | `/api/users/{id}` | ADMIN | `{ "fullName?", "phone?" }` | `UserResponse` |
| POST | `/api/users/{id}/deactivate` | ADMIN | — | `null` |
| POST | `/api/users/{id}/activate` | ADMIN | — | `null` |

**UserResponse:** `{ "id", "fullName", "email", "phone", "role", "isActive", "createdAt", "updatedAt" }`

---

## 3. Appointments – `/api/appointments`

| Method | Path | Auth | Request body / Params | Response `data` |
|--------|------|------|----------------------|-----------------|
| GET | `/api/appointments` | Yes | Query: `clientId?`, `employeeId?`, `status?`, `from?`, `to?` | `List<AppointmentResponse>` |
| GET | `/api/appointments/{id}` | Yes | — | `AppointmentResponse` |
| POST | `/api/appointments` | CLIENT/ADMIN | `{ "clientId?", "employeeId", "serviceId", "startAt", "notes?" }` | `AppointmentResponse` |
| PUT | `/api/appointments/{id}` | ADMIN/EMPLOYEE | `{ "employeeId", "serviceId", "startAt", "notes?" }` | `AppointmentResponse` |
| POST | `/api/appointments/{id}/cancel` | Yes | `{ "cancellationReason" }` | `null` |
| POST | `/api/appointments/{id}/complete` | ADMIN/EMPLOYEE | — | `null` |

**AppointmentResponse:** `{ "id", "clientId", "employeeId", "serviceId", "startAt", "endAt", "status", "cancellationReason", "createdAt", "updatedAt" }`

---

## 4. Appointment notes – `/api/appointments/{appointmentId}/notes`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| GET | `/api/appointments/{appointmentId}/notes` | Yes | — | `List<ProfessionalNoteResponse>` |
| POST | `/api/appointments/{appointmentId}/notes` | EMPLOYEE/ADMIN | `{ "diagnostic?", "phototype?", "carePerformed", "productsAndParameters?", "reactions?", "recommendations?", "nextAppointmentSuggestion?" }` | `ProfessionalNoteResponse` |
| PUT | `/api/appointments/{appointmentId}/notes/{noteId}` | EMPLOYEE/ADMIN | Same fields as create (all optional) | `ProfessionalNoteResponse` |

**ProfessionalNoteResponse:** `{ "id", "appointmentId", "employeeId", "diagnostic", "phototype", "carePerformed", "productsAndParameters", "reactions", "recommendations", "nextAppointmentSuggestion", "createdAt", "updatedAt" }`

---

## 5. Availability – `/api/availability`

| Method | Path | Auth | Params | Response `data` |
|--------|------|------|--------|-----------------|
| GET | `/api/availability` | Yes | `employeeId`, `serviceId`, `date` (YYYY-MM-DD), `days?` (1–30, default 1) | `AvailabilityResponse` |

**AvailabilityResponse:** `{ "employeeId", "serviceId", "startDate", "endDate", "serviceDurationMinutes", "availableSlots": [ { "startAt", "endAt" }, ... ] }`

---

## 6. Services – `/api/services`

| Method | Path | Auth | Request body / Params | Response `data` |
|--------|------|------|----------------------|-----------------|
| GET | `/api/services` | No* | Query: `active?` (boolean) | `List<BeautyServiceResponse>` |
| GET | `/api/services/{id}` | No* | — | `BeautyServiceResponse` |
| POST | `/api/services` | ADMIN | `{ "name", "description?", "durationMinutes", "price", "specialtyId?", "allowedEmployeeIds?", "isActive?" }` | `BeautyServiceResponse` |
| PUT | `/api/services/{id}` | ADMIN | Same as create | `BeautyServiceResponse` |
| DELETE | `/api/services/{id}` | ADMIN | — | `null` |

**BeautyServiceResponse:** `{ "id", "name", "description", "durationMinutes", "price", "isActive", "specialtyId", "specialtyName", "allowedEmployeeIds", "createdAt", "updatedAt" }`

*GET services are public in current security config.

---

## 7. Specialties – `/api/specialties`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| GET | `/api/specialties` | Yes | — | `List<SpecialtyResponse>` |
| GET | `/api/specialties/{id}` | Yes | — | `SpecialtyResponse` |
| POST | `/api/specialties` | ADMIN | `{ "name", "description?" }` | `SpecialtyResponse` |
| PUT | `/api/specialties/{id}` | ADMIN | `{ "name", "description?" }` | `SpecialtyResponse` |
| DELETE | `/api/specialties/{id}` | ADMIN | — | `null` |

**SpecialtyResponse:** `{ "id", "name", "description", "createdAt", "updatedAt" }`

---

## 8. Employee specialties & services – `/api/employees`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| GET | `/api/employees/{employeeId}/specialties` | Yes | — | `List<SpecialtyResponse>` |
| POST | `/api/employees/{employeeId}/specialties` | ADMIN | `{ "specialtyId" }` | `null` |
| DELETE | `/api/employees/{employeeId}/specialties/{specialtyId}` | ADMIN | — | `null` |
| GET | `/api/employees/{employeeId}/services` | Yes | — | `List<BeautyServiceResponse>` |

---

## 9. Admin – employees, working times, absences – `/api/admin`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| POST | `/api/admin/employees` | ADMIN | `{ "fullName", "email", "phone?", "password", "specialtyIds?" }` | `EmployeeResponse` |
| GET | `/api/admin/employees` | ADMIN | Query: `isActive?` | `List<EmployeeResponse>` |
| GET | `/api/admin/employees/{id}` | ADMIN | — | `EmployeeResponse` |
| PUT | `/api/admin/employees/{id}` | ADMIN | `{ "fullName?", "email?", "phone?", "isActive?", "specialtyIds?" }` | `EmployeeResponse` |
| GET | `/api/admin/employees/{id}/working-times` | ADMIN | — | `List<WorkingTimeSlotResponse>` |
| PUT | `/api/admin/employees/{id}/working-times` | ADMIN | `[ { "dayOfWeek", "startTime", "endTime" }, ... ]` | `List<WorkingTimeSlotResponse>` |
| GET | `/api/admin/employees/{id}/absences` | ADMIN | — | `List<AbsenceResponse>` |
| POST | `/api/admin/employees/{id}/absences` | ADMIN | `{ "startAt", "endAt", "reason?" }` | `AbsenceResponse` |
| DELETE | `/api/admin/absences/{absenceId}` | ADMIN | — | `null` |

**EmployeeResponse:** `{ "id", "fullName", "email", "phone", "isActive", "role", "specialties": [ { "id", "name", "description" } ], "createdAt", "updatedAt" }`  
**WorkingTimeSlotResponse:** `{ "id", "employeeId", "dayOfWeek", "startTime", "endTime", ... }`  
**AbsenceResponse:** `{ "id", "employeeId", "startAt", "endAt", "reason", ... }`  
**WorkingTimeSlotRequest dayOfWeek:** one of `MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`

---

## 10. Admin – audit – `/api/admin/audit`

| Method | Path | Auth | Params | Response `data` |
|--------|------|------|--------|-----------------|
| GET | `/api/admin/audit` | ADMIN | `entityType?`, `entityId?`, `action?` | `List<AuditEntry>` |
| GET | `/api/admin/audit/{id}` | ADMIN | — | `AuditEntry` |

**AuditEntry:** `{ "id", "entityType", "entityId", "action", "actorId", "at", "beforeJson", "afterJson", "ipAddress", "createdAt" }`

---

## 11. Admin – notification rules – `/api/admin/notification-rules`

| Method | Path | Auth | Request body / Params | Response `data` |
|--------|------|------|------------------------|-----------------|
| GET | `/api/admin/notification-rules` | ADMIN | — | `List<NotificationRuleResponse>` |
| GET | `/api/admin/notification-rules/{id}` | ADMIN | — | `NotificationRuleResponse` |
| POST | `/api/admin/notification-rules` | ADMIN | `{ "beautyServiceId?", "type", "channel", "offsetHours?", "enabled?" }` | `NotificationRuleResponse` |
| PUT | `/api/admin/notification-rules/{id}` | ADMIN | `{ "enabled?", "offsetHours?" }` | `NotificationRuleResponse` |
| GET | `/api/admin/notification-rules/messages` | ADMIN | Query: `appointmentId?`, `status?` | `List<NotificationMessageResponse>` |

**type:** `BOOKING_CONFIRMATION` \| `REMINDER_24H` \| `REMINDER_2H`  
**channel:** `EMAIL` \| `SMS`  
**NotificationRuleResponse:** `{ "id", "beautyServiceId", "type", "channel", "offsetHours", "enabled", "createdAt", "updatedAt" }`

---

## 12. Client – my file – `/api/client/me/file`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| GET | `/api/client/me/file` | CLIENT | — | `ClientFileResponse` |
| PUT | `/api/client/me/file` | CLIENT | `{ "intake?", "medicalHistory?", "aestheticProcedureHistory?", "photoConsentForFollowup?", "photoConsentForMarketing?" }` | `ClientFileResponse` |

**ClientFileResponse:** `{ "id", "clientId", "intake", "medicalHistory", "aestheticProcedureHistory", "photoConsentForFollowup", "photoConsentForMarketing", "createdAt", "updatedAt" }`

---

## 13. Client – my consent – `/api/client/me/consent`

| Method | Path | Auth | Request body | Response `data` |
|--------|------|------|----------------|-----------------|
| GET | `/api/client/me/consent` | CLIENT | — | `ClientConsentResponse` |
| PUT | `/api/client/me/consent` | CLIENT | `{ "smsOptIn" }` | `ClientConsentResponse` |

**ClientConsentResponse:** `{ "id", "clientId", "smsOptIn", "smsUnsubToken", "createdAt", "updatedAt" }`

---

## 14. Client file by ID (staff) – `/api/clients/{clientId}/file`

| Method | Path | Auth | Response `data` |
|--------|------|------|-----------------|
| GET | `/api/clients/{clientId}/file` | EMPLOYEE/ADMIN | `ClientFileResponse` |

---

## 15. Public – consent unsubscribe – `/api/consent/unsubscribe`

| Method | Path | Auth | Response `data` |
|--------|------|------|-----------------|
| POST | `/api/consent/unsubscribe/{token}` | No | `null` |

---

## Quick reference table (all endpoints)

| Method | Path |
|--------|------|
| POST | `/api/auth/register` |
| POST | `/api/auth/login` |
| GET | `/api/auth/me` |
| POST | `/api/auth/refresh` |
| POST | `/api/auth/logout` |
| GET | `/api/users/profile` |
| PUT | `/api/users/profile` |
| GET | `/api/users/{id}` |
| GET | `/api/users` |
| GET | `/api/users/employees` |
| GET | `/api/users/clients` |
| POST | `/api/users` |
| PUT | `/api/users/{id}` |
| POST | `/api/users/{id}/deactivate` |
| POST | `/api/users/{id}/activate` |
| GET | `/api/appointments` |
| GET | `/api/appointments/{id}` |
| POST | `/api/appointments` |
| PUT | `/api/appointments/{id}` |
| POST | `/api/appointments/{id}/cancel` |
| POST | `/api/appointments/{id}/complete` |
| GET | `/api/appointments/{appointmentId}/notes` |
| POST | `/api/appointments/{appointmentId}/notes` |
| PUT | `/api/appointments/{appointmentId}/notes/{noteId}` |
| GET | `/api/availability` |
| GET | `/api/services` |
| GET | `/api/services/{id}` |
| POST | `/api/services` |
| PUT | `/api/services/{id}` |
| DELETE | `/api/services/{id}` |
| GET | `/api/specialties` |
| GET | `/api/specialties/{id}` |
| POST | `/api/specialties` |
| PUT | `/api/specialties/{id}` |
| DELETE | `/api/specialties/{id}` |
| GET | `/api/employees/{employeeId}/specialties` |
| POST | `/api/employees/{employeeId}/specialties` |
| DELETE | `/api/employees/{employeeId}/specialties/{specialtyId}` |
| GET | `/api/employees/{employeeId}/services` |
| POST | `/api/admin/employees` |
| GET | `/api/admin/employees` |
| GET | `/api/admin/employees/{id}` |
| PUT | `/api/admin/employees/{id}` |
| GET | `/api/admin/employees/{id}/working-times` |
| PUT | `/api/admin/employees/{id}/working-times` |
| GET | `/api/admin/employees/{id}/absences` |
| POST | `/api/admin/employees/{id}/absences` |
| DELETE | `/api/admin/absences/{absenceId}` |
| GET | `/api/admin/audit` |
| GET | `/api/admin/audit/{id}` |
| GET | `/api/admin/notification-rules` |
| GET | `/api/admin/notification-rules/{id}` |
| POST | `/api/admin/notification-rules` |
| PUT | `/api/admin/notification-rules/{id}` |
| GET | `/api/admin/notification-rules/messages` |
| GET | `/api/client/me/file` |
| PUT | `/api/client/me/file` |
| GET | `/api/client/me/consent` |
| PUT | `/api/client/me/consent` |
| GET | `/api/clients/{clientId}/file` |
| POST | `/api/consent/unsubscribe/{token}` |

---

**Port:** 8050 (configurable via `SERVER_PORT`).  
**CORS:** Allowed origin for API: `http://localhost:3000` (configurable via `app.cors.origins`).
