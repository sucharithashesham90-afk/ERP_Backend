package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseReturnDto {
    private UUID id;
    private UUID tenantId;
    private String returnNumber;
    private UUID goodsReceiptId;
    private String grnNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate returnDate;
    private String reason;
    private String notes;
    private BigDecimal totalAmount;
    private String status;
    private String debitNoteNumber;
    private LocalDate debitNoteDate;
    private String debitNoteStatus;
    // return logistics header (per Purchase doc)
    private String returnType;
    private String prLocation;
    private String wayBillNumber;
    private String returnValue;
    private String rrRlNumber;
    private String carrier;
    private String lorryNumber;
    private String freightTotal;
    private String freightPaidAdvance;
    private String freightToPay;
    private String billingAddress;
    private String billingState;
    private String billingDistrict;
    private String billingCity;
    private String billingZip;
    private String billingPhone;
    private String deliveryAddress;
    private String deliveryState;
    private String deliveryDistrict;
    private String deliveryCity;
    private String deliveryZip;
    private String deliveryPhone;
    private List<ItemDto> items;
    private LocalDateTime createdAt;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID goodsReceiptItemId;
        private UUID productId;
        private String productName;
        private UUID cropId;
        private String cropGroupName;
        private String cropName;
        private UUID varietyId;
        private String varietyName;
        private String lotNumber;
        private UUID warehouseId;
        private String warehouseName;
        private String unit;
        private BigDecimal returnQty;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String reason;
    }
}
