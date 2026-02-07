-- V4__create_phase1_complete_schema.sql
-- Complete Phase 1 schema with all entities, FK constraints, indexes, and exclusion constraint for appointment overlap prevention
-- Uses TIMESTAMPTZ for all datetime columns and UUID for primary keys

-- ============================================================================
-- 0. IMMUTABLE FUNCTION for range type (required for use in EXCLUDE constraint)
-- ============================================================================
-- Create an IMMUTABLE wrapper function for tsrange to comply with PostgreSQL requirements
-- PostgreSQL requires functions in index expressions to be IMMUTABLE
CREATE OR REPLACE FUNCTION tsrange_immutable(start_ts timestamptz, end_ts timestamptz)
RETURNS tsrange AS $$
  SELECT tsrange(start_ts::timestamp, end_ts::timestamp)
$$ LANGUAGE SQL IMMUTABLE;

-- ============================================================================
-- 1. USER_ACCOUNT (polymorphic base using SINGLE_TABLE inheritance)
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_account (
  id UUID PRIMARY KEY,
  user_type VARCHAR(50) NOT NULL,  -- Discriminator: CLIENT, EMPLOYEE, ADMIN
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(50),
  password_hash VARCHAR(255) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true,
  role VARCHAR(50),  -- StaffRole: ADMIN, EMPLOYEE (NULL for clients)
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_account_email ON user_account(email);
CREATE INDEX idx_user_account_user_type ON user_account(user_type);
CREATE INDEX idx_user_account_is_active ON user_account(is_active);


-- ============================================================================
-- 2. SPECIALTY
-- ============================================================================
CREATE TABLE IF NOT EXISTS specialty (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_specialty_name ON specialty(name);


-- ============================================================================
-- 3. EMPLOYEE_SPECIALTY (join table: many-to-many)
-- ============================================================================
CREATE TABLE IF NOT EXISTS employee_specialty (
  id UUID PRIMARY KEY,
  employee_id UUID NOT NULL,
  specialty_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_employee_specialty_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_employee_specialty_specialty FOREIGN KEY (specialty_id)
    REFERENCES specialty(id) ON DELETE CASCADE,
  UNIQUE(employee_id, specialty_id)
);

CREATE INDEX idx_employee_specialty_employee ON employee_specialty(employee_id);
CREATE INDEX idx_employee_specialty_specialty ON employee_specialty(specialty_id);


-- ============================================================================
-- 4. BEAUTY_SERVICE
-- ============================================================================
CREATE TABLE IF NOT EXISTS beauty_service (
  id UUID PRIMARY KEY,
  specialty_id UUID,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  duration_min INT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_beauty_service_specialty FOREIGN KEY (specialty_id)
    REFERENCES specialty(id) ON DELETE SET NULL
);

CREATE INDEX idx_beauty_service_name ON beauty_service(name);
CREATE INDEX idx_beauty_service_specialty ON beauty_service(specialty_id);
CREATE INDEX idx_beauty_service_is_active ON beauty_service(is_active);


-- ============================================================================
-- 5. BEAUTY_SERVICE_EMPLOYEE (join table: many-to-many, allowed performers)
-- ============================================================================
CREATE TABLE IF NOT EXISTS beauty_service_employee (
  id UUID PRIMARY KEY,
  beauty_service_id UUID NOT NULL,
  employee_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_beauty_service_employee_service FOREIGN KEY (beauty_service_id)
    REFERENCES beauty_service(id) ON DELETE CASCADE,
  CONSTRAINT fk_beauty_service_employee_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  UNIQUE(beauty_service_id, employee_id)
);

CREATE INDEX idx_beauty_service_employee_service ON beauty_service_employee(beauty_service_id);
CREATE INDEX idx_beauty_service_employee_employee ON beauty_service_employee(employee_id);


-- ============================================================================
-- 6. WORKING_TIME_SLOT (weekly schedule per employee)
-- ============================================================================
CREATE TABLE IF NOT EXISTS working_time_slot (
  id UUID PRIMARY KEY,
  employee_id UUID NOT NULL,
  day_of_week VARCHAR(10) NOT NULL,  -- MON, TUE, WED, THU, FRI, SAT, SUN
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_working_time_slot_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  UNIQUE(employee_id, day_of_week)
);

CREATE INDEX idx_working_time_slot_employee ON working_time_slot(employee_id);
CREATE INDEX idx_working_time_slot_day ON working_time_slot(day_of_week);


-- ============================================================================
-- 7. ABSENCE (employee time off / vacation)
-- ============================================================================
CREATE TABLE IF NOT EXISTS absence (
  id UUID PRIMARY KEY,
  employee_id UUID NOT NULL,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  reason VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_absence_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_absence_employee ON absence(employee_id);
CREATE INDEX idx_absence_start_end ON absence(start_at, end_at);


-- ============================================================================
-- 8. APPOINTMENT (bookings with exclusion constraint for overlap prevention)
-- ============================================================================
CREATE TABLE IF NOT EXISTS appointment (
  id UUID PRIMARY KEY,
  client_id UUID NOT NULL,
  employee_id UUID NOT NULL,
  beauty_service_id UUID,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(50) NOT NULL,  -- CONFIRMED, CANCELED, COMPLETED
  cancellation_reason VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_appointment_client FOREIGN KEY (client_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_appointment_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_appointment_service FOREIGN KEY (beauty_service_id)
    REFERENCES beauty_service(id) ON DELETE SET NULL
);

-- Indexes for common queries
CREATE INDEX idx_appointment_client ON appointment(client_id);
CREATE INDEX idx_appointment_employee ON appointment(employee_id);
CREATE INDEX idx_appointment_start_end ON appointment(start_at, end_at);
CREATE INDEX idx_appointment_status ON appointment(status);

-- EXCLUSION CONSTRAINT (PostgreSQL only): prevents overlapping appointments for same employee
-- Excludes overlapping time ranges for the same employee, BUT allows CANCELED appointments to overlap
-- Syntax: EXCLUDE USING <index_type> (column1 <operator>, column2 <operator>)
-- Using GiST (Generalized Search Tree) for range type support
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointment
ADD CONSTRAINT no_overlapping_appointments EXCLUDE USING gist (
  employee_id WITH =,
  tsrange_immutable(start_at, end_at) WITH &&
) WHERE (status != 'CANCELED');


-- ============================================================================
-- 9. CLIENT_CONSENT (SMS opt-in preferences)
-- ============================================================================
CREATE TABLE IF NOT EXISTS client_consent (
  id UUID PRIMARY KEY,
  client_id UUID NOT NULL UNIQUE,
  sms_opt_in BOOLEAN NOT NULL DEFAULT true,
  sms_unsub_token VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_client_consent_client FOREIGN KEY (client_id)
    REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_client_consent_client ON client_consent(client_id);


-- ============================================================================
-- 10. CLIENT_FILE (medical + personal dossier)
-- ============================================================================
CREATE TABLE IF NOT EXISTS client_file (
  id UUID PRIMARY KEY,
  client_id UUID NOT NULL UNIQUE,

  -- Intake section
  how_did_you_hear_about_us VARCHAR(255),
  consultation_reason TEXT,
  objective TEXT,
  care_type VARCHAR(255),
  skincare_routine TEXT,
  habits TEXT,

  -- Medical history section
  medical_background TEXT,
  current_treatments TEXT,
  allergies_and_reactions TEXT,

  -- Aesthetic procedure history section
  procedures TEXT,

  -- Consent
  photo_consent_for_followup BOOLEAN NOT NULL DEFAULT false,
  photo_consent_for_marketing BOOLEAN NOT NULL DEFAULT false,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_client_file_client FOREIGN KEY (client_id)
    REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_client_file_client ON client_file(client_id);


-- ============================================================================
-- 11. PROFESSIONAL_NOTE (post-appointment clinical notes)
-- ============================================================================
CREATE TABLE IF NOT EXISTS professional_note (
  id UUID PRIMARY KEY,
  appointment_id UUID NOT NULL,
  employee_id UUID NOT NULL,
  diagnostic TEXT,
  phototype VARCHAR(100),
  care_performed TEXT,
  products_and_parameters TEXT,
  reactions TEXT,
  recommendations TEXT,
  next_appointment_suggestion TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_professional_note_appointment FOREIGN KEY (appointment_id)
    REFERENCES appointment(id) ON DELETE CASCADE,
  CONSTRAINT fk_professional_note_employee FOREIGN KEY (employee_id)
    REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE INDEX idx_professional_note_appointment ON professional_note(appointment_id);
CREATE INDEX idx_professional_note_employee ON professional_note(employee_id);


-- ============================================================================
-- 12. NOTIFICATION_RULE (template for auto-notifications)
-- ============================================================================
CREATE TABLE IF NOT EXISTS notification_rule (
  id UUID PRIMARY KEY,
  beauty_service_id UUID,
  type VARCHAR(100) NOT NULL,  -- BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H
  channel VARCHAR(50) NOT NULL,  -- EMAIL, SMS
  offset_hours INT,  -- for reminders: -24, -2, etc.
  is_enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_notification_rule_service FOREIGN KEY (beauty_service_id)
    REFERENCES beauty_service(id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_rule_service ON notification_rule(beauty_service_id);
CREATE INDEX idx_notification_rule_type_channel ON notification_rule(type, channel);


-- ============================================================================
-- 13. NOTIFICATION_MESSAGE (individual sent messages with tracking)
-- ============================================================================
CREATE TABLE IF NOT EXISTS notification_message (
  id UUID PRIMARY KEY,
  client_id UUID NOT NULL,
  appointment_id UUID,
  type VARCHAR(100) NOT NULL,  -- BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H
  channel VARCHAR(50) NOT NULL,  -- EMAIL, SMS
  recipient VARCHAR(255) NOT NULL,
  scheduled_at TIMESTAMPTZ,
  sent_at TIMESTAMPTZ,
  status VARCHAR(50) NOT NULL,  -- SCHEDULED, SENT, FAILED, CANCELED
  provider_message_id VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_notification_message_client FOREIGN KEY (client_id)
    REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_notification_message_appointment FOREIGN KEY (appointment_id)
    REFERENCES appointment(id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_message_client ON notification_message(client_id);
CREATE INDEX idx_notification_message_appointment ON notification_message(appointment_id);
CREATE INDEX idx_notification_message_status ON notification_message(status);
CREATE INDEX idx_notification_message_scheduled_at ON notification_message(scheduled_at);
CREATE INDEX idx_notification_message_sent_at ON notification_message(sent_at);


-- ============================================================================
-- 14. AUDIT_ENTRY (immutable audit trail)
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_entry (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(100) NOT NULL,
  entity_id UUID NOT NULL,
  action VARCHAR(100) NOT NULL,  -- CREATE, UPDATE, DELETE
  actor_id UUID,
  at TIMESTAMPTZ NOT NULL DEFAULT now(),
  before_json TEXT,
  after_json TEXT,
  ip_address VARCHAR(50),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_audit_entry_actor FOREIGN KEY (actor_id)
    REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_entry_entity_type_id ON audit_entry(entity_type, entity_id);
CREATE INDEX idx_audit_entry_actor ON audit_entry(actor_id);
CREATE INDEX idx_audit_entry_at ON audit_entry(at);

