# ⚙️ CORS Configuration for Backend

## Important!
Your **frontend will NOT work** unless your backend has proper CORS configuration.

---

## What is CORS?
CORS (Cross-Origin Resource Sharing) allows your frontend to make requests to your backend when they're on different domains.

---

## Frontend URL After Railway Deployment
After deploying to Railway, your frontend will be at:
```
https://your-app-name.railway.app
```

**Replace `your-app-name` with your actual Railway service name**

---

## Backend CORS Setup

### For Express.js (Node.js Backend)

#### Option 1: Using Environment Variables (RECOMMENDED)

1. **Add to your `.env` file:**
```
CORS_ORIGIN=https://your-app-name.railway.app
NODE_ENV=production
```

2. **In your code (e.g., `server.js` or `app.js`):**
```javascript
import cors from 'cors';

const corsOptions = {
  origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
  credentials: true,
  optionsSuccessStatus: 200,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
};

app.use(cors(corsOptions));
```

#### Option 2: Allow Multiple Origins

```javascript
const allowedOrigins = [
  'https://your-app-name.railway.app',
  'http://localhost:3000',  // for local development
  process.env.CORS_ORIGIN   // for environment variable
];

const corsOptions = {
  origin: function(origin, callback) {
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  },
  credentials: true,
  optionsSuccessStatus: 200
};

app.use(cors(corsOptions));
```

#### Option 3: Allow All Origins (NOT RECOMMENDED FOR PRODUCTION)

```javascript
app.use(cors({
  origin: '*',
  credentials: false
}));
```

---

## Common CORS Errors

### Error: "Access-Control-Allow-Origin' header is missing"
**Solution**: Make sure CORS middleware is configured above your routes:
```javascript
app.use(cors(corsOptions)); // This MUST be before your routes
app.use('/api', routes);
```

### Error: "Credentials mode is 'include', but 'Access-Control-Allow-Credentials' header is missing"
**Solution**: Enable credentials:
```javascript
const corsOptions = {
  origin: 'https://your-app-name.railway.app',
  credentials: true  // ← Add this
};
```

### Error: "Method OPTIONS is not allowed"
**Solution**: CORS preflight requests use OPTIONS method. Make sure it's allowed:
```javascript
app.options('*', cors(corsOptions));
```

---

## Testing CORS Locally

Before deploying, test locally:

```bash
# Terminal 1: Backend on port 8050
npm start  # or python manage.py runserver, etc.

# Terminal 2: Frontend on port 3000
npm run dev
```

Test in browser console:
```javascript
fetch('http://localhost:8050/api/auth/me', {
  method: 'GET',
  credentials: 'include',
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(console.log)
.catch(console.error)
```

---

## After Railway Deployment

1. **Get your actual Railway URL** from Railway dashboard
   - Format: `https://YOUR-SERVICE-NAME.railway.app`

2. **Update backend `.env`:**
```
CORS_ORIGIN=https://YOUR-SERVICE-NAME.railway.app
```

3. **Redeploy your backend** to apply changes

4. **Test from your frontend** (visit your frontend URL in browser)

---

## Complete Example

### Backend Express Setup
```javascript
import express from 'express';
import cors from 'cors';

const app = express();

// CORS middleware
const corsOptions = {
  origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
  credentials: true,
  optionsSuccessStatus: 200,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
};

app.use(cors(corsOptions));
app.options('*', cors(corsOptions)); // Enable preflight for all routes

// Middleware
app.use(express.json());

// Routes
app.post('/api/auth/login', (req, res) => {
  // Your login logic
});

// Start server
const PORT = process.env.PORT || 8050;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`CORS enabled for: ${process.env.CORS_ORIGIN || 'http://localhost:3000'}`);
});
```

### .env File
```
PORT=8050
NODE_ENV=production
CORS_ORIGIN=https://your-app-name.railway.app
# ... other variables
```

---

## Secret Header in Frontend

Your frontend already includes the `Authorization` header for authenticated requests:

```typescript
// In src/services/api.ts
const token = localStorage.getItem('token');
const res = await fetch(`${BASE}${path}`, {
  headers: {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  },
});
```

So make sure your backend CORS allows `Authorization` header.

---

## Checklist

- [ ] CORS middleware is installed (`npm install cors`)
- [ ] CORS is configured in your backend main file
- [ ] `CORS_ORIGIN` environment variable is set in `.env`
- [ ] Backend allows `Authorization` header
- [ ] Backend is deployed to Railway
- [ ] `CORS_ORIGIN` is set in Railway dashboard
- [ ] Frontend URL is added to allowed origins
- [ ] Backend logs show correct CORS origin

---

## Need Help?

Check these resources:
- **Express CORS docs**: https://expressjs.com/en/resources/middleware/cors.html
- **CORS explained**: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
- **Railway Environment Variables**: https://railway.app/docs

---

**After setting this up, your frontend and backend will communicate seamlessly! 🎉**
