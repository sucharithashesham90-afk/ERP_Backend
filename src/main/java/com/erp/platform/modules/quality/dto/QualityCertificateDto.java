package com.erp.platform.modules.quality.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record QualityCertificateDto(
        UUID id,
        String certificateNumber,
        LocalDate issueDate,
        String lotNumber,
        String cropName,
        String varietyName,
        LocalDate validUpTo,
        String certifyingBody,
        BigDecimal germinationPercent,
        BigDecimal purityPercent,
        BigDecimal moisturePercent,
        String status
) {}
