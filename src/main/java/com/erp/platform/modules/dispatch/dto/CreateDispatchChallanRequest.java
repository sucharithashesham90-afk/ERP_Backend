package com.erp.platform.modules.dispatch.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateDispatchChallanRequest {

    /** Optional — auto-generated when blank. */
    private String challanNumber;

    private LocalDate challanDate;
    private String customerId;
    private String customerName;
    private String deliveryAddress;
    private String productName;
    private String lotNumber;
    private BigDecimal quantityKgs;
    private String transporterName;
    private String vehicleNumber;
    private String status;
    private String remarks;
    private BigDecimal value;

    // Delivery order link
    private UUID salesOrderId;
    private String salesOrderNumber;

    private UUID deliveryOrderId;
    private String deliveryOrderNumber;

    // Dispatch header
    private String salesArea;
    private String dispatchLocationId;
    private String dispatchLocation;
    private String wayBillNo;
    private String rrRlNo;

    // Freight
    private String freightCarrierId;
    private String freightCarrier;
    private Boolean otherCarrier;
    private String carrier;
    private String lorryNo;
    private BigDecimal freightAmount;
    private BigDecimal freightPaidAdvance;
    private BigDecimal freightToPay;

    // Billing address
    private String billingAddress;
    private String billingState;
    private String billingDistrict;
    private String billingCity;
    private String billingZip;
    private String billingPhone;

    // Supplier (delivery) address
    private String supplierAddress;
    private String supplierState;
    private String supplierDistrict;
    private String supplierCity;
    private String supplierZip;
    private String supplierPhone;

    // Line items
    private java.util.List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private String productId;
        private String cropGroup;
        private String crop;
        private String variety;
        private String cropVariety;
        private String productName;
        private String packing;
        private String lotNumber;
        private BigDecimal quantity;
        private BigDecimal rate;
        private BigDecimal value;
    }
}
