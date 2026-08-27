package com.erp.platform.modules.intake.dto;

import com.erp.platform.modules.intake.entity.IntakeSlip.SlipStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class IntakeSlipDto {

    private UUID id;
    private UUID tenantId;
    private String slipNumber;
    private UUID scheduleId;
    private UUID purchaseOrderId;
    private String scheduleNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate receiptDate;
    private SlipStatus status;
    private String vehicleNumber;
    private String driverName;
    private BigDecimal totalQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private String rejectionReason;
    private UUID warehouseId;
    private String warehouseName;
    private String notes;
    private List<ItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal receivedQuantity;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String unit;
        private BigDecimal unitPrice;
        private String lotNumber;
        private String qualityRemarks;
    }
}
