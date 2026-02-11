## Backend Module Architecture (MVP → Scalable)

This architecture is **modular, role-driven, and boring on purpose**. Boring means stable, auditable, and expandable without therapy.

---

## 1. High-Level Backend Structure

```
API Gateway / Controllers
        │
        ▼
Application Layer (Use Cases)
        │
        ▼
Domain Modules (Business Logic)
        │
        ▼
Infrastructure Layer (DB, Email, SMS)
```

Clean separation. No spaghetti. No "just one quick hack" that lives forever.

---

## 2. Core Modules (MVP)

### 2.1 Auth & Access Control Module

**Purpose:** Who are you and what are you allowed to touch.

**Responsibilities:**

- Authentication (Admin / Employé / Cliente)
- Role-based access control (RBAC)
- Session / token management
- Password reset

**Exposes:**

- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/reset-password`

**Used by:** All modules

---

### 2.2 User Management Module

**Purpose:** Manage humans, unfortunately.

**Entities:**

- User (base)
- Admin
- Employé
- Cliente

**Responsibilities:**

- Create / update users
- Activate / deactivate accounts
- Link users to roles

**Notes:**

- Employé & Cliente extend User
- Admin has full privileges

---

### 2.3 Service Management Module

**Purpose:** Define what the business actually sells.

**Entities:**

- Service

**Responsibilities:**

- CRUD services
- Duration, price, description
- Required specialties
- Active / inactive services

**Used by:** Booking Module

---

### 2.4 Employee Scheduling Module

**Purpose:** Prevent double-booking and burnout.

**Entities:**

- WorkSchedule
- Absence
- Specialty

**Responsibilities:**

- Define work days & hours
- Manage absences / vacations
- Assign specialties

**Critical Rule Engine:**

- Availability calculation

---

### 2.5 Booking (Appointment) Module

**Purpose:** The heart of the system.

**Entities:**

- Appointment

**Responsibilities:**

- Create / modify / cancel appointments
- Validate availability
- Enforce duration rules
- Manage appointment status

**States:**

- Pending
- Confirmed
- Cancelled
- Completed

**Depends on:**

- Services
- Employee Scheduling
- Client Profiles

---

### 2.6 Client Profile (Medical File) Module

**Purpose:** Sensitive data handled like a grown-up system.

**Entities:**

- ClientProfile
- MedicalHistory
- Consent

**Responsibilities:**

- Store declarative client data
- Versioning / audit trail
- Enforce access rules

**Access Rules:**

- Cliente: read/write own declarative data
- Employé/Admin: read

---

### 2.7 Professional Notes Module

**Purpose:** Staff-only clinical intelligence.

**Entities:**

- ProfessionalNote

**Responsibilities:**

- Create notes post-appointment
- Link notes to appointment
- Restrict modification rights

**Access Rules:**

- Employé/Admin: write
- Cliente: read-only (optional)

---

### 2.8 Notification Module

**Purpose:** Reduce no-shows without annoying people.

**Responsibilities:**

- Email notifications
- SMS notifications
- Reminder scheduling
- Consent & unsubscribe handling

**Triggers:**

- Appointment created
- Appointment updated
- Reminder time reached

---

### 2.9 Audit & Security Module

**Purpose:** When something goes wrong, you know who did it.

**Responsibilities:**

- Log sensitive actions
- Track data modifications
- Access logs

**Mandatory for:**

- Medical data
- Appointment changes

---

## 3. Infrastructure Modules

### 3.1 Database Layer

- Relational DB (PostgreSQL / MySQL)
- Encrypted fields for medical data
- Soft deletes where needed

---

### 3.2 External Services Adapters

```
Notification Adapter
  ├── Email Provider
  └── SMS Provider
```

Abstracted. Replaceable. No vendor lock-in tantrums.

---

## 4. Module Dependency Map (Simplified)

```
Auth ─┬─ User Management
      ├─ Booking
      ├─ Client Profile
      └─ Notes

Booking ─┬─ Services
         ├─ Employee Scheduling
         ├─ Client Profile
         └─ Notifications
```

No circular dependencies. If you see one, you fix it.

---

## 5. Phase 2 & 3 Add-On Modules

- Loyalty & VIP Module
- Package / Cure Module
- Waiting List Automation Module
- Payment Module
- Chatbot / FAQ Module
- Inventory Module

Each plugs in **without touching core logic**. That’s the point.

---

## Final Architectural Truth

This backend is:

- legally sane
- medically cautious
- business-aligned
- boring enough to survive real users

Which is exactly what you want.

