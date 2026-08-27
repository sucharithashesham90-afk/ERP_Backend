package com.erp.platform.modules.quality.dto;
import java.util.UUID;

import java.math.BigDecimal;

public record QualityStandardDto(
    UUID id,
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
