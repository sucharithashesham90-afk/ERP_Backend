package com.erp.platform.modules.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDimensionRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    private boolean mandatory = false;

    private boolean active = true;
}
