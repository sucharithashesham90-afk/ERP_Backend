package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;

public record CreatePackagingDesignRequest(
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
