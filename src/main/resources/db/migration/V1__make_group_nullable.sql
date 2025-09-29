-- Migration: Make expenses.group_id nullable and ensure FK allows NULLs
-- Run this on the application's MySQL database (use your DB client or `mysql` CLI).
-- It will drop an existing foreign-key constraint (if any), make the column nullable,
-- then re-add a foreign key that sets group_id to NULL when the referenced group is deleted.

-- Note: MySQL does not allow IF statements outside stored routines, so we use a
-- dynamic PREPARE to drop the FK if it exists. This script is safe to run multiple times.

-- Get the foreign key constraint name (if present)
SELECT CONSTRAINT_NAME INTO @fk_name
FROM information_schema.key_column_usage
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'expenses'
  AND COLUMN_NAME = 'group_id'
  AND REFERENCED_TABLE_NAME IS NOT NULL
LIMIT 1;

SET @drop_stmt = IF(@fk_name IS NULL,
    'SELECT 1;',
    CONCAT('ALTER TABLE expenses DROP FOREIGN KEY ', @fk_name));

PREPARE stmt FROM @drop_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Make the column nullable
ALTER TABLE expenses
  MODIFY COLUMN group_id BIGINT NULL;

-- Re-add a named foreign key (idempotent if previous was dropped)
-- Use a deterministic name to make future maintenance easier.
ALTER TABLE expenses
  ADD CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES expense_groups(id) ON DELETE SET NULL;

-- End of migration
