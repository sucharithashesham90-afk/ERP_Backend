package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_intakes",
       indexes = {
           @Index(name = "idx_pi_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pi_number", columnList = "tenant_id,intake_number", unique = true),
           @Index(name = "idx_pi_type",   columnList = "tenant_id,intake_type")
       })
@Getter
@Setter
public class ProductionIntake extends TenantEntity {

    @Column(name = "intake_number", nullable = false, length = 50)
    private String intakeNumber;

    @Column(name = "intake_type", nullable = false, length = 20)
    private String intakeType; // LOT_WISE, TRUCK_WISE, THIRD_PARTY

    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "storage_location_name", length = 100)
    private String storageLocationName; // godown

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber; // for TRUCK_WISE

    @Column(name = "party_name", length = 200)
    private String partyName; // for THIRD_PARTY

    @Column(name = "organizer", length = 200)
    private String organizer;

    @Column(name = "supervisor", length = 200)
    private String supervisor;

    @Column(name = "input_type", length = 50)
    private String inputType; // Raw, Certified, Foundation, etc.

    @Column(name = "moisture_percent", precision = 5, scale = 2)
    private BigDecimal moisturePercent;

    @Column(name = "inward_gate_pass_number", length = 50)
    private String inwardGatePassNumber;

    @Column(name = "unloading_slip_number", length = 50)
    private String unloadingSlipNumber;

    @Column(name = "stack_number", length = 50)
    private String stackNumber;

    @Column(name = "weighbridge_quantity", precision = 10, scale = 3)
    private BigDecimal weighbridgeQuantity = BigDecimal.ZERO;

    @Column(name = "bag_weight_type", length = 20)
    private String bagWeightType = "STANDARD"; // STANDARD, INDIVIDUAL

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(precision = 20, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "KG";

    @Column(name = "pack_type", length = 50)
    private String packType;

    @Column(name = "pack_size", precision = 10, scale = 4)
    private BigDecimal packSize;

    @Column(name = "net_bags")
    private Integer netBags;

    @Column(name = "qty_per_bag", precision = 10, scale = 3)
    private BigDecimal qtyPerBag;

    @Column(name = "empty_bags_weight", precision = 10, scale = 3)
    private BigDecimal emptyBagsWeight = BigDecimal.ZERO;

    @Column(name = "total_quantity_kg", precision = 20, scale = 4)
    private BigDecimal totalQuantityKg = BigDecimal.ZERO;

    @Column(name = "net_quantity_kg", precision = 20, scale = 4)
    private BigDecimal netQuantityKg = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "DRAFT"; // DRAFT, RECEIVED, VERIFIED

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "intake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeWeightDeduction> weightDeductions = new ArrayList<>();

    @OneToMany(mappedBy = "intake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeLine> intakeLines = new ArrayList<>();

    @Column(name = "hamal_contractor", length = 200)
    private String hamalContractor;

    @Column(name = "rejected_bags")
    private Integer rejectedBags;

    @Column(name = "return_type", length = 50)
    private String returnType;

    @Column(name = "purchase_order_ref", length = 100)
    private String purchaseOrderRef;

    @Column(name = "freight_total", precision = 18, scale = 2)
    private BigDecimal freightTotal = BigDecimal.ZERO;

    @Column(name = "freight_paid_adv", precision = 18, scale = 2)
    private BigDecimal freightPaidAdv = BigDecimal.ZERO;

    @Column(name = "freight_to_pay", precision = 18, scale = 2)
    private BigDecimal freightToPay = BigDecimal.ZERO;

    @Column(name = "in_gate_pass_date")
    private java.time.LocalDate inGatePassDate;

    @Column(name = "customer_dc_number", length = 100)
    private String customerDcNumber;

    @Column(name = "customer_dc_date")
    private java.time.LocalDate customerDcDate;

    @Column(name = "is_direct_return", columnDefinition = "boolean not null default false")
    private boolean directReturn = false;

    @Column(name = "is_free_sample_return", columnDefinition = "boolean not null default false")
    private boolean freeSampleReturn = false;

    @Column(name = "tests_selected", columnDefinition = "TEXT")
    private String testsSelected;

    @Column(name = "transport", length = 200)
    private String transport;
}
