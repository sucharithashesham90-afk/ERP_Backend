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
@Table(name = "purchase_quotations",
       indexes = {
           @Index(name = "idx_pq_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pq_req", columnList = "tenant_id, requisition_id")
       })
@Getter
@Setter
public class PurchaseQuotation extends TenantEntity {

    @Column(name = "quotation_number", nullable = false, length = 50)
    private String quotationNumber;

    @Column(name = "requisition_id")
    private UUID requisitionId;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PQStatus status = PQStatus.RECEIVED;

    /** SEED / CONSUMABLE / PACKING_MATERIAL — mirrors PurchaseOrder.poType, drives what the line
     *  items look like (crop cascade vs. a product picker) on both the RFQ and Vendor Quotations
     *  screens, which share this one entity. */
    @Column(name = "quotation_type", length = 30)
    private String quotationType;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "delivery_days")
    private int deliveryDays;

    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseQuotationItem> items = new ArrayList<>();

    public enum PQStatus {
        RECEIVED, SHORTLISTED, SELECTED, REJECTED
    }
}
