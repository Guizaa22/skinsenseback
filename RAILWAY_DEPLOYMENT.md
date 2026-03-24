# Railway Deployment Guide

## Project Status
✅ **Frontend**: Ready for Railway deployment  
✅ **Backend**: Already deployed at `https://skinsenseback-production.up.railway.app`  

## Frontend Configuration for Railway

### What's Been Updated:
1. **Procfile** - Tells Railway how to run the app
2. **.env.production** - Production API URL pointing to your deployed backend
3. **vite.config.ts** - Updated to handle production builds and dynamic port configuration
4. **package.json** - Added `serve` dependency and `start` script for production serving
5. **Node engine spec** - Specified Node.js version compatibility

### Before Deploying to Railway:

#### Step 1: Install Dependencies Locally
```bash
npm install
```

#### Step 2: Test Build Locally
```bash
npm run build
```
This should create a `dist/` folder with your compiled React app.

#### Step 3: Test Production Build
```bash
npm run preview
```
This builds and serves the app locally on port 3000 to test if everything works.

### Deployment to Railway:

#### Option A: Using Railway CLI
```bash
# Install Railway CLI
npm install -g railway

# Login to Railway
railway login

# Create new project
railway init

# Set environment variables on Railway
railway variables set VITE_API_BASE_URL=https://skinsenseback-production.up.railway.app

# Deploy
railway up
```

#### Option B: Using Railway Dashboard
1. Go to [railway.app](https://railway.app)
2. Login to your account
3. Create new project → Deploy from GitHub
4. Connect your GitHub repository
5. Select this repository branch
6. Configure variables:
   - **VITE_API_BASE_URL**: `https://skinsenseback-production.up.railway.app`
7. Deploy

### Key Configuration Points:

1. **Port**: Railway automatically assigns a PORT. The Procfile uses port 3000 by default (`serve -l 3000 -s dist`)
   - To use Railway's assigned port instead, modify Procfile to:
   ```
   web: npm run build && serve -l ${PORT:-3000} -s dist
   ```

2. **API Connection**:
   - The frontend connects to your backend at: `https://skinsenseback-production.up.railway.app`
   - This is configured in `.env.production`
   - Verify your backend CORS settings allow requests from your Railway frontend URL

3. **Build Process**:
   - Vite will build your React app into the `dist/` folder
   - Railway serves this static folder using the `serve` package

### Troubleshooting:

#### Issue: "Cannot connect to API"
- **Solution**: Check if your backend URL is correct in `.env.production`
- Verify backend CORS settings include your frontend URL

#### Issue: "Port already in use"
- **Solution**: Use Railway's PORT environment variable (see "Key Configuration Points" #1)

#### Issue: "Module not found"
- **Solution**: Run `npm install` to ensure all dependencies are installed

#### Issue: ".env variables not loading"
- **Solution**: Ensure you've set variables in Railway dashboard
- Vite only exposes variables prefixed with `VITE_`

### Environment Variables Setup in Railway:

In Railway dashboard, add these variables:
```
VITE_API_BASE_URL=https://skinsenseback-production.up.railway.app
```

### Post-Deployment Verification:

1. Visit your Railway app URL
2. Test login functionality
3. Verify API calls are reaching your backend
4. Check browser console for CORS or connection errors
5. Test on mobile devices to ensure responsiveness

### Local Development:

Still use:
```bash
npm run dev
```

This runs Vite with hot reload and uses the local proxy to `localhost:8050` (your local backend).

### Build & Production Preview:

```bash
npm run build    # Build for production
npm start        # Serve production build locally
```

### File Structure After Deployment:
```
project/
├── dist/                    # Built app (served by Railway)
├── src/                     # Source code
├── package.json            
├── vite.config.ts          
├── Procfile                # Railway deployment config
├── .env.production         # Production variables
└── .env                    # Development variables
```

## Deployment Checklist:
- [ ] Run `npm install` to update dependencies (including `serve`)
- [ ] Run `npm run build` to test build locally
- [ ] Update Procfile if needed for PORT handling
- [ ] Verify `.env.production` has correct backend URL
- [ ] Push code to GitHub
- [ ] Deploy via Railway dashboard or CLI
- [ ] Set environment variables in Railway dashboard
- [ ] Verify frontend loads
- [ ] Test login and API calls
- [ ] Check browser console for errors

## Backend CORS Configuration Needed:

Make sure your backend (`https://skinsenseback-production.up.railway.app`) has CORS configured to accept:
- Your Railway frontend URL (e.g., `https://your-app-name.railway.app`)
- Requests with credentials if needed

## Questions?

- Check Vite docs: https://vitejs.dev/
- Check Railway docs: https://railway.app/docs
- Check Express CORS: https://expressjs.com/en/resources/middleware/cors.html
