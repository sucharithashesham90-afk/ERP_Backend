-- Make the dispatch count columns safe to read back.
--
-- packageCount, secondaryPackCount and totalPackages are primitive `int` on the entities. Hibernate
-- cannot put a NULL into a primitive: reading a row whose column is NULL throws
-- PropertyAccessException, and because the list query maps a whole page at once, ONE such row makes
-- the entire dispatch list fail with a 500. Writing is unaffected - an insert always supplies a
-- value - which is why dispatches could be created and then not appear in the grid.
--
-- secondary_pack_count arrived later than the rows around it (added with the secondary packing
-- fields), and auto-DDL adds a column to a populated table without NOT NULL and without a default,
-- so every dispatch item that existed beforehand got a NULL. total_packages and package_count are
-- covered here too: the same thing happens to any of them on a database where the column was added
-- after the fact.
--
-- Fix the data, then make the column unable to hold NULL again so this cannot recur.

-- The columns may not exist at all on a database where auto-DDL failed to add them.
ALTER TABLE dispatch_items
    ADD COLUMN IF NOT EXISTS package_count        INTEGER,
    ADD COLUMN IF NOT EXISTS secondary_pack_count INTEGER;

ALTER TABLE dispatches
    ADD COLUMN IF NOT EXISTS total_packages INTEGER;

-- A missing count means "none recorded", which is zero. package_count defaults to 1 on the entity
-- because an item is at least one package, so an existing row with no count is treated the same way
-- rather than being silently turned into zero packages.
UPDATE dispatch_items SET package_count        = 1 WHERE package_count        IS NULL;
UPDATE dispatch_items SET secondary_pack_count = 0 WHERE secondary_pack_count IS NULL;
UPDATE dispatches     SET total_packages       = 0 WHERE total_packages       IS NULL;

ALTER TABLE dispatch_items
    ALTER COLUMN package_count        SET DEFAULT 1,
    ALTER COLUMN package_count        SET NOT NULL,
    ALTER COLUMN secondary_pack_count SET DEFAULT 0,
    ALTER COLUMN secondary_pack_count SET NOT NULL;

ALTER TABLE dispatches
    ALTER COLUMN total_packages SET DEFAULT 0,
    ALTER COLUMN total_packages SET NOT NULL;
