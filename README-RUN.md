# Beauty Center – Run Backend + Frontend (Separate Ports)

Backend and frontend run separately, each on its own port.

## Ports

| Service  | Port | URL                    |
|----------|------|------------------------|
| Backend  | 8050 | http://localhost:8050  |
| Frontend | 3000 | http://localhost:3000  |

(CORS is configured for 3000, 3001, 5173.)

---

## 1. Backend (port 8050)

From the project root:

```powershell
.\run.ps1
```

Or:

```powershell
$env:SERVER_PORT = "8050"
.\mvnw.cmd spring-boot:run
```

- API: http://localhost:8050/api
- Swagger: http://localhost:8050/swagger-ui.html

---

## 2. Frontend (port 3000)

Put the frontend in a folder named `frontend` (or `Beauty_Center_FrontOffice-main`) inside the project root.

> **Note:** Root `package.json` scripts use `--prefix frontend`. If your folder has another name, run npm from that folder directly, or update the `--prefix` value in `package.json`.

### Requirements

1. **`.env` file** in the frontend folder:

   ```
   VITE_API_BASE_URL=http://localhost:8050
   ```

2. **Start the dev server:**

   ```bash
   cd frontend_folder_name
   npm install
   npm run dev
   ```

3. Open: http://localhost:3000

### Vite proxy (optional)

If using Vite, configure a proxy in `vite.config.ts` so `/api` calls go to the backend:

```ts
server: {
  port: 3000,
  proxy: {
    '/api': { target: 'http://localhost:8050', changeOrigin: true },
  },
},
```

Then either:

- Use `VITE_API_BASE_URL=` (empty) so requests use relative `/api` and are proxied.
- Or use `VITE_API_BASE_URL=http://localhost:8050` for direct calls to the backend.

---

## 3. Token handling (frontend)

Token-related changes are documented in `FRONTEND-TOKEN-SETUP.md`. Apply them in your frontend `api.ts` when the frontend is restored.
