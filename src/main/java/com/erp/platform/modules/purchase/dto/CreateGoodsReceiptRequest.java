package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateGoodsReceiptRequest {
    private UUID purchaseOrderId;
    private UUID warehouseId;
    private LocalDate receiptDate;
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
    private List<GrnItem> items;

    @Data
    public static class GrnItem {
        private UUID purchaseOrderItemId;
        private UUID productId;
        private Double receivedQuantity;
        private Double acceptedQuantity;
        private Double rejectedQuantity;
    }
}
