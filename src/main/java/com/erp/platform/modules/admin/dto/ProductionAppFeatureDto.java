package com.erp.platform.modules.admin.dto;

import java.util.UUID;

public record ProductionAppFeatureDto(
        UUID id,
        String featureKey,
        String featureName,
        String description,
        boolean enabled,
        String moduleContext
) {}
