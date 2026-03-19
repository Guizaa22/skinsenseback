# Beauty Center - Free port 8050, then start
$ErrorActionPreference = "SilentlyContinue"
$port = 8050

Write-Host "=== Beauty Center - Starting ===" -ForegroundColor Cyan
Write-Host "Port: $port" -ForegroundColor Cyan

$maxAttempts = 3
$attempt = 0
$portFree = $false

while ($attempt -lt $maxAttempts) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if (-not $conn) {
        $portFree = $true
        Write-Host "Port $port is free." -ForegroundColor Green
        break
    }
    $attempt++
    $pids = $conn.OwningProcess | Sort-Object -Unique
    foreach ($p in $pids) {
        Write-Host "Killing process on port $port (PID: $p)..."
        Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
    }
    Write-Host "Waiting 5 seconds for port to be released (attempt $attempt/$maxAttempts)..."
    Start-Sleep -Seconds 5
}

if (-not $portFree) {
    $still = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($still) {
        Write-Host ""
        Write-Host "ERROR: Port $port is still in use. Close the other app using it, then run this script again." -ForegroundColor Red
        Write-Host "To force-kill from another PowerShell: Get-NetTCPConnection -LocalPort $port | ForEach-Object { Stop-Process -Id `$_.OwningProcess -Force }" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""
Write-Host "Starting Spring Boot (backend only)..." -ForegroundColor Yellow
Write-Host "Open http://localhost:$port when ready." -ForegroundColor Green
Write-Host ""
$env:SERVER_PORT = "$port"
& .\mvnw.cmd spring-boot:run
