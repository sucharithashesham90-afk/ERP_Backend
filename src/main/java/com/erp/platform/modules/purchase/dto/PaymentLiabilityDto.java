package com.erp.platform.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentLiabilityDto(
        UUID id,
        String liabilityNumber,
        String partyType,
        String partyName,
        String vendorCode,
        String lotNumber,
        LocalDate liabilityFromDate,
        LocalDate liabilityToDate,
        BigDecimal totalLiability,
        BigDecimal paidAmount,
        BigDecimal balance,
        String status
) {}
