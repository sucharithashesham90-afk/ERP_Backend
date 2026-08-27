-- Legacy NOT NULL "name" columns that no entity maps any more.
--
-- These tables were created by an earlier schema that stored the label in `name`. The entities now
-- use a specific column (`method_name`, `contractor_name`, …), and ddl-auto=update adds the new
-- column but never drops the old one — so every insert failed the leftover NOT NULL with
-- "Required field is missing: name", even though the form had been filled in correctly.
--
-- Backfill from the current column so any existing row keeps a sensible label, then drop the
-- constraint. The column itself is left in place: dropping data is not something a startup
-- migration should do unasked.

DO $$
DECLARE
    t   text;
    src text;
    pairs text[][] := ARRAY[
        ['pricing_methods',         'method_name'],
        ['agri_hamali_contractors', 'contractor_name'],
        ['season_periods',          'period_name']
    ];
    i int;
BEGIN
    FOR i IN 1 .. array_length(pairs, 1) LOOP
        t   := pairs[i][1];
        src := pairs[i][2];

        -- Only touch a table that exists and actually carries both columns.
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = t AND column_name = 'name')
        THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = t AND column_name = src)
            THEN
                EXECUTE format('UPDATE %I SET name = %I WHERE name IS NULL AND %I IS NOT NULL', t, src, src);
            END IF;

            EXECUTE format('UPDATE %I SET name = ''(unnamed)'' WHERE name IS NULL', t);
            EXECUTE format('ALTER TABLE %I ALTER COLUMN name DROP NOT NULL', t);
            RAISE NOTICE 'Relaxed legacy NOT NULL on %.name', t;
        END IF;
    END LOOP;
END $$;
