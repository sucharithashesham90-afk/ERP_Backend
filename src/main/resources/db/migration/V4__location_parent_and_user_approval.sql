-- V4: Location parent linkage + user approval workflow

-- 1. Link Processing Plants to their parent Production Area
ALTER TABLE admin_locations
    ADD COLUMN IF NOT EXISTS parent_id   UUID         REFERENCES admin_locations(id),
    ADD COLUMN IF NOT EXISTS parent_name VARCHAR(200);

CREATE INDEX IF NOT EXISTS idx_adminloc_parent ON admin_locations(tenant_id, parent_id);

-- 2. User approval workflow columns
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS reporting_manager_id   UUID,
    ADD COLUMN IF NOT EXISTS reporting_manager_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS approval_remarks        VARCHAR(500),
    ADD COLUMN IF NOT EXISTS approved_by             UUID,
    ADD COLUMN IF NOT EXISTS approved_at             TIMESTAMP;

-- 3. Extend user status to include PENDING_APPROVAL and REJECTED
--    (JPA @Enumerated(STRING) stores as text; no enum type in PostgreSQL by default)
--    Existing CHECK constraint must be dropped/replaced if it exists.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'users' AND constraint_type = 'CHECK'
          AND constraint_name LIKE '%status%'
    ) THEN
        EXECUTE (
            SELECT 'ALTER TABLE users DROP CONSTRAINT ' || constraint_name
            FROM information_schema.table_constraints
            WHERE table_name = 'users' AND constraint_type = 'CHECK'
              AND constraint_name LIKE '%status%'
            LIMIT 1
        );
    END IF;
END $$;
