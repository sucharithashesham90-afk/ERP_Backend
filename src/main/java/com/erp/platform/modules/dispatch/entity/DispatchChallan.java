package com.erp.platform.modules.dispatch.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "AgriDispatchChallan")
@Table(name = "agri_dispatch_challans", indexes = {@Index(name = "idx_dispatch_challan_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DispatchChallan extends TenantEntity {

    @Column(name = "challan_number", length = 100)
    private String challanNumber;

    @Column(name = "challan_date")
    private LocalDate challanDate;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "quantity_kgs", precision = 15, scale = 3)
    private BigDecimal quantityKgs;

    @Column(name = "transporter_name", length = 200)
    private String transporterName;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "remarks", length = 500)
    private String remarks;

    /** Dispatch value used to raise the sales invoice on dispatch. */
    @Column(name = "value", precision = 18, scale = 2)
    private BigDecimal value;

    /** Set once the challan is dispatched: the auto-created sales invoice. */
    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "invoice_number", length = 40)
    private String invoiceNumber;

    // ── Order link ──
    // A challan is raised against a sales order. The delivery-order columns stay for the challans
    // already filed against one; nothing writes them from the screen any more.
    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "sales_order_number", length = 100)
    private String salesOrderNumber;

    @Column(name = "delivery_order_id")
    private UUID deliveryOrderId;

    @Column(name = "delivery_order_number", length = 100)
    private String deliveryOrderNumber;

    // ── Dispatch header ──
    @Column(name = "sales_area", length = 200)
    private String salesArea;
    @Column(name = "dispatch_location_id", length = 100)
    private String dispatchLocationId;
    @Column(name = "dispatch_location", length = 200)
    private String dispatchLocation;
    @Column(name = "way_bill_no", length = 100)
    private String wayBillNo;
    @Column(name = "rr_rl_no", length = 100)
    private String rrRlNo;

    // ── Freight ──
    @Column(name = "freight_carrier_id", length = 100)
    private String freightCarrierId;
    @Column(name = "freight_carrier", length = 200)
    private String freightCarrier;
    @Column(name = "other_carrier")
    private Boolean otherCarrier;
    @Column(name = "carrier", length = 200)
    private String carrier;
    @Column(name = "lorry_no", length = 50)
    private String lorryNo;
    @Column(name = "freight_amount", precision = 18, scale = 2)
    private BigDecimal freightAmount;
    @Column(name = "freight_paid_advance", precision = 18, scale = 2)
    private BigDecimal freightPaidAdvance;
    @Column(name = "freight_to_pay", precision = 18, scale = 2)
    private BigDecimal freightToPay;

    // ── Billing address ──
    @Column(name = "billing_address", length = 500)
    private String billingAddress;
    @Column(name = "billing_state", length = 120)
    private String billingState;
    @Column(name = "billing_district", length = 120)
    private String billingDistrict;
    @Column(name = "billing_city", length = 120)
    private String billingCity;
    @Column(name = "billing_zip", length = 20)
    private String billingZip;
    @Column(name = "billing_phone", length = 40)
    private String billingPhone;

    // ── Supplier address ──
    @Column(name = "supplier_address", length = 500)
    private String supplierAddress;
    @Column(name = "supplier_state", length = 120)
    private String supplierState;
    @Column(name = "supplier_district", length = 120)
    private String supplierDistrict;
    @Column(name = "supplier_city", length = 120)
    private String supplierCity;
    @Column(name = "supplier_zip", length = 20)
    private String supplierZip;
    @Column(name = "supplier_phone", length = 40)
    private String supplierPhone;

    // ── Line items (one product / lot / quantity per row) ──
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agri_dispatch_challan_items", joinColumns = @JoinColumn(name = "challan_id"))
    private List<DispatchChallanLine> items = new ArrayList<>();
}
