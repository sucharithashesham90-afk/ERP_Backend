package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProcessingScreenRequest(
        @NotBlank String code,
        @NotBlank String name,
        String meshSize,
        String screenType,
        String material,
        String description,
        boolean active
) {}
