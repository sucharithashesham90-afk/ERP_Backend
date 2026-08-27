package com.erp.platform.modules.manufacturing.entity;

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
@Table(name = "production_jobs",
       indexes = {
           @Index(name = "idx_pj_tenant",     columnList = "tenant_id"),
           @Index(name = "idx_pj_status",     columnList = "tenant_id, status"),
           @Index(name = "idx_pj_work_order", columnList = "work_order_id")
       })
@Getter
@Setter
public class ProductionJob extends TenantEntity {

    @Column(name = "job_number", nullable = false, length = 50)
    private String jobNumber;

    @Column(name = "job_name", length = 200)
    private String jobName;

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "job_type", length = 20)
    private String jobType = "PROCESS";

    @Column(name = "process_type", length = 50)
    private String processType; // GRADING, PROCESSING, PACKAGING, ASSEMBLY, FINISHING, TREATMENT, INSPECTION

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.DRAFT;

    // ── Links ────────────────────────────────────────────────────────────────
    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "production_plan_id")
    private UUID productionPlanId;

    // ── Input material ───────────────────────────────────────────────────────
    @Column(name = "input_product_id")
    private UUID inputProductId;

    @Column(name = "input_product_name", length = 200)
    private String inputProductName;

    @Column(name = "input_quantity", precision = 18, scale = 4)
    private BigDecimal inputQuantity = BigDecimal.ZERO;

    @Column(name = "input_unit", length = 20)
    private String inputUnit = "KG";

    // ── Output product ───────────────────────────────────────────────────────
    @Column(name = "output_product_id")
    private UUID outputProductId;

    @Column(name = "output_product_name", length = 200)
    private String outputProductName;

    @Column(name = "planned_output_quantity", precision = 18, scale = 4)
    private BigDecimal plannedOutputQuantity = BigDecimal.ZERO;

    @Column(name = "actual_output_quantity", precision = 18, scale = 4)
    private BigDecimal actualOutputQuantity = BigDecimal.ZERO;

    @Column(name = "output_unit", length = 20)
    private String outputUnit = "KG";

    // ── Schedule ─────────────────────────────────────────────────────────────
    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    // ── Execution ────────────────────────────────────────────────────────────
    @Column(name = "work_center", length = 100)
    private String workCenter;

    @Column(name = "supervisor", length = 100)
    private String supervisor;

    @Column(name = "labor_count")
    private Integer laborCount = 0;

    @Column(name = "expenditure", precision = 18, scale = 2)
    private BigDecimal expenditure = BigDecimal.ZERO;

    @Column(name = "expected_yield", precision = 5, scale = 2)
    private BigDecimal expectedYield = BigDecimal.ZERO; // percentage

    @Column(name = "actual_yield", precision = 5, scale = 2)
    private BigDecimal actualYield = BigDecimal.ZERO; // percentage

    @Column(length = 1000)
    private String notes;

    // ── Created lot (populated when job is initiated) ─────────────────────────
    @Column(name = "lot_id")
    private UUID lotId;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Column(name = "storage_location_name", length = 200)
    private String storageLocationName;

    @Column(name = "location_name", length = 150)
    private String locationName;

    @Column(name = "start_time", length = 10)
    private String startTime;

    @Column(name = "end_time", length = 10)
    private String endTime;

    @Column(name = "process_step_id")
    private UUID processStepId;

    @Column(name = "process_step_name", length = 200)
    private String processStepName;

    @Column(name = "process_line_id")
    private UUID processLineId;

    @Column(name = "process_line_name", length = 200)
    private String processLineName;

    // ── Packaging unit size (PACKING process type) ───────────────────────────
    @Column(name = "packaging_unit_size", precision = 18, scale = 4)
    private BigDecimal packagingUnitSize;

    @Column(name = "packaging_unit_size_unit", length = 30)
    private String packagingUnitSizeUnit;

    // ── Packaging (optional, for finished goods jobs) ─────────────────────────
    @Column(name = "packaging_date")
    private LocalDate packagingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;

    // ── Seed context (auto-populated from the input lot's stock) & Process Job header ──
    @Column(name = "crop_group_name", length = 150)
    private String cropGroupName;

    @Column(name = "crop_name", length = 150)
    private String cropName;

    @Column(name = "variety_name", length = 150)
    private String varietyName;

    @Column(name = "seed_state", length = 100)
    private String seedState;

    /** Input material state (Fresh / Grading / Graded ...) — a process job transforms a state,
     *  not a product. Defaults to the input lot's seed state. */
    @Column(name = "input_state", length = 100)
    private String inputState;

    /** Output material state after this step (Grading / Graded / Product ...), from the process step. */
    @Column(name = "output_state", length = 100)
    private String outputState;

    /** Multi-step processing: JSON array of the job's process-step sequence, each step holding its
     *  own process step, type, status, input lots and output lines (Step 1 of N stepper). */
    @Column(name = "steps_json", columnDefinition = "TEXT")
    private String stepsJson;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(name = "grower_name", length = 200)
    private String growerName;

    @Column(name = "village_name", length = 200)
    private String villageName;

    @Column(length = 30)
    private String moisture;

    /** Input Type (e.g. Individual / Bulk). */
    @Column(name = "input_type", length = 50)
    private String inputType;

    /** Seed Type (e.g. Fresh). */
    @Column(name = "seed_type", length = 50)
    private String seedType;

    @Column(length = 50)
    private String stage;

    @Column(name = "male_labour_count")
    private Integer maleLabourCount = 0;

    @Column(name = "female_labour_count")
    private Integer femaleLabourCount = 0;

    @Column(name = "hamali_contractor", length = 200)
    private String hamaliContractor;

    /** Include only quality-tested lots. */
    @Column(name = "only_quality_tested", nullable = false, columnDefinition = "boolean not null default false")
    private boolean onlyQualityTested = false;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessJobInputLot> inputLots = new ArrayList<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessJobOutputLine> outputLines = new ArrayList<>();

    public enum JobStatus {
        DRAFT, PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
