# Database Migration Scripts

## Migration Steps (Run in Order!)

### Step 1: Add password column to guides table

**From terminal:**
```bash
docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager -f /workspaces/ToursManager/scripts/01_add_password_column.sql
```

**Or in pgAdmin:**
```sql
ALTER TABLE guides 
ADD COLUMN IF NOT EXISTS password VARCHAR(255);
```

### Step 2: Add Default Password to Existing Guides

**Quick Command (from terminal):**

```bash
docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager -c "UPDATE guides SET password = '\$2a\$10\$N9qo8uLOickgx2ZoXn/4qO6PrKqFz/G5pV3YaHwKvUh3uT2f5hPfG' WHERE password IS NULL;"
```

### Or use pgAdmin:

1. Open pgAdmin
2. Connect to your database
3. Open Query Tool
4. Paste and run:

```sql
UPDATE guides 
SET password = '$2a$10$N9qo8uLOickgx2ZoXn/4qO6PrKqFz/G5pV3YaHwKvUh3uT2f5hPfG'
WHERE password IS NULL;
```

### What this does:

- Sets password to **"password123"** for any guides without a password
- The hash `$2a$10$N9q...hPfG` is the BCrypt-encrypted version
- After running, all existing guides can login with `password123`

### Check results:

```sql
SELECT 
    id, 
    first_name, 
    last_name, 
    email,
    CASE 
        WHEN password IS NULL THEN 'No password' 
        ELSE 'Has password' 
    END as status
FROM guides;
```

---

## Alternative: Just Delete and Start Fresh

If you want to start completely fresh:

```bash
docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager -c "DELETE FROM guide_languages; DELETE FROM guides;"
```

Then register new guides using `/api/auth/register`.
