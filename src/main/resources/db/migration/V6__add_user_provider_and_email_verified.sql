-- V6__add_user_provider_and_email_verified.sql
-- Add email_verified and provider columns to user_account for signup feature

ALTER TABLE user_account
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE user_account
    ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL';

-- Set existing users as LOCAL provider with email verified (they were seeded)
UPDATE user_account SET email_verified = true, provider = 'LOCAL' WHERE provider = 'LOCAL';
