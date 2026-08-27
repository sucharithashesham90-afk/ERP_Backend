package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "goods_receipts",
       indexes = {@Index(name = "idx_grn_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class GoodsReceipt extends TenantEntity {

    @Column(name = "grn_number", nullable = false, length = 50)
    private String grnNumber;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    /**
     * Set when this receipt was raised automatically on completion of an intake slip.
     * Also keeps that generation idempotent — one receipt per slip.
     */
    @Column(name = "source_intake_slip_id")
    private UUID sourceIntakeSlipId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(length = 20)
    private String status;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "lr_number", length = 50)
    private String lrNumber;

    @Column(name = "dc_number", length = 50)
    private String dcNumber;

    @Column(name = "dc_date")
    private LocalDate dcDate;

    @Column(name = "freight_carrier_name", length = 200)
    private String freightCarrierName;

    @Column(name = "freight_amount", precision = 18, scale = 2)
    private java.math.BigDecimal freightAmount;

    @Column(name = "freight_advance_paid", precision = 18, scale = 2)
    private java.math.BigDecimal freightAdvancePaid;

    @Column(name = "in_gate_pass", length = 50)
    private String inGatePass;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptItem> items = new ArrayList<>();
}
