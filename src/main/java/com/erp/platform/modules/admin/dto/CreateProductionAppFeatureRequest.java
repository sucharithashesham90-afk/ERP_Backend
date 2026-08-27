package com.erp.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProductionAppFeatureRequest(
        @NotBlank String featureKey,
        @NotBlank String featureName,
        String description,
        boolean enabled,
        String moduleContext
) {}
