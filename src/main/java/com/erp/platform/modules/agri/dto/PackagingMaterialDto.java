package com.erp.platform.modules.agri.dto;

import java.util.UUID;

public record PackagingMaterialDto(
        UUID id,
        String materialCode,
        String materialName,
        String description,
        String materialType,
        String classOfSeed,
        boolean hybrid,
        boolean active
) {}
