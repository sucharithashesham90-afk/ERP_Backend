package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateFreightPaymentRequest {
    private UUID goodsReceiptId;
    private String carrierName;
    private String lrNumber;
    private String dcNumber;
    private String vehicleNumber;
    private LocalDate paymentDate;
    private BigDecimal freightAmount;
    private BigDecimal advancePaid;
    private BigDecimal amountPaid;
    private String paymentMode;
    private String chequeNumber;
    private String bankName;
    private String referenceNumber;
    private String notes;
}
