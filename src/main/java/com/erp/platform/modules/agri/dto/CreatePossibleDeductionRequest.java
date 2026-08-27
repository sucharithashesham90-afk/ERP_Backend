package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePossibleDeductionRequest {

    @NotBlank
    private String name;

    private String description;
    private String type;
    private String units;
    private boolean active = true;
}
