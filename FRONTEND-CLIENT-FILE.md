# Frontend – Client File Compatibility

## Problem

After filling and saving the client file, the "complete your file" popup kept showing.

## Backend Fixes Applied

1. **consentGiven & isComplete** – API now returns these in GET/PUT responses.
2. **Relaxed isComplete** – `true` when user has given consent **OR** filled content (not both required).
3. **photoConsentForFollowUp alias** – Backend accepts both `photoConsentForFollowup` and `photoConsentForFollowUp` in the PUT request body.

## Response Fields

| Field         | Type    | Description |
|---------------|---------|-------------|
| `consentGiven` | boolean | `true` when client checked at least one photo consent (followup or marketing). |
| `isComplete`   | boolean | `true` when consent given **OR** at least one section has content. |

## Frontend Usage

### Hide "complete file" popup

Use either field depending on your rules:

- **Option A – require consent only:**  
  Show the popup only when `!data.consentGiven`.

- **Option B – require content + consent:**  
  Show the popup only when `!data.isComplete`.

### After save

1. Ensure the PUT response body is used to update local state.
2. Check `response.data.consentGiven` or `response.data.isComplete` to decide whether to hide the popup.
3. Or refetch with `GET /api/client/me/file` after a successful save and use the new `consentGiven` / `isComplete` values.

### PUT request – payload shape

Send either spelling for the follow-up consent (both work):

```json
{
  "intake": { "howDidYouHearAboutUs": "...", "consultationReason": "...", ... },
  "medicalHistory": { "medicalBackground": "...", "allergiesAndReactions": "...", ... },
  "aestheticProcedureHistory": { "procedures": "..." },
  "photoConsentForFollowup": true,
  "photoConsentForMarketing": false
}
```

Or use `photoConsentForFollowUp` (capital U) – the backend accepts both.

### Frontend checklist

1. **After save** – Update local state from the PUT response (do not keep stale state).
2. **Popup logic** – Use `!clientFile?.isComplete` or `!clientFile?.consentGiven` (both now work reliably).
3. **Refetch** – If redirecting after save, ensure Booking/Dashboard refetches `GET /api/client/me/file` so it gets fresh `isComplete`.
4. **State source** – Prefer the API response for `isComplete` over any local flag.

---

## Troubleshooting: no logs in terminal

1. **API base URL** – Frontend must call the backend. Set `VITE_API_BASE_URL=http://localhost:8050` in frontend `.env` so requests go to `http://localhost:8050/api/...`, or use Vite proxy with empty base URL.
2. **Verify requests** – DevTools → Network: filter by "client" or "file". Confirm `PUT /api/client/me/file` returns 200 and the response body has `isComplete: true`.
3. **Backend logs** – Restart backend with `.\run.ps1`, then save the client file. Look for `[CLIENT-FILE]` in the same terminal:
   - `[CLIENT-FILE] PUT /api/client/me/file - request received, photoConsentForFollowup=true, ...`
   - `[CLIENT-FILE] PUT response: isComplete=true, consentGiven=true`
4. **No logs** – If you never see `[CLIENT-FILE]`, requests are not reaching the backend. Check CORS, proxy config, and `VITE_API_BASE_URL`.
