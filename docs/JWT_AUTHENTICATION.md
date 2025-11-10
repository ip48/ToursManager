# JWT Authentication - Testing Guide

## What We Built

✅ **Backend JWT Authentication System:**
- BCrypt password hashing
- JWT token generation and validation
- Authenticated endpoints
- Public and protected routes

## API Endpoints

### 1. Register (Public)
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "phoneNumber": "+1234567890",
  "profile": "Experienced tour guide",
  "languages": "en,es,fr"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 2. Login (Public)
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 3. Get My Profile (Protected - Requires JWT)
```bash
GET http://localhost:8080/api/guides/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "profile": "Experienced tour guide",
  "languages": "en,es,fr",
  "active": true,
  "createdAt": "2025-11-03T10:30:00",
  "updatedAt": "2025-11-03T10:30:00"
}
```

### 4. Update My Profile (Protected - Requires JWT)
```bash
PUT http://localhost:8080/api/guides/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "profile": "UPDATED: Expert tour guide with 10 years experience",
  "languages": "en,es,fr,de",
  "active": true
}
```

## How JWT Authentication Works

```
┌─────────────────────────────────────────────────────────┐
│  1. REGISTER or LOGIN                                   │
│     POST /api/auth/register or /api/auth/login          │
│     → Password hashed with BCrypt                       │
│     → JWT token generated (contains email)              │
│     → Token returned to client                          │
└─────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────┐
│  2. CLIENT STORES TOKEN                                 │
│     → localStorage.setItem('token', response.token)     │
└─────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────┐
│  3. AUTHENTICATED REQUESTS                              │
│     GET /api/guides/profile                             │
│     Headers: { Authorization: "Bearer <token>" }        │
│     → JwtAuthenticationFilter extracts token            │
│     → Token validated                                   │
│     → Email extracted from token                        │
│     → Spring Security context set                       │
│     → Controller gets email from SecurityContext        │
└─────────────────────────────────────────────────────────┘
```

## Testing with curl

### 1. Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "password123",
    "languages": "en,es"
  }'
```

**Save the token from the response!**

### 2. Get Profile (use your token)
```bash
curl -X GET http://localhost:8080/api/guides/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3. Update Profile
```bash
curl -X PUT http://localhost:8080/api/guides/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "profile": "Updated profile!",
    "languages": "en,es,fr"
  }'
```

## Security Features

✅ **Password Security:**
- BCrypt hashing (automatically salted)
- Passwords never stored in plain text
- Slow by design (protects against brute force)

✅ **JWT Security:**
- Token signed with secret key
- Cannot be tampered with (signature verification)
- Contains expiration time (24 hours)
- Stateless (no server-side session storage)

✅ **Endpoint Protection:**
- Public: `/api/auth/**`, `/api/guides` (view only)
- Protected: `/api/guides/profile` (requires JWT)
- Filter validates token on every request

## What Password Hashing Looks Like

**Plain text:** `password123`

**BCrypt hash:** `$2a$10$N9qo8uLOickgx2ZMRZoMye/1Zy8LDCl6P4Z4wLX9H3kx0X0X0X0X0`

Components:
- `$2a$` - BCrypt algorithm version
- `10$` - Cost factor (2^10 iterations)
- Next 22 chars - Salt (random, unique per password)
- Rest - Actual hash

## Next Steps

1. ✅ Backend JWT authentication complete
2. ⬜ Build frontend login page
3. ⬜ Store JWT token in localStorage
4. ⬜ Add Authorization header to API calls
5. ⬜ Build profile edit page (authenticated)
6. ⬜ Add logout (clear token from localStorage)

## Important Notes

- Token expires in 24 hours (configurable in application.properties)
- In production, use environment variable for JWT_SECRET
- Change JWT_SECRET to a strong random value
- HTTPS required in production (JWT sent in headers)
