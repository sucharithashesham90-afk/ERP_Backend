package com.erp.platform.modules.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProducerFinalPaymentRequest(
        String paymentNumber,
        LocalDate paymentDate,
        String producerName,
        String cropName,
        String season,
        String lotNumber,
        BigDecimal contractQuantityKgs,
        BigDecimal actualSuppliedKgs,
        BigDecimal ratePerKg,
        BigDecimal basicAmount,
        BigDecimal deductions,
        BigDecimal advancePaid,
        BigDecimal netPayable,
        String status
) {}
