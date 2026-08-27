package com.erp.platform.modules.intake.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "intake_slips",
       indexes = {
           @Index(name = "idx_isl_tenant", columnList = "tenant_id"),
           @Index(name = "idx_isl_status", columnList = "tenant_id, status"),
           @Index(name = "idx_isl_vendor", columnList = "tenant_id, vendor_id"),
           @Index(name = "idx_isl_slip_number", columnList = "tenant_id, slip_number")
       })
@Getter
@Setter
public class IntakeSlip extends TenantEntity {

    @Column(name = "slip_number", nullable = false, length = 50)
    private String slipNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private IntakeSchedule schedule;

    /** Purchase order this intake is against, so the receipt it raises links back to the PO. */
    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private SlipStatus status = SlipStatus.DRAFT;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 200)
    private String driverName;

    @Column(name = "total_quantity", precision = 18, scale = 4)
    private BigDecimal totalQuantity = BigDecimal.ZERO;

    @Column(name = "accepted_quantity", precision = 18, scale = 4)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", precision = 18, scale = 4)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(length = 1000)
    private String notes;

    // ── Agricultural Lot-Wise Intake Fields ──────────────────────────────────

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "delivery_type", length = 50)
    private String deliveryType;

    @Column(name = "tc_name", length = 100)
    private String tcName;

    @Column(name = "field_producer_id")
    private java.util.UUID fieldProducerId;

    @Column(name = "field_producer_name", length = 200)
    private String fieldProducerName;

    @Column(name = "plant_variant_id")
    private java.util.UUID plantVariantId;

    @Column(name = "plant_variant_name", length = 200)
    private String plantVariantName;

    @Column(name = "temporary_lot_number", length = 50)
    private String temporaryLotNumber;

    @Column(name = "father_name", length = 200)
    private String fatherName;

    @Column(name = "village", length = 100)
    private String village;

    @Column(name = "inward_gate_pass_number", length = 50)
    private String inwardGatePassNumber;

    @Column(name = "weighbridge_quantity", precision = 15, scale = 3)
    private BigDecimal weighbridgeQuantity;

    @Column(name = "godown_name", length = 100)
    private String godownName;

    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Column(name = "stack_number", length = 50)
    private String stackNumber;

    @Column(name = "hamali_contractor", length = 100)
    private String hamaliContractor;

    @Column(name = "empty_gunny_weight_kg", precision = 15, scale = 3)
    private BigDecimal emptyGunnyWeightKg;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "input_type", length = 50)
    private String inputType;

    @Column(name = "moisture_percent", precision = 5, scale = 2)
    private BigDecimal moisturePercent;

    @Column(name = "unloading_slip_number", length = 50)
    private String unloadingSlipNumber;

    /** STANDARD or INDIVIDUAL */
    @Column(name = "bag_weight_mode", length = 20)
    private String bagWeightMode;

    @Column(name = "bag_type", length = 50)
    private String bagType;

    @Column(name = "bag_size_kg", length = 20)
    private String bagSizeKg;

    @Column(name = "quantity_per_bag", precision = 10, scale = 3)
    private BigDecimal quantityPerBag;

    @Column(name = "number_of_bags")
    private Integer numberOfBags;

    @OneToMany(mappedBy = "slip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeSlipItem> items = new ArrayList<>();

    public enum SlipStatus {
        DRAFT, QUALITY_PENDING, QUALITY_PASSED, QUALITY_FAILED, COMPLETED
    }
}
