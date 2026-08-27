package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeedProductionStageDto {

    private UUID id;
    private String code;
    private String name;
    private boolean requiresApproval;
    private String fromStage;
    private String toStage;
    private int stageOrder;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}
