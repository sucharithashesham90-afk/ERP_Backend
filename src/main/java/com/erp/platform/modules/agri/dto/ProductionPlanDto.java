package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductionPlanDto {
    private UUID id;
    private String planNumber;
    private String planName;
    private UUID seedCategoryId;
    private String seedCategoryName;
    private UUID seasonPeriodId;
    private String seasonPeriodName;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
