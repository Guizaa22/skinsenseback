-- V2__create_complete_schema.sql
-- Complete schema creation for Beauty Center application
-- Creates all 14 tables with proper constraints and indexes

-- Enable btree_gist extension for exclusion constraints
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Helper function for tstzrange with immutable marker (required for exclusion constraint)
CREATE OR REPLACE FUNCTION tstzrange_immutable(timestamptz, timestamptz, text)
RETURNS tstzrange AS $$
  SELECT tstzrange($1, $2, $3);
$$ LANGUAGE SQL IMMUTABLE;

-- ============================================================================
-- 1. USER_ACCOUNT (Polymorphic: Client, Employee, Admin)
-- ============================================================================
CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    user_type VARCHAR(50) NOT NULL,  -- Discriminator column for SINGLE_TABLE inheritance
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    role VARCHAR(50) NOT NULL,  -- ADMIN, EMPLOYEE, CLIENT
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_user_account_email ON user_account(email);
CREATE INDEX idx_user_account_role ON user_account(role);
CREATE INDEX idx_user_account_user_type ON user_account(user_type);

-- ============================================================================
-- 2. SPECIALTY (Areas of expertise)
-- ============================================================================
CREATE TABLE specialty (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_specialty_name ON specialty(name);

-- ============================================================================
-- 3. EMPLOYEE_SPECIALTY (Join table: Employee M-to-M Specialty)
-- ============================================================================
CREATE TABLE employee_specialty (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES specialty(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(employee_id, specialty_id)
);

CREATE INDEX idx_employee_specialty_employee ON employee_specialty(employee_id);
CREATE INDEX idx_employee_specialty_specialty ON employee_specialty(specialty_id);

-- ============================================================================
-- 4. BEAUTY_SERVICE (Service catalog)
-- ============================================================================
CREATE TABLE beauty_service (
    id UUID PRIMARY KEY,
    specialty_id UUID REFERENCES specialty(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_min INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_beauty_service_specialty ON beauty_service(specialty_id);
CREATE INDEX idx_beauty_service_active ON beauty_service(is_active);

-- ============================================================================
-- 5. BEAUTY_SERVICE_EMPLOYEE (Join table: Service M-to-M Employee)
-- ============================================================================
CREATE TABLE beauty_service_employee (
    id UUID PRIMARY KEY,
    beauty_service_id UUID NOT NULL REFERENCES beauty_service(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(beauty_service_id, employee_id)
);

CREATE INDEX idx_beauty_service_employee_service ON beauty_service_employee(beauty_service_id);
CREATE INDEX idx_beauty_service_employee_employee ON beauty_service_employee(employee_id);

-- ============================================================================
-- 6. WORKING_TIME_SLOT (Employee weekly schedule)
-- ============================================================================
CREATE TABLE working_time_slot (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,  -- MON, TUE, WED, THU, FRI, SAT, SUN
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_working_time_slot_employee ON working_time_slot(employee_id);
CREATE INDEX idx_working_time_slot_day ON working_time_slot(day_of_week);

-- ============================================================================
-- 7. ABSENCE (Employee time off/vacation)
-- ============================================================================
CREATE TABLE absence (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_absence_employee ON absence(employee_id);
CREATE INDEX idx_absence_dates ON absence(start_at, end_at);

-- ============================================================================
-- 8. APPOINTMENT (Bookings with exclusion constraint)
-- ============================================================================
CREATE TABLE appointment (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    beauty_service_id UUID REFERENCES beauty_service(id) ON DELETE SET NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',  -- CONFIRMED, CANCELED, COMPLETED, NO_SHOW
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_appointment_client ON appointment(client_id);
CREATE INDEX idx_appointment_employee ON appointment(employee_id);
CREATE INDEX idx_appointment_service ON appointment(beauty_service_id);
CREATE INDEX idx_appointment_dates ON appointment(start_at, end_at);
CREATE INDEX idx_appointment_status ON appointment(status);

-- Exclusion constraint: prevent overlapping appointments for same employee
-- Uses GiST index with btree_gist extension for efficient overlap detection
-- Note: '[)' means start inclusive, end exclusive to avoid boundary overlaps
ALTER TABLE appointment ADD CONSTRAINT appointment_no_overlap
    EXCLUDE USING gist (
        employee_id WITH =,
        tstzrange_immutable(start_at, end_at, '[)') WITH &&
    ) WHERE (status != 'CANCELED');

-- ============================================================================
-- 9. CLIENT_CONSENT (Client notification preferences)
-- ============================================================================
CREATE TABLE client_consent (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL UNIQUE REFERENCES user_account(id) ON DELETE CASCADE,
    sms_opt_in BOOLEAN NOT NULL DEFAULT true,
    sms_unsub_token VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_client_consent_client ON client_consent(client_id);

-- ============================================================================
-- 10. CLIENT_FILE (Client medical/personal data)
-- ============================================================================
CREATE TABLE client_file (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL UNIQUE REFERENCES user_account(id) ON DELETE CASCADE,
    -- Intake Section
    how_did_you_hear_about_us VARCHAR(255),
    consultation_reason TEXT,
    objective TEXT,
    care_type VARCHAR(255),
    skincare_routine TEXT,
    habits TEXT,
    -- Medical History Section
    medical_background TEXT,
    current_treatments TEXT,
    allergies_and_reactions TEXT,
    -- Aesthetic Procedure History Section
    procedures TEXT,
    -- Consent Section
    photo_consent_for_followup BOOLEAN DEFAULT false,
    photo_consent_for_marketing BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_client_file_client ON client_file(client_id);

-- ============================================================================
-- 11. PROFESSIONAL_NOTE (Post-appointment staff documentation)
-- ============================================================================
CREATE TABLE professional_note (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointment(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    diagnostic TEXT,
    phototype VARCHAR(50),
    care_performed TEXT,
    products_and_parameters TEXT,
    reactions TEXT,
    recommendations TEXT,
    next_appointment_suggestion VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_professional_note_appointment ON professional_note(appointment_id);
CREATE INDEX idx_professional_note_employee ON professional_note(employee_id);

-- ============================================================================
-- 12. NOTIFICATION_RULE (Notification templates)
-- ============================================================================
CREATE TABLE notification_rule (
    id UUID PRIMARY KEY,
    beauty_service_id UUID REFERENCES beauty_service(id) ON DELETE CASCADE,
    type VARCHAR(100) NOT NULL,  -- BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H
    channel VARCHAR(50) NOT NULL,  -- EMAIL, SMS
    offset_hours INTEGER,  -- for reminders: -24, -2, etc.
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_notification_rule_service ON notification_rule(beauty_service_id);
CREATE INDEX idx_notification_rule_type ON notification_rule(type);
CREATE INDEX idx_notification_rule_enabled ON notification_rule(is_enabled);

-- ============================================================================
-- 13. NOTIFICATION_MESSAGE (Notification tracking)
-- ============================================================================
CREATE TABLE notification_message (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    appointment_id UUID REFERENCES appointment(id) ON DELETE SET NULL,
    type VARCHAR(100) NOT NULL,  -- BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H
    channel VARCHAR(50) NOT NULL,  -- EMAIL, SMS
    recipient VARCHAR(255) NOT NULL,
    scheduled_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL,  -- SCHEDULED, SENT, FAILED, CANCELED
    provider_message_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_notification_message_client ON notification_message(client_id);
CREATE INDEX idx_notification_message_appointment ON notification_message(appointment_id);
CREATE INDEX idx_notification_message_status ON notification_message(status);
CREATE INDEX idx_notification_message_scheduled ON notification_message(scheduled_at);

-- ============================================================================
-- 14. AUDIT_ENTRY (Immutable audit trail)
-- ============================================================================
CREATE TABLE audit_entry (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,  -- CREATE, UPDATE, DELETE
    actor_id UUID REFERENCES user_account(id) ON DELETE SET NULL,
    at TIMESTAMPTZ NOT NULL,
    before_json TEXT,
    after_json TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_entry_entity ON audit_entry(entity_type, entity_id);
CREATE INDEX idx_audit_entry_actor ON audit_entry(actor_id);
CREATE INDEX idx_audit_entry_at ON audit_entry(at);
CREATE INDEX idx_audit_entry_action ON audit_entry(action);

-- ============================================================================
-- Schema creation complete
-- ============================================================================

