package com.erp.platform.modules.agri.dto;

public record CreatePackagingMaterialRequest(
        String materialCode,
        String materialName,
        String description,
        String materialType,
        String classOfSeed,
        boolean hybrid,
        boolean active
) {}
