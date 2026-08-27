package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlantProcessSequenceDto {
    private UUID id;
    private UUID plantCategoryId;
    private String plantCategoryName;
    private int sequenceOrder;
    private String processStepName;
    private String description;
    private String processType;
    private Double expectedOutputPercent;
    private boolean active;
    private LocalDateTime createdAt;
}
