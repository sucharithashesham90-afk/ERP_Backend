package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.PurchaseQuotation.PQStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseQuotationDto {

    private UUID id;
    private UUID tenantId;
    private String quotationNumber;
    private UUID requisitionId;
    private UUID vendorId;
    private String vendorName;
    private String quotationType;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private PQStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private int deliveryDays;
    private String paymentTerms;
    private String notes;
    private LocalDateTime createdAt;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private UUID cropGroupId;
        private String cropGroupName;
        private UUID cropId;
        private String cropName;
        private UUID varietyId;
        private String varietyName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private int deliveryDays;
        private String remarks;
    }
}
