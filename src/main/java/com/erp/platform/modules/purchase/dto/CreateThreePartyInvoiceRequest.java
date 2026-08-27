package com.erp.platform.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateThreePartyInvoiceRequest(
        String invoiceNumber,
        LocalDate invoiceDate,
        String thirdPartyName,
        String cropName,
        String lotNumber,
        Integer quantityBags,
        BigDecimal quantityKgs,
        BigDecimal ratePerKg,
        BigDecimal basicAmount,
        BigDecimal otherCharges,
        BigDecimal totalAmount,
        String paymentTerms,
        String status
) {}
