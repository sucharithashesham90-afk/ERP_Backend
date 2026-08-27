package com.erp.platform.modules.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDimensionValueRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private UUID parentId;

    private boolean active = true;
}
