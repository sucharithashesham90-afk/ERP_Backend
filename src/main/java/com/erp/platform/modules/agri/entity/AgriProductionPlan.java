package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriProductionPlan")
@Table(name = "agri_production_plans",
       indexes = {@Index(name = "idx_agri_prod_plan_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AgriProductionPlan extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String planNumber;

    @Column(length = 50)
    private String season;

    @Column(length = 10)
    private String planningYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_variant_id")
    private PlantVariant plantVariant;

    @Column(precision = 15, scale = 3)
    private BigDecimal targetArea;

    @Column(precision = 15, scale = 3)
    private BigDecimal targetQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    private LocalDate plannedStart;
    private LocalDate plannedEnd;

    /** DRAFT / APPROVED / IN_PROGRESS / COMPLETED / CANCELLED */
    @Column(length = 20)
    private String status = "DRAFT";

    @Column(length = 500)
    private String remarks;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Column(name = "seed_category_id")
    private java.util.UUID seedCategoryId;

    @Column(name = "seed_category_name", length = 100)
    private String seedCategoryName;

    @Column(name = "season_period_id")
    private java.util.UUID seasonPeriodId;

    @Column(name = "season_period_name", length = 200)
    private String seasonPeriodName;
}
