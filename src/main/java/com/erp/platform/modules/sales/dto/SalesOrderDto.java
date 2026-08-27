package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SalesOrderDto {
    private UUID id;
    private UUID tenantId;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private UUID quotationId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private SalesOrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String shippingState;
    private String shippingDistrict;
    private String shippingCity;
    private String shippingCountry;
    private String paymentTerms;
    private String notes;
    private String signatureImage;
    private String signedBy;
    private java.time.LocalDateTime signedAt;
    private List<SalesOrderItemDto> items;
    private LocalDateTime createdAt;
    private boolean hasInvoice;
    private boolean hasDeliveryNote;

    @Data
    public static class SalesOrderItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private BigDecimal deliveredQty;
    }
}
