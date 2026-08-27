-- V5: Extend villages table with geo hierarchy, production area, incharge, and landmark fields

ALTER TABLE villages
    ADD COLUMN IF NOT EXISTS village_code          VARCHAR(30),
    ADD COLUMN IF NOT EXISTS zip                   VARCHAR(10),
    ADD COLUMN IF NOT EXISTS state_id              UUID,
    ADD COLUMN IF NOT EXISTS state_name            VARCHAR(200),
    ADD COLUMN IF NOT EXISTS district_id           UUID,
    ADD COLUMN IF NOT EXISTS district_name         VARCHAR(200),
    ADD COLUMN IF NOT EXISTS mandal_id             UUID,
    ADD COLUMN IF NOT EXISTS mandal_name           VARCHAR(200),
    ADD COLUMN IF NOT EXISTS production_area_id    UUID,
    ADD COLUMN IF NOT EXISTS production_area_name  VARCHAR(200),
    ADD COLUMN IF NOT EXISTS incharge_ids          TEXT,
    ADD COLUMN IF NOT EXISTS incharge_names        TEXT,
    ADD COLUMN IF NOT EXISTS telegraph_office      VARCHAR(200),
    ADD COLUMN IF NOT EXISTS nearest_railway_stn   VARCHAR(200),
    ADD COLUMN IF NOT EXISTS nearest_post_office   VARCHAR(200),
    ADD COLUMN IF NOT EXISTS nearest_town          VARCHAR(200);
