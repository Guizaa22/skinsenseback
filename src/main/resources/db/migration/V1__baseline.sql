-- Baseline migration: create basic schema and minimal tables used by app
-- This is intentionally minimal and idempotent where possible.

CREATE SCHEMA IF NOT EXISTS public;

-- Users table (adjust columns to match entity mapping if necessary)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  full_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(50),
  password_hash VARCHAR(255),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Appointments table (minimal)
CREATE TABLE IF NOT EXISTS appointments (
  id UUID PRIMARY KEY,
  start_at TIMESTAMP WITH TIME ZONE,
  end_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(50),
  client_id UUID REFERENCES users(id),
  employee_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Audit entries table
CREATE TABLE IF NOT EXISTS audit_entry (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(100),
  entity_id UUID,
  action VARCHAR(100),
  at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  before_json TEXT,
  after_json TEXT,
  ip_address VARCHAR(50)
);

-- Flyway schema history will be created automatically by Flyway itself
