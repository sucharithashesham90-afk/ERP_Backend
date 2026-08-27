package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDealerClaimRequest(
        String claimNumber,
        LocalDate claimDate,
        String dealerName,
        String schemeName,
        String productName,
        BigDecimal quantitySold,
        BigDecimal claimAmountPerUnit,
        BigDecimal totalClaim,
        String supportingDocsRef,
        String status
) {}
