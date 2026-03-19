# Beauty Center – Project Structure

```
beauty_center_project/
│
├── src/                                    # Backend (Spring Boot)
│   └── main/
│       ├── java/beauty_center/             # Java source
│       └── resources/
│           ├── application.yml
│           └── db/migration/               # Flyway migrations
│
├── Beauty_Center_FrontOffice-main/         # Frontend (React + Vite)
│   ├── src/
│   │   ├── api/api.ts                      # Axios client, API base URL
│   │   ├── services/api.ts                 # API service layer
│   │   ├── pages/                          # React pages
│   │   └── ...
│   ├── .env                                # VITE_API_BASE_URL (empty = proxy)
│   ├── vite.config.ts                      # Dev server, proxy /api → 8082
│   └── package.json
│
├── package.json                            # Root: npm scripts for frontend
├── pom.xml                                 # Maven: backend
├── mvnw, mvnw.cmd                          # Maven wrapper
├── API_ENDPOINTS.md                        # API reference
├── README_INTEGRATION.md                   # How to run
└── PROJECT_STRUCTURE.md                    # This file
```

## API integration

- **Frontend** sends requests to `/api/*` (relative) when served from backend
- **Backend** runs on port **8050**
- **CORS** allows `http://localhost:3000`, `3001`, `5173`
