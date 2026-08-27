package com.erp.platform.modules.agri.dto;

import java.util.UUID;

public record PackagingBagDto(
        UUID id,
        String name,
        String description,
        UUID purchaseUnitUomId,
        String purchaseUnit,
        boolean active
) {}
