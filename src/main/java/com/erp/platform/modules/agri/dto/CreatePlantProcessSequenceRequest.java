package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreatePlantProcessSequenceRequest {
    private UUID plantCategoryId;
    private int sequenceOrder;
    @NotBlank
    private String processStepName;
    private String description;
    private String processType;
    private Double expectedOutputPercent;
}
