package com.erp.platform.modules.purchase.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePurchaseReturnRequest {
    private UUID goodsReceiptId;
    private UUID invoiceId;
    private UUID vendorId;
    private String vendorName;
    private String reason;
    private String notes;
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
    private List<ReturnItem> items;

    @Data
    public static class ReturnItem {
        private UUID goodsReceiptItemId;
        private UUID productId;
        private String productName;
        private UUID cropGroupId;
        private String cropGroupName;
        private UUID cropId;
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
        private String reason;
        /** The return screen labels this field "Remarks"; treated as the line reason. */
        private String remarks;
    }
}
