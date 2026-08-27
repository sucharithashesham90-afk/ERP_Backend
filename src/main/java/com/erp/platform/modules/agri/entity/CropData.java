package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "AgriCropData")
@Table(name = "agri_crop_data", indexes = {@Index(name = "idx_cd_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CropData extends TenantEntity {

    @Column(name = "crop_name", length = 100, nullable = false)
    private String cropName;

    @Column(name = "crop_code", length = 50)
    private String cropCode;

    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 100)
    private String cropGroupName;

    @Column(name = "season_ids", length = 1000)
    private String seasonIds;

    @Column(name = "season_names", length = 500)
    private String seasonNames;

    @Column(name = "no_of_inspections")
    private Integer noOfInspections;

    @Column(name = "uom", length = 20)
    private String uom;

    // ── Automatic Sample Creation ──────────────────────────────────────────────

    @Column(name = "sample_at_intake", nullable = false)
    private boolean sampleAtIntake = false;

    @Column(name = "sample_at_sales_return", nullable = false)
    private boolean sampleAtSalesReturn = false;

    @Column(name = "sample_at_third_party", nullable = false)
    private boolean sampleAtThirdParty = false;

    @Column(name = "sample_at_seed_expire", nullable = false)
    private boolean sampleAtSeedExpire = false;

    // ── Timing & Sizing ────────────────────────────────────────────────────────

    @Column(name = "certification_time", precision = 8, scale = 2)
    private BigDecimal certificationTime;

    @Column(name = "certification_time_unit", length = 20)
    private String certificationTimeUnit;

    @Column(name = "processing_time", precision = 8, scale = 2)
    private BigDecimal processingTime;

    @Column(name = "processing_time_unit", length = 20)
    private String processingTimeUnit;

    @Column(name = "lot_size_qty", precision = 10, scale = 3)
    private BigDecimal lotSizeQty;

    @Column(name = "isolation_distance", precision = 8, scale = 2)
    private BigDecimal isolationDistance;

    // ── Parent Seed Pack ───────────────────────────────────────────────────────

    @Column(name = "parent_seed_pack_type", length = 100)
    private String parentSeedPackType;

    @Column(name = "parent_seed_pack_size_kgs", precision = 6, scale = 3)
    private BigDecimal parentSeedPackSizeKgs;

    // ── Material ───────────────────────────────────────────────────────────────

    @Column(name = "material_state", length = 100)
    private String materialState;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "material_type", length = 100)
    private String materialType;

    // ── General ────────────────────────────────────────────────────────────────

    @Column(name = "automatic_sample_creation", length = 500)
    private String automaticSampleCreation;

    @Column(name = "notes", length = 500)
    private String notes;
}
