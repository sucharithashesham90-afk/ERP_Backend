package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSeedProductionStageRequest {

    private String code;

    @NotBlank
    private String name;

    private boolean requiresApproval;
    private String fromStage;
    private String toStage;
    private int stageOrder;
    private String description;
    private boolean active = true;
}
