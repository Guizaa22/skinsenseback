-- SQL Verification Script for Phase 1 Schema
-- Run these queries to verify all tables and constraints are correctly created

-- ============================================================================
-- 1. Vérifier que toutes les 14 tables existent
-- ============================================================================
SELECT
  table_name,
  (SELECT count(*) FROM information_schema.columns
   WHERE table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Expected output: 14 tables
-- user_account, specialty, employee_specialty, beauty_service, beauty_service_employee,
-- working_time_slot, absence, appointment, client_consent, client_file,
-- professional_note, notification_rule, notification_message, audit_entry


-- ============================================================================
-- 2. Vérifier la contrainte d'exclusion (anti-chevauchement)
-- ============================================================================
SELECT
  constraint_name,
  constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'appointment'
ORDER BY constraint_name;

-- Expected: Should include "no_overlapping_appointments" with type "EXCLUDE"
-- If not found, the exclusion constraint may have failed to create


-- ============================================================================
-- 3. Vérifier l'extension btree_gist
-- ============================================================================
SELECT * FROM pg_extension WHERE extname = 'btree_gist';

-- Expected: One row showing btree_gist extension is installed
-- If empty, run: CREATE EXTENSION IF NOT EXISTS btree_gist;


-- ============================================================================
-- 4. Lister tous les indexes (FK + recherche)
-- ============================================================================
SELECT
  indexname,
  tablename,
  indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Expected: Multiple indexes on:
-- - user_account: idx_user_account_email, idx_user_account_user_type, idx_user_account_is_active
-- - appointment: idx_appointment_client, idx_appointment_employee, idx_appointment_start_end, idx_appointment_status
-- - notification_message: idx_notification_message_status, idx_notification_message_scheduled_at
-- etc.


-- ============================================================================
-- 5. Vérifier les Foreign Keys
-- ============================================================================
SELECT
  tc.table_name,
  kcu.column_name,
  ccu.table_name AS foreign_table_name,
  ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name;

-- Expected: FKs for:
-- employee_specialty: employee_id → user_account, specialty_id → specialty
-- beauty_service: specialty_id → specialty
-- beauty_service_employee: beauty_service_id → beauty_service, employee_id → user_account
-- working_time_slot: employee_id → user_account
-- absence: employee_id → user_account
-- appointment: client_id, employee_id, beauty_service_id → their targets
-- client_consent: client_id → user_account
-- client_file: client_id → user_account
-- professional_note: appointment_id → appointment, employee_id → user_account
-- notification_rule: beauty_service_id → beauty_service
-- notification_message: client_id → user_account, appointment_id → appointment
-- audit_entry: actor_id → user_account


-- ============================================================================
-- 6. Vérifier les colonnes et types de user_account
-- ============================================================================
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'user_account'
ORDER BY ordinal_position;

-- Expected columns with types:
-- id: uuid
-- user_type: character varying (discriminator)
-- full_name: character varying
-- email: character varying
-- phone: character varying
-- password_hash: character varying
-- is_active: boolean
-- role: character varying
-- created_at: timestamp with time zone
-- updated_at: timestamp with time zone


-- ============================================================================
-- 7. Vérifier les colonnes et types de appointment
-- ============================================================================
SELECT
  column_name,
  data_type,
  is_nullable,
  column_default
FROM information_schema.columns
WHERE table_name = 'appointment'
ORDER BY ordinal_position;

-- Expected:
-- id: uuid
-- client_id: uuid (NOT NULL)
-- employee_id: uuid (NOT NULL)
-- beauty_service_id: uuid (nullable)
-- start_at: timestamp with time zone (NOT NULL)
-- end_at: timestamp with time zone (NOT NULL)
-- status: character varying (NOT NULL)
-- cancellation_reason: character varying
-- created_at: timestamp with time zone
-- updated_at: timestamp with time zone


-- ============================================================================
-- 8. Vérifier les colonnes de client_file (sections)
-- ============================================================================
SELECT
  column_name,
  data_type,
  is_nullable
FROM information_schema.columns
WHERE table_name = 'client_file'
ORDER BY ordinal_position;

-- Expected sections:
-- Intake: how_did_you_hear_about_us, consultation_reason, objective, care_type, skincare_routine, habits
-- Medical: medical_background, current_treatments, allergies_and_reactions
-- Aesthetic: procedures
-- Consent: photo_consent_for_followup, photo_consent_for_marketing


-- ============================================================================
-- 9. Test: Créer un appointment non chevauchant (devrait réussir)
-- ============================================================================
-- UNCOMMENT POUR TESTER

-- BEGIN;
--
-- -- Créer client
-- INSERT INTO user_account (id, user_type, full_name, email, password_hash, is_active, role)
-- VALUES ('11111111-1111-1111-1111-111111111111'::uuid, 'CLIENT', 'Test Client', 'client@test.com', 'hash', true, 'CLIENT');
--
-- -- Créer employee
-- INSERT INTO user_account (id, user_type, full_name, email, password_hash, is_active, role)
-- VALUES ('22222222-2222-2222-2222-222222222222'::uuid, 'EMPLOYEE', 'Test Employee', 'employee@test.com', 'hash', true, 'EMPLOYEE');
--
-- -- Créer service
-- INSERT INTO beauty_service (id, name, duration_min, price, is_active)
-- VALUES ('33333333-3333-3333-3333-333333333333'::uuid, 'Test Service', 60, 100.00, true);
--
-- -- Créer appointment
-- INSERT INTO appointment (id, client_id, employee_id, beauty_service_id, start_at, end_at, status)
-- VALUES (
--   '44444444-4444-4444-4444-444444444444'::uuid,
--   '11111111-1111-1111-1111-111111111111'::uuid,
--   '22222222-2222-2222-2222-222222222222'::uuid,
--   '33333333-3333-3333-3333-333333333333'::uuid,
--   '2026-02-10 10:00:00+01:00'::timestamptz,
--   '2026-02-10 11:00:00+01:00'::timestamptz,
--   'CONFIRMED'
-- );
--
-- SELECT 'Test 1: NON-OVERLAPPING appointment' as result, 'SUCCESS' as status;
--
-- ROLLBACK;


-- ============================================================================
-- 10. Test: Créer un appointment chevauchant (devrait échouer)
-- ============================================================================
-- UNCOMMENT POUR TESTER

-- BEGIN;
--
-- -- Créer client
-- INSERT INTO user_account (id, user_type, full_name, email, password_hash, is_active, role)
-- VALUES ('11111111-1111-1111-1111-111111111111'::uuid, 'CLIENT', 'Test Client', 'client@test.com', 'hash', true, 'CLIENT');
--
-- -- Créer employee
-- INSERT INTO user_account (id, user_type, full_name, email, password_hash, is_active, role)
-- VALUES ('22222222-2222-2222-2222-222222222222'::uuid, 'EMPLOYEE', 'Test Employee', 'employee@test.com', 'hash', true, 'EMPLOYEE');
--
-- -- Créer service
-- INSERT INTO beauty_service (id, name, duration_min, price, is_active)
-- VALUES ('33333333-3333-3333-3333-333333333333'::uuid, 'Test Service', 60, 100.00, true);
--
-- -- Créer appointment 1
-- INSERT INTO appointment (id, client_id, employee_id, beauty_service_id, start_at, end_at, status)
-- VALUES (
--   '44444444-4444-4444-4444-444444444444'::uuid,
--   '11111111-1111-1111-1111-111111111111'::uuid,
--   '22222222-2222-2222-2222-222222222222'::uuid,
--   '33333333-3333-3333-3333-333333333333'::uuid,
--   '2026-02-10 10:00:00+01:00'::timestamptz,
--   '2026-02-10 11:00:00+01:00'::timestamptz,
--   'CONFIRMED'
-- );
--
-- -- Tentative de créer appointment 2 qui chevauche (devrait échouer)
-- INSERT INTO appointment (id, client_id, employee_id, beauty_service_id, start_at, end_at, status)
-- VALUES (
--   '55555555-5555-5555-5555-555555555555'::uuid,
--   '11111111-1111-1111-1111-111111111111'::uuid,
--   '22222222-2222-2222-2222-222222222222'::uuid,
--   '33333333-3333-3333-3333-333333333333'::uuid,
--   '2026-02-10 10:30:00+01:00'::timestamptz,
--   '2026-02-10 11:30:00+01:00'::timestamptz,
--   'CONFIRMED'
-- );
-- -- Should fail with: ERROR: duplicate key value violates unique constraint "no_overlapping_appointments"
--
-- ROLLBACK;


-- ============================================================================
-- 11. Vérifier Flyway schema history
-- ============================================================================
SELECT
  version,
  description,
  type,
  installed_by,
  installed_on,
  execution_time,
  success
FROM flyway_schema_history
ORDER BY version;

-- Expected: 4 rows (V1-V4) with success=true
-- If V4 shows success=false, there was an error during migration


-- ============================================================================
-- 12. Compter le nombre de lignes par table (vérifier données)
-- ============================================================================
WITH tables AS (
  SELECT table_name FROM information_schema.tables
  WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
)
SELECT
  table_name,
  (SELECT count(*) FROM (SELECT 1 FROM information_schema.tables t
   WHERE t.table_name = tables.table_name LIMIT 1) AS dummy) as row_count
FROM tables
ORDER BY table_name;

-- Vérifie qu'on peut requêter chaque table sans erreur

