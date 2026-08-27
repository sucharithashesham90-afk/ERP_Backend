package com.erp.platform.modules.intake.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateIntakeSlipRequest {

    private UUID scheduleId;
    private UUID purchaseOrderId;

    private UUID vendorId;

    private String vendorName;

    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate;

    private String vehicleNumber;

    private String driverName;

    private UUID warehouseId;

    private String warehouseName;

    private String notes;

    @Valid
    private List<ItemRequest> items;

    // ── Agricultural Lot-Wise Intake Fields ──────────────────────────────────

    private String location;
    private String deliveryType;
    private String tcName;

    private UUID fieldProducerId;
    private String fieldProducerName;

    private UUID plantVariantId;
    private String plantVariantName;

    private String temporaryLotNumber;
    private String fatherName;
    private String village;

    private String inwardGatePassNumber;
    private BigDecimal weighbridgeQuantity;

    private String godownName;
    private String supervisorName;
    private String stackNumber;
    private String hamaliContractor;
    private BigDecimal emptyGunnyWeightKg;

    private String lotNumber;
    private String inputType;
    private BigDecimal moisturePercent;
    private String unloadingSlipNumber;

    private String bagWeightMode;
    private String bagType;
    private String bagSizeKg;
    private BigDecimal quantityPerBag;
    private Integer numberOfBags;

    @Data
    public static class ItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        private String productName;

        @NotNull(message = "Received quantity is required")
        private BigDecimal receivedQuantity;

        private String unit;

        private BigDecimal unitPrice;

        private String qualityRemarks;
    }
}
