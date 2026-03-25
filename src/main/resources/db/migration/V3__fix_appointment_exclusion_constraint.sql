-- V3__fix_appointment_exclusion_constraint.sql
-- Fix the appointment exclusion constraint to use '[)' instead of '[]'
-- This prevents boundary overlaps (e.g., 11:00 is not included in 11:00-12:00)

-- Check if constraint exists and drop it if it does
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_no_overlap;

-- Re-create the constraint with correct boundaries: '[)' means start inclusive, end exclusive
ALTER TABLE appointment ADD CONSTRAINT appointment_no_overlap
    EXCLUDE USING gist (
        employee_id WITH =,
        tstzrange_immutable(start_at, end_at, '[)') WITH &&
    ) WHERE (status != 'CANCELED');

