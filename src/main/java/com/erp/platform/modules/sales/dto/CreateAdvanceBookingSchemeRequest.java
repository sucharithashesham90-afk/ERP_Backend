package com.erp.platform.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAdvanceBookingSchemeRequest(
    String schemeCode,
    String schemeName,
    LocalDate fromDate,
    LocalDate toDate,
    String productName,
    String benefitType,
    BigDecimal flatBenefitPercent,
    BigDecimal minOrderQty,
    String description,
    String status
) {}
