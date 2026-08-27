package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProductionPlanRequest {

    @NotBlank
    private String planName;

    private UUID seedCategoryId;
    private String seedCategoryName;
    private UUID seasonPeriodId;
    private String seasonPeriodName;
    private String status;
    private String remarks;
}
