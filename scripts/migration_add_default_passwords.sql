-- Migration: Add default password for existing guides
-- 
-- PREREQUISITE: Run 01_add_password_column.sql FIRST!
-- 
-- This sets password = "password123" (BCrypt hashed) for any guides without a password
-- 
-- Run this in pgAdmin or via command line:
-- docker exec -it toursmanager-postgres-1 psql -U postgres -d toursmanager -f /path/to/this/file.sql

-- The BCrypt hash below is for password: "password123"
-- Generated using: new BCryptPasswordEncoder().encode("password123")
UPDATE guides 
SET password = '$2a$10$N9qo8uLOickgx2ZoXn/4qO6PrKqFz/G5pV3YaHwKvUh3uT2f5hPfG'
WHERE password IS NULL;

-- Check the results
SELECT 
    id, 
    first_name, 
    last_name, 
    email,
    CASE 
        WHEN password IS NULL THEN 'No password (ERROR!)' 
        ELSE 'Has password ✓' 
    END as password_status
FROM guides
ORDER BY id;

-- To verify: All existing guides can now login with:
--   email: their existing email
--   password: password123
