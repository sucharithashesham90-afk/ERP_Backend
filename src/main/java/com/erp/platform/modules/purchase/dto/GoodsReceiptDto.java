package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GoodsReceiptDto {
    private UUID id;
    private UUID tenantId;
    private String grnNumber;
    private UUID purchaseOrderId;
    private UUID sourceIntakeSlipId;
    private String poNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate receiptDate;
    private String status;
    private String vehicleNumber;
    private String driverName;
    private String lrNumber;
    private String dcNumber;
    private LocalDate dcDate;
    private String freightCarrierName;
    private BigDecimal freightAmount;
    private BigDecimal freightAdvancePaid;
    private String inGatePass;
    private String notes;
    private LocalDateTime createdAt;
    private java.util.List<GrnItemDto> items;

    @Data
    public static class GrnItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal orderedQty;
        private BigDecimal receivedQty;
        private BigDecimal acceptedQty;
        private BigDecimal rejectedQty;
    }
}
