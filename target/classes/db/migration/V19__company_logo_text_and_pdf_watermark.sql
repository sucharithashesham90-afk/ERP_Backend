-- Company logo as text, plus the PDF watermark switch.
--
-- companies.logo was varchar(500), which could hold a URL and nothing else. Storing an uploaded
-- mark means a base64 data URI running to tens of thousands of characters, so the column has to be
-- text. Hibernate's ddl-auto=update does not reliably widen an existing column's type — it adds
-- columns but leaves existing ones alone — so the change is made here rather than left to chance.
--
-- pdf_watermark_enabled defaults to true: a document leaving the system should say whose it is.
-- Existing rows are backfilled to true for the same reason.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema()
                 AND table_name = 'companies' AND column_name = 'logo') THEN
        ALTER TABLE companies ALTER COLUMN logo TYPE text;
        RAISE NOTICE 'companies.logo widened to text';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = current_schema()
                     AND table_name = 'companies' AND column_name = 'pdf_watermark_enabled') THEN
        ALTER TABLE companies ADD COLUMN pdf_watermark_enabled boolean;
        RAISE NOTICE 'companies.pdf_watermark_enabled added';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema()
                 AND table_name = 'companies' AND column_name = 'pdf_watermark_enabled') THEN
        UPDATE companies SET pdf_watermark_enabled = true WHERE pdf_watermark_enabled IS NULL;
        ALTER TABLE companies ALTER COLUMN pdf_watermark_enabled SET DEFAULT true;
        ALTER TABLE companies ALTER COLUMN pdf_watermark_enabled SET NOT NULL;
    END IF;
END $$;
