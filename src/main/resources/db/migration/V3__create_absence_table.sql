-- V3__create_absence_table.sql
-- Create the `absence` table required by the Absence entity
CREATE TABLE IF NOT EXISTS absence (
  id UUID PRIMARY KEY,
  employee_id UUID NOT NULL,
  start_at TIMESTAMP WITH TIME ZONE NOT NULL,
  end_at TIMESTAMP WITH TIME ZONE NOT NULL,
  reason VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
