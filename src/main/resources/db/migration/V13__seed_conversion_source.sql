-- Mark the seed conversions that process jobs wrote, so the Seed Conversion screen can leave them out.
--
-- Until now the only thing separating an automatic record from one an operator typed was the
-- wording of its note, so the screen listed every stage transition the plant had run alongside the
-- handful of entries somebody actually made. New records carry a source; these are the ones already
-- in the table.
--
-- Nothing is deleted. An automatic conversion is a real audit of what became what, and a graded lot
-- that changed state with no trace would be worse than a cluttered screen — it just belongs behind
-- includeAutomatic=true rather than in front of the person recording a conversion by hand.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'seed_conversions' AND column_name = 'source')
    THEN
        -- Written by ManufacturingService on process-job completion; the note has always started
        -- with this marker, which is what makes the existing rows identifiable at all.
        UPDATE seed_conversions
           SET source = 'PROCESS_JOB'
         WHERE source IS NULL
           AND notes LIKE 'Auto: Process Job %';

        -- Everything else in the table predates the automatic writer or came from the screen.
        UPDATE seed_conversions
           SET source = 'MANUAL'
         WHERE source IS NULL;

        RAISE NOTICE 'seed_conversions.source backfilled';
    END IF;
END $$;
