# PowerShell script to run Flyway migrations with environment variables
# Usage: .\flyway-migrate.ps1

# Load environment variables from .env file
if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]*)\s*=\s*(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path "env:$name" -Value $value
            Write-Host "Set $name" -ForegroundColor Green
        }
    }
} else {
    Write-Host ".env file not found! Please create it from .env.example" -ForegroundColor Red
    exit 1
}


Write-Host "`nRunning Flyway migration..." -ForegroundColor Cyan
mvn flyway:migrate

