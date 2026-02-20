-- V7__fix_appointment_overlap_constraint.sql
-- Fix: change appointment overlap exclusion constraint from '[]' (inclusive end)
-- to '[)' (exclusive end) so back-to-back appointments are allowed.
-- e.g. 10:00-11:00 and 11:00-12:00 should NOT overlap.

-- Drop the old immutable helper function and constraint
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_no_overlap;
DROP FUNCTION IF EXISTS tstzrange_immutable(timestamptz, timestamptz, text);

-- Recreate helper function (unchanged, still needed by GiST)
CREATE OR REPLACE FUNCTION tstzrange_immutable(timestamptz, timestamptz, text)
RETURNS tstzrange AS $$
  SELECT tstzrange($1, $2, $3);
$$ LANGUAGE SQL IMMUTABLE;

-- Recreate constraint with '[)' (inclusive start, exclusive end)
ALTER TABLE appointment ADD CONSTRAINT appointment_no_overlap
    EXCLUDE USING gist (
        employee_id WITH =,
        tstzrange_immutable(start_at, end_at, '[)') WITH &&
    ) WHERE (status != 'CANCELED');
