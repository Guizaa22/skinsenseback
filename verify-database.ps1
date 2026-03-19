# PowerShell script to verify database schema
# Usage: .\verify-database.ps1

$env:PGPASSWORD = "gezgez"

Write-Host "`n=== Verifying Database Schema ===" -ForegroundColor Cyan

# Check tables
Write-Host "`n1. Checking tables..." -ForegroundColor Yellow
psql -U postgres -d beauty_center -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;"

# Check user_account table
Write-Host "`n2. Checking user_account structure..." -ForegroundColor Yellow
psql -U postgres -d beauty_center -c "\d user_account"

# Check appointment table with exclusion constraint
Write-Host "`n3. Checking appointment table constraints..." -ForegroundColor Yellow
psql -U postgres -d beauty_center -c "SELECT constraint_name, constraint_type FROM information_schema.table_constraints WHERE table_name = 'appointment' ORDER BY constraint_name;"

# Check if test users were created
Write-Host "`n4. Checking test users..." -ForegroundColor Yellow
psql -U postgres -d beauty_center -c "SELECT id, full_name, email, role FROM user_account ORDER BY role;"

Write-Host "`n=== Verification Complete ===" -ForegroundColor Green

