# Beauty Center – Integration Guide

## Project structure

```
beauty_center_project/
├── src/main/
│   ├── java/beauty_center/           # Backend
│   └── resources/static/             # Frontend build (generated)
├── Beauty_Center_FrontOffice-main/   # Frontend source (React + Vite)
├── pom.xml
└── mvnw
```

## Run on one port (recommended)

**Option A – Run script (recommended; frees port 8082 if already in use):**

```powershell
.\run.ps1
```

Or on Windows CMD:
```cmd
run.bat
```

**Option B – Maven directly:**

```bash
.\mvnw spring-boot:run
```

Then open: **http://localhost:8050**

Port 8050 avoids conflict with Oracle XE (8080). The run scripts kill any process on 8050 before starting.

Maven will:
1. Run `npm install` and `npm run build` in the frontend
2. Build the backend (including static files)
3. Start the app – UI at `/`, API at `/api/*`

**Requirements:** Node.js and npm installed.

## Environment

- **Backend:** PostgreSQL on `localhost:5432`, database `beauty_center`
- **Frontend:** `.env` has `VITE_API_BASE_URL=` (empty) so the Vite proxy is used
- **CORS:** Backend allows `http://localhost:3000`, `3001`, `5173`

## Test accounts

| Role   | Email                      | Password   |
|--------|----------------------------|------------|
| Client | client@beautycenter.com    | Client@123 |
| Admin  | admin@beautycenter.com     | Admin@123  |
| Employee | employee@beautycenter.com | Employee@123 |
