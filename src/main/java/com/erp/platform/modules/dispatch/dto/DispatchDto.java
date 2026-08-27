package com.erp.platform.modules.dispatch.dto;

import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchStatus;
import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DispatchDto {

    private UUID id;
    private UUID tenantId;
    private String dispatchNumber;
    private DispatchType dispatchType;
    private DispatchStatus status;
    private UUID salesOrderId;
    private UUID customerId;
    private String customerName;
    private String deliveryOrderRef;
    private BigDecimal challanValue;
    private String billingAddress;
    private String billingPhone;
    private String deliveryAddress;
    private String deliveryPhone;
    private boolean consigneeBuyerSame;
    private String salesOffice;
    private String fromLocation;
    private String toLocation;
    private String stockTransferOrderRef;
    private LocalDate dispatchDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String carrierName;
    private String carrierPhone;
    private String vehicleNumber;
    private String trackingNumber;
    private String lrNumber;
    private String wayBillNumber;
    private String rrRlNumber;
    private BigDecimal freightCharges;
    private BigDecimal freightPaidAdvance;
    private BigDecimal freightToPay;
    private BigDecimal balanceAfterSubmission;
    private int totalPackages;
    private BigDecimal totalWeight;
    private String weightUnit;
    private String packedBy;
    private String transporterPanNumber;
    private String documentsThrough;
    private boolean podReceived;
    private LocalDate podDate;
    private String podReference;
    private String notes;
    private List<ItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private UUID warehouseId;
        private String lotNumber;
        private String batchNumber;
        private BigDecimal quantity;
        private String unit;
        private int packageCount;
        private String packageType;
        private BigDecimal packSizeKg;
        private String secondaryPackType;
        private int secondaryPackCount;
        private String secondaryBagSize;
        private String materialState;
        private String materialType;
        private BigDecimal unitPrice;
        private BigDecimal grossWeight;
        private BigDecimal netWeight;
        private String remarks;
    }
}
