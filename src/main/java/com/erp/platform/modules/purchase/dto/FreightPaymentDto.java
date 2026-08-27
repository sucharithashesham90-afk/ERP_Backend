package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FreightPaymentDto {
    private UUID id;
    private UUID tenantId;
    private String paymentNumber;
    private UUID goodsReceiptId;
    private String grnNumber;
    private String carrierName;
    private String lrNumber;
    private String dcNumber;
    private String vehicleNumber;
    private LocalDate paymentDate;
    private BigDecimal freightAmount;
    private BigDecimal advancePaid;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String paymentMode;
    private String chequeNumber;
    private String bankName;
    private String referenceNumber;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
