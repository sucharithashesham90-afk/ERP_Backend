package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseOrderDto {
    private UUID id;
    private UUID tenantId;
    private String poNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private POStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String subject;
    private String quotationReference;
    private BigDecimal discountAmount;
    private BigDecimal freightCharges;
    private String deliveryAddress;
    private String paymentTerms;
    private String deliveryTerms;
    private String qualityTerms;
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
    private List<POItemDto> items;
    private LocalDateTime createdAt;
    private Boolean emailSent;
    private String emailMessage;

    @Data
    public static class POItemDto {
        private UUID id;
        private String itemType;
        private String cropGroupName;
        private String cropName;
        private String varietyName;
        private Integer numberOfBags;
        private BigDecimal quantityPerBag;
        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal discountAmount;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private BigDecimal receivedQty;
    }
}
