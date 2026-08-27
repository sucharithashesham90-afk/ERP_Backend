package com.erp.platform.modules.sales.entity;

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
@Table(name = "invoices",
       indexes = {
           @Index(name = "idx_invoice_tenant", columnList = "tenant_id"),
           @Index(name = "idx_invoice_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class Invoice extends TenantEntity {

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 18, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(name = "freight_charges", precision = 18, scale = 2)
    private BigDecimal freightCharges = BigDecimal.ZERO;

    @Column(name = "freight_paid_advance", precision = 18, scale = 2)
    private BigDecimal freightPaidAdvance = BigDecimal.ZERO;

    @Column(name = "packing_forwarding", precision = 18, scale = 2)
    private BigDecimal packingForwarding = BigDecimal.ZERO;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "surcharge_amount", precision = 18, scale = 2)
    private BigDecimal surchargeAmount = BigDecimal.ZERO;

    @Column(name = "rounded_value", precision = 18, scale = 2)
    private BigDecimal roundedValue = BigDecimal.ZERO;

    @Column(name = "balance_after_submission", precision = 18, scale = 2)
    private BigDecimal balanceAfterSubmission = BigDecimal.ZERO;

    @Column(name = "sales_area", length = 100)
    private String salesArea;

    @Column(name = "from_location", length = 200)
    private String fromLocation;

    @Column(name = "lorry_number", length = 50)
    private String lorryNumber;

    @Column(name = "way_bill_number", length = 50)
    private String wayBillNumber;

    @Column(name = "rr_rl_number", length = 50)
    private String rrRlNumber;

    @Column(name = "carrier", length = 200)
    private String carrier;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "dc_comments", length = 500)
    private String dcComments;

    /** Set when the invoice was auto-created from a dispatch challan; locks line composition. */
    @Column(name = "dispatch_challan_number", length = 60)
    private String dispatchChallanNumber;

    @Column(name = "invoice_comments", length = 500)
    private String invoiceComments;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(length = 500)
    private String subject;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    // ---- Ledger posting on completion (per Sales module spec) ----

    /** Whether this invoice has been completed and posted to the ledgers. */
    @Column(name = "posted", nullable = false, columnDefinition = "boolean not null default false")
    private boolean posted = false;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @Column(name = "journal_entry_number", length = 40)
    private String journalEntryNumber;

    public enum InvoiceStatus {
        DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, COMPLETED
    }
}
