-- V10: Add client_file columns that were in entity/updated V9 but missing in DB (old V9 was applied earlier)
ALTER TABLE client_file
  ADD COLUMN IF NOT EXISTS how_found_us VARCHAR(255),
  ADD COLUMN IF NOT EXISTS consultation_type_autre VARCHAR(255),
  ADD COLUMN IF NOT EXISTS autre_antecedent TEXT,
  ADD COLUMN IF NOT EXISTS date_arret_medicament VARCHAR(100),
  ADD COLUMN IF NOT EXISTS allergies_medicamenteuses BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS allergies_cosmetiques BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS reactions_post_soins_anterieures BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS details_allergies TEXT,
  ADD COLUMN IF NOT EXISTS details_injections TEXT;
