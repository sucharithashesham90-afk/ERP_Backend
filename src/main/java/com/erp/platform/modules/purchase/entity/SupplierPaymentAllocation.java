package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "supplier_payment_allocations",
       indexes = {@Index(name = "idx_spay_alloc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SupplierPaymentAllocation extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnore
    private SupplierPayment payment;

    @Column(name = "purchase_invoice_id")
    private UUID purchaseInvoiceId;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_amount", precision = 18, scale = 2)
    private BigDecimal invoiceAmount;

    @Column(name = "allocated_amount", precision = 18, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "outstanding_after", precision = 18, scale = 2)
    private BigDecimal outstandingAfter;
}
