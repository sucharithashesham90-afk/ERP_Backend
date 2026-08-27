-- Columns behind fields the screens were already sending but had nowhere to land.
--
-- Each of these was the same defect wearing a different hat: the form collected a value, posted it,
-- and Jackson dropped it because no property existed to bind to. Nothing reported an error, so the
-- value simply was not there the next time the record was opened.
--
-- auto-DDL would add these too, but stating them here means a fresh database and an existing one
-- end up the same shape, and the reason is written down next to the change.

-- Process steps: the crops a step applies to. The screen has always sent a `crops` array.
ALTER TABLE process_steps
    ADD COLUMN IF NOT EXISTS crops TEXT;

-- Processing lines: the ids behind the Processing Plant and Godown dropdowns. Only the names were
-- stored, so a saved line reopened with both dropdowns blank - the values were on the row, just not
-- in a form the form could match back to an option.
ALTER TABLE processing_lines
    ADD COLUMN IF NOT EXISTS location_id  UUID,
    ADD COLUMN IF NOT EXISTS warehouse_id UUID;

-- Hamali contractors: the rest of the address. State, district and mandal keep both the id, which
-- is what the cascading dropdowns reopen against, and the name, which is what a printed docket
-- needs without three further lookups.
ALTER TABLE agri_hamali_contractors
    ADD COLUMN IF NOT EXISTS state_id      UUID,
    ADD COLUMN IF NOT EXISTS state_name    VARCHAR(150),
    ADD COLUMN IF NOT EXISTS district_id   UUID,
    ADD COLUMN IF NOT EXISTS district_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS mandal_id     UUID,
    ADD COLUMN IF NOT EXISTS mandal_name   VARCHAR(150),
    ADD COLUMN IF NOT EXISTS zip_code      VARCHAR(20);

-- Backfill the two processing-line ids from the names already stored, so lines saved before this
-- change reopen with their plant and godown filled in rather than looking newly blank.
UPDATE processing_lines pl
   SET location_id = al.id
  FROM admin_locations al
 WHERE pl.location_id IS NULL
   AND pl.location_name IS NOT NULL
   AND al.tenant_id = pl.tenant_id
   AND al.deleted_at IS NULL
   AND al.name = pl.location_name;
