package com.erp.platform.modules.planning.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "production_plans",
       indexes = {
           @Index(name = "idx_pp_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pp_status", columnList = "tenant_id, status"),
           @Index(name = "idx_pp_plan_number", columnList = "tenant_id, plan_number")
       })
@Getter
@Setter
public class ProductionPlan extends TenantEntity {

    @Column(name = "plan_number", nullable = false, length = 50)
    private String planNumber;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "total_target_qty", precision = 18, scale = 4)
    private BigDecimal totalTargetQty = BigDecimal.ZERO;

    @Column(name = "total_produced_qty", precision = 18, scale = 4)
    private BigDecimal totalProducedQty = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @Column(name = "sales_plan_id")
    private UUID salesPlanId;

    @Column(name = "sales_plan_name", length = 200)
    private String salesPlanName;

    /** Seed category (Plan Seed Type) from seed-category master */
    @Column(name = "seed_category_id")
    private UUID seedCategoryId;

    @Column(name = "seed_category_name", length = 100)
    private String seedCategoryName;

    /** Season period (Plan Period) from season-period master */
    @Column(name = "season_period_id")
    private UUID seasonPeriodId;

    @Column(name = "season_period_name", length = 100)
    private String seasonPeriodName;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionPlanItem> items = new ArrayList<>();

    public enum PlanType {
        ANNUAL, QUARTERLY, MONTHLY, WEEKLY
    }

    public enum PlanStatus {
        DRAFT, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
