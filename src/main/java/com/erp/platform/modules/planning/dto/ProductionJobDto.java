package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionJob.JobStatus;
import com.erp.platform.modules.planning.entity.ProductionJob.JobType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductionJobDto {

    private UUID id;
    private UUID tenantId;
    private String jobNumber;
    private UUID planId;
    private String planNumber;
    private String planName;
    private UUID productId;
    private String productName;
    private UUID workOrderId;
    private JobType jobType;
    private JobStatus status;
    private BigDecimal plannedQuantity;
    private BigDecimal completedQuantity;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String assignedTeam;
    private String productionCentre;
    private String notes;

    // Crop context
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropDataId;
    private String cropName;
    private UUID plantVariantId;
    private String varietyName;
    private UUID productionAreaId;
    private String productionAreaName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
