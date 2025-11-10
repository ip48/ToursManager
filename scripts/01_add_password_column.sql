-- Migration: Add password column to guides table
-- Run this FIRST before setting default passwords

-- Step 1: Add password column (allow NULL initially for existing records)
ALTER TABLE guides 
ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Step 2: Verify the column was added
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'guides' AND column_name = 'password';

-- Step 3: Check current guides
SELECT id, first_name, last_name, email, password
FROM guides;
