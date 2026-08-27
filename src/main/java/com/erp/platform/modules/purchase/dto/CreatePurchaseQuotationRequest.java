package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePurchaseQuotationRequest {

    private UUID requisitionId;
    private UUID vendorId;
    private String vendorName;
    private String quotationType;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private int deliveryDays;
    private String paymentTerms;
    private String notes;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
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
        private int deliveryDays;
        private String remarks;
    }
}
