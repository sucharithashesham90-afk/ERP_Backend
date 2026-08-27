package com.erp.platform.modules.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SalesReturnDto {
    private UUID id;
    private UUID tenantId;
    private String returnNumber;
    private UUID invoiceId;
    private UUID salesOrderId;
    private UUID customerId;
    private String customerName;
    private LocalDate returnDate;
    private String reason;
    private String notes;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private String paymentMethod;
    private String chequeNumber;
    private String chequeDate;
    private LocalDateTime createdAt;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private UUID id;
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
        private BigDecimal totalAmount;
        private String unit;
        private UUID warehouseId;
        private String warehouseName;
        private String remarks;
    }
}
