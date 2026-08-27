package com.erp.platform.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLotPurchaseReturnRequest(
        String returnNumber,
        LocalDate returnDate,
        String lotNumber,
        String supplierName,
        String cropName,
        BigDecimal returnQuantityKgs,
        String returnReason,
        String originalInvoiceNumber,
        String creditNoteNumber,
        String status
) {}
