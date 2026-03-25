# Frontend Token Setup (Apply When Frontend Is Restored)

When you add the frontend back, apply these token-related changes to `src/api/api.ts`.

## 1. Refresh URL helper

Add this function before `refreshAccessToken`:

```ts
function getRefreshUrl(): string {
  const base = (API_BASE_URL ?? '').toString().replace(/\/+$/, '');
  return base ? `${base}/api/auth/refresh` : '/api/auth/refresh';
}
```

## 2. Use getRefreshUrl in refresh

In `refreshAccessToken`, replace the hardcoded URL:

```ts
// Before:
`${API_BASE_URL}/api/auth/refresh`

// After:
getRefreshUrl()
```

## 3. Request interceptor – ensure headers exist

```ts
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = config.headers ?? ({} as Record<string, string>);
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

## 4. Retry after refresh – ensure headers exist

In the 401 response interceptor, when retrying after refresh:

```ts
originalRequest.headers = originalRequest.headers ?? ({} as Record<string, string>);
originalRequest.headers.Authorization = `Bearer ${newToken}`;
```

## 5. API_BASE_URL normalization (optional)

For `VITE_API_BASE_URL`, trim trailing slashes:

```ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL !== undefined && import.meta.env.VITE_API_BASE_URL !== ''
  ? (String(import.meta.env.VITE_API_BASE_URL).replace(/\/+$/, '') || '')
  : '';
```

---

## Frontend .env (separate ports)

When frontend runs on 3000 and backend on 8050:

```
VITE_API_BASE_URL=http://localhost:8050
```

Empty = use Vite proxy (relative `/api`).
