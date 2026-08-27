package com.erp.platform.modules.quality.dto;

import java.math.BigDecimal;

public record CreateQualityStandardRequest(
    String standardCode,
    String standardName,
    String cropName,
    String seedClass,
    BigDecimal minGerminationPercent,
    BigDecimal minPurityPercent,
    BigDecimal maxMoisturePercent,
    BigDecimal maxInertMatterPercent,
    String description,
    Boolean active
) {}
