package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProductLineRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private UUID brandId;

    private String category;

    private String targetMarket;

    private boolean active = true;

    private String notes;
}
