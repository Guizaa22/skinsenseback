-- V4__employee_service_authorization.sql
-- Adds validation to ensure employees can only perform services they're authorized for
-- This is enforced at the application level via BeautyServiceEmployeeRepository

-- No database changes needed; authorization is handled by the application layer.
-- This migration exists as documentation of the enforcement point.

-- The beauty_service_employee table (foreign keys) ensures referential integrity:
-- CREATE TABLE beauty_service_employee (
--     id UUID PRIMARY KEY,
--     beauty_service_id UUID NOT NULL REFERENCES beauty_service(id) ON DELETE CASCADE,
--     employee_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
--     created_at TIMESTAMPTZ NOT NULL,
--     UNIQUE(beauty_service_id, employee_id)
-- );

