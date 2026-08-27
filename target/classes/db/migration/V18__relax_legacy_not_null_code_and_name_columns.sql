-- Legacy NOT NULL `code` / `name` columns that no entity maps any more.
--
-- Same failure V12 fixed for three `name` columns, on tables it did not cover. The entity writes a
-- specific column (`currency_code`, `currency_name`, `stage_name`) and ddl-auto=update adds the new
-- column but never drops the old one — so on any environment whose schema predates the rename, the
-- leftover NOT NULL rejects every insert with "Required field is missing: code", even though the
-- form was filled in correctly.
--
-- Currency Definition was unsaveable on Railway for exactly this reason: `currencies` carries both
-- `code` and `currency_code`, and only the second is ever written. `currency_name`/`name` is the
-- same pair, so it would have broken the moment `code` stopped breaking — both go together.
--
-- Deliberately minimal: static DDL, no dynamic identifiers, no loops, and no backfill. Nothing
-- reads these columns, so copying values into them would be cosmetic risk for no gain, and this
-- runs at startup where a failure costs a boot. Each statement is guarded because a fresh schema
-- has no legacy column at all, and DROP NOT NULL on an already-nullable column is a no-op.
-- Guards are scoped to current_schema(): this database holds more than one ERP schema, and the
-- unqualified ALTER resolves through search_path.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema()
                 AND table_name = 'currencies' AND column_name = 'code') THEN
        ALTER TABLE currencies ALTER COLUMN code DROP NOT NULL;
        RAISE NOTICE 'Relaxed legacy NOT NULL on currencies.code';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema()
                 AND table_name = 'currencies' AND column_name = 'name') THEN
        ALTER TABLE currencies ALTER COLUMN name DROP NOT NULL;
        RAISE NOTICE 'Relaxed legacy NOT NULL on currencies.name';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema()
                 AND table_name = 'seed_production_stages' AND column_name = 'name') THEN
        ALTER TABLE seed_production_stages ALTER COLUMN name DROP NOT NULL;
        RAISE NOTICE 'Relaxed legacy NOT NULL on seed_production_stages.name';
    END IF;
END $$;
