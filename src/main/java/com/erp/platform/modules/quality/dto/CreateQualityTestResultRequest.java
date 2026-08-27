package com.erp.platform.modules.quality.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateQualityTestResultRequest(
        String testResultNumber,
        LocalDate testDate,
        String lotNumber,
        String cropName,
        String varietyName,
        String testType,
        BigDecimal germinationPercent,
        BigDecimal purityPercent,
        BigDecimal moisturePercent,
        BigDecimal inertMatterPercent,
        String testedBy,
        String status,
        String remarks
) {}
