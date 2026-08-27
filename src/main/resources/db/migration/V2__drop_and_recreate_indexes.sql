-- Drop idx_* constraints first (they back same-named indexes in PostgreSQL).
-- Then drop remaining idx_* indexes. Flyway runs before Hibernate DDL so
-- indexes are absent when Hibernate runs, preventing 'relation already exists'.
DO $$
DECLARE
  r RECORD;
BEGIN
  -- Step 1: drop unique/exclusion constraints named idx_* (backs the index)
  FOR r IN
    SELECT tc.table_name, tc.constraint_name
    FROM information_schema.table_constraints tc
    WHERE tc.constraint_schema = 'public'
      AND tc.constraint_name LIKE 'idx\_%' ESCAPE '\'
  LOOP
    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I',
                   r.table_name, r.constraint_name);
  END LOOP;

  -- Step 2: drop any remaining idx_* indexes not backing a constraint
  FOR r IN
    SELECT indexname
    FROM pg_indexes
    WHERE schemaname = 'public'
      AND indexname LIKE 'idx\_%' ESCAPE '\'
      AND indexname NOT LIKE '%_pkey'
  LOOP
    EXECUTE 'DROP INDEX IF EXISTS ' || quote_ident(r.indexname);
  END LOOP;
END $$;
