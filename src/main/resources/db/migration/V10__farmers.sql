CREATE TABLE IF NOT EXISTS agri_farmers (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    fathers_name                VARCHAR(200),
    nearest_rly_stn             VARCHAR(200),
    telegraph_off               VARCHAR(200),
    state_id                    UUID,
    state_name                  VARCHAR(200),
    district_id                 UUID,
    district_name               VARCHAR(200),
    zip                         VARCHAR(10),
    farmer_code                 VARCHAR(50),
    is_shareholder              BOOLEAN NOT NULL DEFAULT FALSE,
    folio_no                    VARCHAR(50),
    mobile_no                   VARCHAR(20),
    farmer_type                 VARCHAR(100),
    account_heads               TEXT,
    adhar_card                  VARCHAR(20),
    bank_name                   VARCHAR(100),
    bank_account_no             VARCHAR(50),
    bank_ifsc                   VARCHAR(20),
    bank_branch                 VARCHAR(200),
    document_type               VARCHAR(100),
    village_id                  UUID,
    village_name                VARCHAR(200),
    distance_from_highway       NUMERIC(8,2),
    distance_from_highway_unit  VARCHAR(10),
    nearest_town                VARCHAR(200),
    taluka_or_tehsil            VARCHAR(200),
    active                      BOOLEAN NOT NULL DEFAULT TRUE,
    is_organizer                BOOLEAN NOT NULL DEFAULT FALSE,
    phone_number                VARCHAR(20),
    distance_from_plant         NUMERIC(8,2),
    distance_from_plant_unit    VARCHAR(10),
    created_at                  TIMESTAMP,
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(100),
    updated_by                  VARCHAR(100),
    deleted_at                  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_farmer_tenant ON agri_farmers(tenant_id);

CREATE TABLE IF NOT EXISTS agri_farmer_land_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    farmer_id       UUID NOT NULL,
    village_id      UUID,
    village_name    VARCHAR(200),
    plot_survey_no  VARCHAR(100),
    acreage         NUMERIC(10,3),
    land_type       VARCHAR(100),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_farmer_land_tenant ON agri_farmer_land_records(tenant_id);
CREATE INDEX IF NOT EXISTS idx_farmer_land_farmer  ON agri_farmer_land_records(farmer_id);
