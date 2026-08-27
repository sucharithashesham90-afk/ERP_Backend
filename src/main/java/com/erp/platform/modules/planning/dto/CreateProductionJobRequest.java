package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionJob.JobType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProductionJobRequest {

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private UUID productId;

    private String productName;

    private UUID workOrderId;

    private JobType jobType = JobType.PRODUCTION;

    private BigDecimal plannedQuantity;

    private LocalDate scheduledDate;

    private String assignedTeam;

    private String productionCentre;

    private String notes;

    // Crop context (seed-industry)
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropDataId;
    private String cropName;
    private UUID plantVariantId;
    private String varietyName;
    private UUID productionAreaId;
    private String productionAreaName;
}
