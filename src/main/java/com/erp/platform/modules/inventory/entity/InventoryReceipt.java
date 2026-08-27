package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Inventory receipt (stock received into a godown). Backs /api/v1/inventory/receipts. */
@Entity
@Table(name = "inventory_receipts",
       indexes = {@Index(name = "idx_invreceipt_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class InventoryReceipt extends TenantEntity {

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(length = 150)
    private String location;

    @Column(name = "godown_id")
    private UUID godownId;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(name = "net_id")
    private UUID netId;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    /** Reference to the purchase order being received against. poNo is the display number for it. */
    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "po_no", length = 60)
    private String poNo;

    @Column(name = "received_by", length = 200)
    private String receivedBy;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(name = "in_gate_pass", length = 60)
    private String inGatePass;

    @Column(name = "pack_details", length = 500)
    private String packDetails;

    @Column(name = "truck_number", length = 40)
    private String truckNumber;

    @Column(name = "dc_no", length = 60)
    private String dcNo;

    @Column(name = "dc_date")
    private LocalDate dcDate;

    @Column(length = 200)
    private String carrier;

    @Column(name = "lr_no", length = 60)
    private String lrNo;

    // Audited quantity received — set once and never recalculated (audit record, does not drive stock).
    @Column(name = "quantity", precision = 18, scale = 4)
    private java.math.BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(length = 20)
    private String status = "RECEIVED";
}
