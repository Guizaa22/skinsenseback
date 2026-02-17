-- V5__appointment_authorization_enforcement.sql
-- Documents authorization enforcement for appointments
-- This migration ensures the database structure supports the authorization rules:
--
-- Authorization Rules:
-- 1. ADMIN: Can manage all appointments
-- 2. EMPLOYEE: Can only manage their own appointments (appointment.employee_id == current_user.id)
-- 3. CLIENT: Can only cancel/view their own appointments (appointment.client_id == current_user.id)
--
-- No database schema changes needed; enforcement is at the application layer.
-- The existing foreign keys ensure data integrity:

-- Appointments table structure (already present from V2):
-- CREATE TABLE appointment (
--     id UUID PRIMARY KEY,
--     client_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
--     employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
--     beauty_service_id UUID REFERENCES beauty_service(id) ON DELETE SET NULL,
--     start_at TIMESTAMPTZ NOT NULL,
--     end_at TIMESTAMPTZ NOT NULL,
--     status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
--     cancellation_reason TEXT,
--     created_at TIMESTAMPTZ NOT NULL,
--     updated_at TIMESTAMPTZ
-- );

-- The application layer validates authorization before any operation using CurrentUser context.

