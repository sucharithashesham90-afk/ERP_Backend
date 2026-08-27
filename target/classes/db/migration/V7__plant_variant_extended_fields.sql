-- V7: Add Variety Details tab fields to plant_variants

ALTER TABLE plant_variants
    ADD COLUMN IF NOT EXISTS hybrid                  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS transgenic              BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS max_process_loss        NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS min_process_loss        NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS in_wt_per_moisture      NUMERIC(10,4),
    ADD COLUMN IF NOT EXISTS sow_harvest_prd         INTEGER,
    ADD COLUMN IF NOT EXISTS isolation_dist          NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS max_off_types_count     INTEGER,
    ADD COLUMN IF NOT EXISTS max_selfs_count         INTEGER,
    ADD COLUMN IF NOT EXISTS plant_product_weight    NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS year_of_release         INTEGER,
    ADD COLUMN IF NOT EXISTS screens                 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS characteristics         TEXT,
    ADD COLUMN IF NOT EXISTS product_wise_sales_plan BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS division                VARCHAR(200),
    ADD COLUMN IF NOT EXISTS notification_number     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS treatments              TEXT;
