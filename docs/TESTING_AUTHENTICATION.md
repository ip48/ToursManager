# Testing Authentication Flow

## Services Running

- **Backend**: http://localhost:8080
- **Frontend**: http://localhost:3000
- **Database**: PostgreSQL on localhost:5432

## Test Flow

### 1. Register a New Guide

1. Open browser: http://localhost:3000
2. Click **"Register"** in navigation
3. Fill out form:
   - First Name: Alice
   - Last Name: Guide
   - Email: alice@test.com
   - Password: password123
   - Languages: Select English, Spanish
4. Click **"Create Account"**
5. ✅ Should redirect to home page logged in

### 2. Logout

1. Click **"Logout"** button in navbar
2. ✅ Should see "Login or Register to get started"

### 3. Login

1. Click **"Login"** in navigation
2. Enter credentials:
   - Email: alice@test.com
   - Password: password123
3. Click **"Sign In"**
4. ✅ Should redirect to home page logged in

### 4. Protected Routes

1. While logged out, try to visit: http://localhost:3000/guides
2. ✅ Should redirect to /login
3. Login first, then visit: http://localhost:3000/guides
4. ✅ Should show Guides page

## Test with curl (Backend Only)

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob",
    "lastName": "Guide",
    "email": "bob@test.com",
    "password": "password123",
    "languages": "en,fr"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@test.com",
    "password": "password123"
  }'
```

### Access Protected Endpoint
```bash
# Copy token from login response
TOKEN="<your-jwt-token>"

curl -X GET http://localhost:8080/api/guides/profile \
  -H "Authorization: Bearer $TOKEN"
```

## Expected Behavior

✅ **Registration**: Returns JWT token + user info
✅ **Login**: Returns JWT token + user info
✅ **Logout**: Clears localStorage
✅ **Protected Routes**: Requires authentication
✅ **Token Storage**: Persists across page refreshes
✅ **Navigation**: Shows/hides menu items based on auth state

## Troubleshooting

### Backend not starting?
```bash
# Check if port 8080 is in use
lsof -i :8080

# Check backend logs
mvn spring-boot:run
```

### Frontend not starting?
```bash
# Check if port 3000 is in use
lsof -i :3000

# Restart frontend
cd frontend
npm run dev
```

### Database issues?
```bash
# Check if PostgreSQL is running
docker ps

# Restart database
docker compose up -d postgres
```
