-- V5__init_auth_users.sql
-- Initialize test users with different roles for Phase 2 authentication testing
-- Users created with BCrypt hashed passwords

-- Admin user: admin@beautycenter.com / password: Admin@123
-- Employee user: employee@beautycenter.com / password: Employee@123
-- Client user: client@beautycenter.com / password: Client@123

-- NOTE: Users are now created at runtime via @DataJpaTest setup methods
-- This migration file is kept for documentation purposes only
-- See src/test/java/beauty_center/modules/auth/AuthenticationIntegrationTest.java for setup method
