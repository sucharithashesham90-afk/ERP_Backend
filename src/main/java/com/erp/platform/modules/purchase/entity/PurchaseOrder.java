package com.erp.platform.modules.purchase.entity;

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
@Table(name = "purchase_orders",
       indexes = {
           @Index(name = "idx_puord_tenant", columnList = "tenant_id"),
           @Index(name = "idx_puord_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class PurchaseOrder extends TenantEntity {

    @Column(name = "po_number", nullable = false, length = 50)
    private String poNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(length = 25)
    @Enumerated(EnumType.STRING)
    private POStatus status = POStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "freight_charges", precision = 18, scale = 2)
    private BigDecimal freightCharges = BigDecimal.ZERO;

    @Column(length = 200)
    private String subject;

    @Column(name = "quotation_reference", length = 100)
    private String quotationReference;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "delivery_terms", length = 500)
    private String deliveryTerms;

    @Column(name = "quality_terms", length = 500)
    private String qualityTerms;

    // ── Seed-industry PO header (per Purchase doc) ──
    @Column(name = "po_type", length = 30)
    private String poType;                 // seed / consumable / packing material

    @Column(name = "seed_type", length = 100)
    private String seedType;

    @Column(name = "seed_state", length = 100)
    private String seedState;

    @Column(length = 150)
    private String location;

    @Column(name = "delivery_state", length = 100)
    private String deliveryState;
    @Column(name = "delivery_district", length = 100)
    private String deliveryDistrict;
    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;
    @Column(name = "delivery_zip", length = 20)
    private String deliveryZip;
    @Column(name = "delivery_phone", length = 30)
    private String deliveryPhone;

    @Column(name = "supplier_address", length = 500)
    private String supplierAddress;
    @Column(name = "supplier_state", length = 100)
    private String supplierState;
    @Column(name = "supplier_district", length = 100)
    private String supplierDistrict;
    @Column(name = "supplier_city", length = 100)
    private String supplierCity;
    @Column(name = "supplier_zip", length = 20)
    private String supplierZip;
    @Column(name = "supplier_phone", length = 30)
    private String supplierPhone;

    @Column(name = "supplier_ref", length = 100)
    private String supplierRef;
    @Column(name = "validity_of_order", length = 100)
    private String validityOfOrder;
    @Column(name = "insurance_details", length = 500)
    private String insuranceDetails;
    @Column(name = "other_terms", length = 1000)
    private String otherTerms;
    @Column(name = "signature_by", length = 150)
    private String signatureBy;
    @Column(name = "advance_percent", length = 20)
    private String advancePercent;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public enum POStatus {
        DRAFT, SENT, CONFIRMED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED
    }
}
