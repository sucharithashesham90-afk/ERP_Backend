package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePackagingBagRequest(
        @NotBlank String name,
        String description,
        UUID purchaseUnitUomId,
        String purchaseUnit,
        boolean active
) {}
