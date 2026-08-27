package com.erp.platform.modules.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSalesReturnRequest {
    private UUID invoiceId;
    private UUID salesOrderId;
    private UUID customerId;
    private String customerName;
    private String reason;
    private String notes;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private UUID productId;
        private String productName;
        private UUID cropId;
        private String cropName;
        private UUID varietyId;
        private String varietyName;
        private UUID bagSizeId;
        private String bagSizeName;
        private UUID bagTypeId;
        private String bagTypeName;
        private String lotNumber;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String unit;
        private UUID warehouseId;
        private String warehouseName;
        private String remarks;
    }
}
