package com.erp.platform.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSalesOrderRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID quotationId;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String shippingAddress;
    private String shippingState;
    private String shippingDistrict;
    private String shippingCity;
    private String shippingCountry;
    private String paymentTerms;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String notes;
    /** Captured on the rep's phone at the point of sale; a data URI. */
    private String signatureImage;
    private String signedBy;

    @Valid
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private UUID productId;
        private String productName;
        @NotNull
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent = BigDecimal.ZERO;
        private BigDecimal taxPercent = BigDecimal.ZERO;
    }
}
