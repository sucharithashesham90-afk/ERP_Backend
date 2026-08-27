package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.Quotation.QuotationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class QuotationDto {
    private UUID id;
    private UUID tenantId;
    private String quotationNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private QuotationStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String terms;
    private String notes;
    private List<QuotationItemDto> items;
    private LocalDateTime createdAt;
    private Boolean emailSent;
    private String emailMessage;

    @Data
    public static class QuotationItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private UUID cropGroupId;
        private String cropGroupName;
        private UUID cropId;
        private String cropName;
        private UUID varietyId;
        private String varietyName;
        private String description;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
    }
}
