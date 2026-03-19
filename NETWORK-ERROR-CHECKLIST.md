# Network Error – What to Check

When the frontend shows **"Network Error"** or **ERR_CONNECTION_REFUSED**, the browser cannot reach the backend. Use this checklist.

---

## 1. Backend is running

- Run: `.\run.ps1` (from project root).
- Wait until you see something like: `Tomcat started on port(s): 8050`.
- Open in browser: http://localhost:8050/actuator/health  
  - If it loads → backend is up.  
  - If it fails → backend is not running or not on 8050.

---

## 2. Database (PostgreSQL)

If the **DB password is wrong**, the backend may fail to start or crash right after start. Then nothing listens on 8050 → frontend gets "network error".

- PostgreSQL service must be **running** (pgAdmin does not need to be open).
- User: `postgres`, password: `gezgez`.
- Database must exist: `beauty_center`.
- Test from command line:
  ```powershell
  $env:PGPASSWORD = "gezgez"
  psql -U postgres -h localhost -p 5432 -d beauty_center -c "SELECT 1"
  ```
  If this fails, fix PostgreSQL (password, database name, or service).

---

## 3. Frontend API base URL

The frontend must call the **same host and port** as the backend.

- Backend: http://localhost:8050  
- In the **frontend** project, in `.env`:
  ```
  VITE_API_BASE_URL=http://localhost:8050
  ```
- If you use 8082, 8080, or another port (instead of 8050), the frontend will get connection refused.
- Restart the frontend dev server after changing `.env` (`npm run dev`).

---

## 4. Backend port

- Default in this project: **8050** (see `application.yml` and `run.ps1`).
- If you changed the port, the frontend `VITE_API_BASE_URL` must use that port.

---

## 5. Firewall / antivirus

- Temporarily allow Java or the backend process, or disable firewall for a quick test.
- Blocking localhost can cause connection refused.

---

## 6. CORS (only if backend responds but frontend still fails)

- Backend allows: `http://localhost:3000`, `http://localhost:5173`, etc. (see `application.yml`).
- If your frontend runs on another origin, add it to CORS in the backend.

---

## Summary

| Check              | Action |
|--------------------|--------|
| Backend running    | `.\run.ps1`, then open http://localhost:8050/actuator/health |
| DB password        | All configs use `gezgez`; PostgreSQL user `postgres` must have this password |
| DB exists          | Database `beauty_center` must exist |
| Frontend URL       | `.env`: `VITE_API_BASE_URL=http://localhost:8050` |
| Restart frontend   | After changing `.env`, run `npm run dev` again |

**Most often:** backend not running, or frontend still using the wrong port (e.g. 8082 instead of 8050).
