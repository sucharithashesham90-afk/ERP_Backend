package com.erp.platform.modules.dispatch.dto;

import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateDispatchRequest {

    @NotNull(message = "Dispatch type is required")
    private DispatchType dispatchType;

    private UUID salesOrderId;
    private UUID customerId;
    private String customerName;
    private String deliveryOrderRef;
    private BigDecimal challanValue = BigDecimal.ZERO;
    private String billingAddress;
    private String billingPhone;
    private String deliveryAddress;
    private String deliveryPhone;
    private boolean consigneeBuyerSame = true;
    private String salesOffice;
    private String fromLocation;
    private String toLocation;
    private String stockTransferOrderRef;
    private LocalDate dispatchDate;
    private LocalDate expectedDeliveryDate;
    private String carrierName;
    private String carrierPhone;
    private String vehicleNumber;
    private String trackingNumber;
    private String lrNumber;
    private String wayBillNumber;
    private String rrRlNumber;
    private BigDecimal freightCharges = BigDecimal.ZERO;
    private BigDecimal freightPaidAdvance = BigDecimal.ZERO;
    private BigDecimal freightToPay = BigDecimal.ZERO;
    private BigDecimal balanceAfterSubmission = BigDecimal.ZERO;
    private int totalPackages;
    private BigDecimal totalWeight = BigDecimal.ZERO;
    private String weightUnit = "KG";
    private String packedBy;
    private String transporterPanNumber;
    private String documentsThrough;
    private String notes;

    @Valid
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private UUID productId;
        private String productName;
        private UUID warehouseId;
        private String lotNumber;
        private String batchNumber;
        @NotNull
        private BigDecimal quantity;
        private String unit;
        private int packageCount = 1;
        private String packageType;
        private BigDecimal packSizeKg = BigDecimal.ZERO;
        private String secondaryPackType;
        private int secondaryPackCount = 0;
        private String secondaryBagSize;
        private String materialState;
        private String materialType;
        private BigDecimal unitPrice = BigDecimal.ZERO;
        private BigDecimal grossWeight;
        private BigDecimal netWeight;
        private String remarks;
    }
}
