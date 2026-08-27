package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "plant_variants",
       indexes = {@Index(name = "idx_plant_var_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PlantVariant extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "alias_name", length = 150)
    private String aliasName;

    @Column(length = 50)
    private String code;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // ── Tab 1: Basic / Classification ──────────────────────────────────────

    /** A variety belongs to a crop (which belongs to a crop group) — drives the cascade everywhere. */
    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 100)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_category_id")
    private PlantCategory plantCategory;

    @Column(name = "seed_class_id")
    private UUID seedClassId;

    @Column(name = "seed_class_name", length = 100)
    private String seedClassName;

    @Column(name = "seed_category_id")
    private UUID seedCategoryId;

    @Column(name = "seed_category_name", length = 100)
    private String seedCategoryName;

    @Column(name = "year_of_release")
    private Integer yearOfRelease;

    @Column(name = "release_year", length = 100)
    private String releaseYear;

    @Column(name = "notification_number", length = 100)
    private String notificationNumber;

    @Column(name = "division", length = 200)
    private String division;

    // ── Tab 2: Variety Details ──────────────────────────────────────────────

    @Column(name = "hybrid")
    private boolean hybrid = false;

    @Column(name = "transgenic")
    private boolean transgenic = false;

    @Column(name = "product_wise_sales_plan")
    private boolean productWiseSalesPlan = false;

    @Column(name = "got_required", length = 10)
    private String gotRequired;

    @Column(name = "average_seed_weight", precision = 10, scale = 3)
    private BigDecimal averageSeedWeight;

    @Column(name = "plant_product_weight", precision = 10, scale = 3)
    private BigDecimal plantProductWeight;

    @Column(name = "max_process_loss", precision = 6, scale = 2)
    private BigDecimal maxProcessLoss;

    @Column(name = "min_process_loss", precision = 6, scale = 2)
    private BigDecimal minProcessLoss;

    @Column(name = "in_wt_per_moisture", precision = 10, scale = 4)
    private BigDecimal inWtPerMoisture;

    @Column(name = "sow_harvest_prd")
    private Integer sowHarvestPrd;

    @Column(name = "isolation_dist", precision = 10, scale = 2)
    private BigDecimal isolationDist;

    @Column(name = "max_off_types_count")
    private Integer maxOffTypesCount;

    @Column(name = "max_selfs_count")
    private Integer maxSelfsCount;

    @Column(name = "screens", length = 100)
    private String screens;

    @Column(name = "expiration_period_value")
    private Integer expirationPeriodValue;

    @Column(name = "expiration_period_unit", length = 20)
    private String expirationPeriodUnit;

    @Column(name = "characteristics", columnDefinition = "TEXT")
    private String characteristics;

    @Column(name = "treatments", columnDefinition = "TEXT")
    private String treatments;

    // ── Tab 3: Production Areas & Processing Plants ─────────────────────────

    @Column(name = "production_area_ids", length = 2000)
    private String productionAreaIds;

    @Column(name = "production_area_names", length = 1000)
    private String productionAreaNames;

    @Column(name = "processing_plant_ids", length = 2000)
    private String processingPlantIds;

    @Column(name = "processing_plant_names", length = 1000)
    private String processingPlantNames;

    // ── Legacy fields ────────────────────────────────────────────────────────

    @Column(length = 500)
    private String description;

    @Column(name = "licensing_authority", length = 100)
    private String licensingAuthority;

    @Column(precision = 5, scale = 2)
    private BigDecimal standardViabilityRate;

    @Column(precision = 5, scale = 2)
    private BigDecimal standardQualityGrade;

    @Column(precision = 5, scale = 2)
    private BigDecimal standardMoistureLevel;

    @Column(name = "production_season", length = 50)
    private String productionSeason;

    @Column(name = "maturity_days", length = 50)
    private String maturityDays;
}
