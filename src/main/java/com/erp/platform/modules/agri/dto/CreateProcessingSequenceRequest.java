package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProcessingSequenceRequest {

    @NotBlank
    private String name;

    private String processingSteps;
    private String stepsJson;
    private boolean active = true;
}
