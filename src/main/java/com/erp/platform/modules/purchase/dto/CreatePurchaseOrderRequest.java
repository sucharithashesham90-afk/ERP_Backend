package com.erp.platform.modules.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePurchaseOrderRequest {

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private String subject;
    private String quotationReference;
    private String deliveryAddress;
    private String paymentTerms;
    private String deliveryTerms;
    private String qualityTerms;
    private BigDecimal discountAmount;
    private BigDecimal freightCharges;
    private String notes;

    // seed-industry PO header (per Purchase doc)
    private String poType;
    private String seedType;
    private String seedState;
    private String location;
    private String deliveryState;
    private String deliveryDistrict;
    private String deliveryCity;
    private String deliveryZip;
    private String deliveryPhone;
    private String supplierAddress;
    private String supplierState;
    private String supplierDistrict;
    private String supplierCity;
    private String supplierZip;
    private String supplierPhone;
    private String supplierRef;
    private String validityOfOrder;
    private String insuranceDetails;
    private String otherTerms;
    private String signatureBy;
    private String advancePercent;

    @Valid
    private List<POItemRequest> items;

    @Data
    public static class POItemRequest {
        // SEED | CONSUMABLE | PACKING_MATERIAL — Seed items use crop group + variety, not a product.
        private String itemType;
        private String cropGroupName;
        private String cropName;
        private String varietyName;
        private Integer numberOfBags;
        private BigDecimal quantityPerBag;
        // productId is NOT required (seed items have no product).
        private UUID productId;
        private String productName;
        @NotNull
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice = BigDecimal.ZERO;
        private BigDecimal discountPercent = BigDecimal.ZERO;
        private BigDecimal taxPercent = BigDecimal.ZERO;
    }
}
