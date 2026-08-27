-- change_password_on_login was added without a DEFAULT, leaving existing rows
-- as NULL. Primitive boolean in User entity cannot hold null → startup crash.
-- Backfill to false and enforce NOT NULL so the primitive mapping always works.
UPDATE users SET change_password_on_login = false WHERE change_password_on_login IS NULL;
ALTER TABLE users ALTER COLUMN change_password_on_login SET DEFAULT false;
ALTER TABLE users ALTER COLUMN change_password_on_login SET NOT NULL;
