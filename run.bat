@echo off
REM Beauty Center - Kill process on 8050, then start (avoids Oracle on 8080)
set PORT=8050

echo Killing process on port %PORT%...
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":%PORT%" ^| findstr LISTENING') do (
    echo   Stopping PID %%a...
    taskkill /PID %%a /F 2>nul
)
timeout /t 2 /nobreak >nul

echo Starting application on port %PORT%...
set SERVER_PORT=%PORT%
call mvnw.cmd spring-boot:run
