package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PackagingDesignDto(
        UUID id,
        String productName,
        String varietyLabel,
        String brandCode,
        String brandName,
        String packingMaterial,
        BigDecimal packingQty,
        BigDecimal netWeightKg,
        BigDecimal grossWeightKg,
        BigDecimal dimensionLength,
        BigDecimal dimensionWidth,
        BigDecimal dimensionHeight,
        String packSize,
        boolean active
) {}
