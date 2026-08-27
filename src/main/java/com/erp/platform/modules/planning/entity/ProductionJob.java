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

@Entity(name = "PlanningJob")
@Table(name = "planning_jobs",
       indexes = {
           @Index(name = "idx_planning_job_tenant", columnList = "tenant_id"),
           @Index(name = "idx_planning_job_plan", columnList = "tenant_id, plan_id"),
           @Index(name = "idx_planning_job_status", columnList = "tenant_id, status"),
           @Index(name = "idx_planning_job_number", columnList = "tenant_id, job_number")
       })
@Getter
@Setter
public class ProductionJob extends TenantEntity {

    @Column(name = "job_number", nullable = false, length = 50)
    private String jobNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private ProductionPlan plan;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType = JobType.PRODUCTION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "planned_quantity", precision = 18, scale = 4)
    private BigDecimal plannedQuantity = BigDecimal.ZERO;

    @Column(name = "completed_quantity", precision = 18, scale = 4)
    private BigDecimal completedQuantity = BigDecimal.ZERO;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "assigned_team", length = 200)
    private String assignedTeam;

    @Column(name = "production_centre", length = 200)
    private String productionCentre;

    @Column(length = 1000)
    private String notes;

    // Crop context fields (seed-industry specific)
    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 100)
    private String cropGroupName;

    @Column(name = "crop_data_id")
    private UUID cropDataId;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "plant_variant_id")
    private UUID plantVariantId;

    @Column(name = "variety_name", length = 150)
    private String varietyName;

    @Column(name = "production_area_id")
    private UUID productionAreaId;

    @Column(name = "production_area_name", length = 200)
    private String productionAreaName;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionJobAllocation> allocations = new ArrayList<>();

    public enum JobType {
        PRODUCTION, REWORK, TRIAL
    }

    public enum JobStatus {
        OPEN, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    }
}
