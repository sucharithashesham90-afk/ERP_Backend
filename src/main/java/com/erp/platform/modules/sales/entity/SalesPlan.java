package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_plans",
       indexes = {
           @Index(name = "idx_spln_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sp_status", columnList = "tenant_id, status"),
           @Index(name = "idx_sp_year", columnList = "tenant_id, plan_year")
       })
@Getter
@Setter
public class SalesPlan extends TenantEntity {

    @Column(name = "plan_number", nullable = false, length = 50)
    private String planNumber;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "plan_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @Column(name = "plan_year", nullable = false)
    private int planYear;

    @Column(name = "plan_quarter")
    private Integer planQuarter;

    @Column(name = "plan_month")
    private Integer planMonth;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PlanStatus status = PlanStatus.DRAFT;

    @Column(name = "total_target_revenue", precision = 18, scale = 2)
    private BigDecimal totalTargetRevenue = BigDecimal.ZERO;

    @Column(name = "total_actual_revenue", precision = 18, scale = 2)
    private BigDecimal totalActualRevenue = BigDecimal.ZERO;

    @Column(length = 200)
    private String territory;

    @Column(name = "sales_rep_id")
    private UUID salesRepId;

    @Column(name = "sales_rep_name", length = 200)
    private String salesRepName;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "salesPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesPlanTarget> targets = new ArrayList<>();

    public enum PlanType {
        ANNUAL, QUARTERLY, MONTHLY, WEEKLY
    }

    public enum PlanStatus {
        DRAFT, APPROVED, ACTIVE, CLOSED
    }
}
