-- ================================================================
-- V6: Organizer extended fields + FieldProducer extended fields
-- ================================================================

-- ORGANIZER: drop old generic columns, add full field set
ALTER TABLE organizers
    ADD COLUMN IF NOT EXISTS office_address        VARCHAR(500),
    ADD COLUMN IF NOT EXISTS plant_address         VARCHAR(500),
    ADD COLUMN IF NOT EXISTS village_id            UUID,
    ADD COLUMN IF NOT EXISTS village_name          VARCHAR(200),
    ADD COLUMN IF NOT EXISTS state_id              UUID,
    ADD COLUMN IF NOT EXISTS state_name            VARCHAR(200),
    ADD COLUMN IF NOT EXISTS district_id           UUID,
    ADD COLUMN IF NOT EXISTS district_name         VARCHAR(200),
    ADD COLUMN IF NOT EXISTS zip                   VARCHAR(10),
    ADD COLUMN IF NOT EXISTS office_phone          VARCHAR(20),
    ADD COLUMN IF NOT EXISTS plant_phone           VARCHAR(20),
    ADD COLUMN IF NOT EXISTS relationship_with_company VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pan_no                VARCHAR(20),
    ADD COLUMN IF NOT EXISTS tin_no                VARCHAR(20),
    ADD COLUMN IF NOT EXISTS vat_no                VARCHAR(20),
    ADD COLUMN IF NOT EXISTS sst_no                VARCHAR(20),
    ADD COLUMN IF NOT EXISTS adhar_no              VARCHAR(20),
    ADD COLUMN IF NOT EXISTS own_godown_sqft       NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS hired_godown_sqft     NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS own_machine_tph       NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS hired_machine_tph     NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS reg_cert_no           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS seed_license_no       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS seed_license_validity DATE,
    ADD COLUMN IF NOT EXISTS seed_cert_agency      VARCHAR(200),
    ADD COLUMN IF NOT EXISTS plant_reg_no          VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bank_account_no       VARCHAR(50),
    ADD COLUMN IF NOT EXISTS bank_branch           VARCHAR(200),
    ADD COLUMN IF NOT EXISTS is_supplier           BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS production_area_ids   TEXT,
    ADD COLUMN IF NOT EXISTS production_area_names TEXT,
    ADD COLUMN IF NOT EXISTS experience_crops      TEXT,
    ADD COLUMN IF NOT EXISTS account_heads         TEXT;

-- FIELD_PRODUCERS: add missing fields
ALTER TABLE field_producers
    ADD COLUMN IF NOT EXISTS father_name           VARCHAR(150),
    ADD COLUMN IF NOT EXISTS village_id            UUID,
    ADD COLUMN IF NOT EXISTS village_name          VARCHAR(200),
    ADD COLUMN IF NOT EXISTS adhar_no              VARCHAR(20),
    ADD COLUMN IF NOT EXISTS folio_no              VARCHAR(50),
    ADD COLUMN IF NOT EXISTS is_shareholder        BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_organizer          BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS bank_branch           VARCHAR(200);
