# Authentication Migration Guide

## What Changed

We added JWT authentication to the ToursManager application. This required adding a `password` field to the `guides` table.

## Impact on Existing Data

**Before authentication:**
- Guides registered via `POST /api/guides` (no password)
- Anyone could view/edit any guide

**After authentication:**
- New guides register via `POST /api/auth/register` (with password)
- Guides must login to edit their own profile
- Old guides in database have `password = NULL`

## Migration Strategy

### Option 1: Allow Null Passwords (Current Implementation)

**What we did:**
```java
// Guide.java
@Column(nullable = true)  // Allow null for existing data
private String password;
```

**Result:**
- ✅ Existing guides remain in database
- ✅ Old endpoint `POST /api/guides` still works (backward compatible)
- ❌ Existing guides **cannot login** (no password)
- ⚠️ Existing guides need to "claim" their account

**How existing guides can set password:**

Create an endpoint to set password for existing guides:

```java
@PostMapping("/claim-account")
public ResponseEntity<?> claimAccount(@RequestBody ClaimAccountRequest request) {
    // 1. Find guide by email
    Guide guide = guideRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Guide not found"));
    
    // 2. Check if password already set
    if (guide.getPassword() != null) {
        return ResponseEntity.badRequest().body("Account already claimed");
    }
    
    // 3. Send verification email (TODO: implement email service)
    // emailService.sendVerificationCode(guide.getEmail(), verificationCode);
    
    // 4. Verify code and set password
    guide.setPassword(passwordEncoder.encode(request.getPassword()));
    guideRepository.save(guide);
    
    return ResponseEntity.ok("Account claimed successfully");
}
```

---

### Option 2: Set Default Password for Existing Guides

**Create a migration script:**

```sql
-- Set a temporary password for all existing guides without passwords
UPDATE guides 
SET password = '$2a$10$...'  -- BCrypt hash of "ChangeMe123!"
WHERE password IS NULL;
```

**Then notify users:**
- Email: "We've added authentication. Your temporary password is: ChangeMe123!"
- Force password change on first login

---

### Option 3: Delete Existing Data (Clean Slate)

**If this is dev/test environment:**

```sql
-- Delete all guides and start fresh
DELETE FROM guide_languages;
DELETE FROM guides;
```

**Then:**
- All new guides use `POST /api/auth/register`
- Everyone has passwords from the start

---

## Current Database State

### Check if you have existing guides:

```sql
-- Connect to database
docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager

-- Count guides
SELECT COUNT(*) FROM guides;

-- Check which guides have no password
SELECT id, first_name, last_name, email, 
       CASE WHEN password IS NULL THEN 'No password' ELSE 'Has password' END as status
FROM guides;
```

---

## Recommended Steps

### For Development (Choose One):

**A. Fresh Start (Easiest)**
```sql
-- Delete all data
DELETE FROM guide_languages;
DELETE FROM guides;
DELETE FROM languages;
```

Then re-register guides using `/api/auth/register`.

**B. Keep Existing Data**
- Leave guides in database with `password = NULL`
- Document that old guides can't login
- Add "Claim Account" endpoint later if needed

---

### For Production (Future):

1. **Before deploying authentication:**
   - Announce to all guides: "Authentication coming soon"
   - Explain they'll need to claim their accounts

2. **Deploy authentication:**
   - Existing guides have `password = NULL`
   - They see: "Account needs password. Click here to claim."

3. **Add claim account flow:**
   - Guide enters email
   - System sends verification email
   - Guide sets password
   - Can now login

---

## API Endpoint Summary

### Public Endpoints (no authentication):
- `POST /api/auth/register` - New guides register with password
- `POST /api/auth/login` - Guides login
- `GET /api/guides` - View all guides (public directory)
- `GET /api/guides/{id}` - View specific guide
- `POST /api/guides` - **DEPRECATED** - Old registration (no password)

### Protected Endpoints (requires JWT token):
- `GET /api/guides/profile` - Get my profile
- `PUT /api/guides/profile` - Update my profile

---

## Testing the Migration

### 1. Check current guides:
```bash
curl http://localhost:8080/api/guides
```

### 2. Register new guide WITH password:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "New",
    "email": "alice@example.com",
    "password": "SecurePass123",
    "languages": "en,es"
  }'
```

### 3. Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'
```

### 4. Try to login with old guide (should fail):
```bash
# If you have an old guide without password
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "old.guide@example.com",
    "password": "anything"
  }'
# Result: "Guide not found" or "Invalid password"
```

---

## Decision Needed

**For your project, I recommend:**

Since this is a learning project and likely in development:

**Clean Slate Approach:**
1. Delete existing guides
2. Use only `/api/auth/register` going forward
3. All guides have passwords from the start

**Command:**
```sql
docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager -c "
DELETE FROM guide_languages;
DELETE FROM guides;
"
```

**Want me to help you decide or implement one of these options?**
