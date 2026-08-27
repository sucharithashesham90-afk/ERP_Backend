package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "purchase_invoices",
       indexes = {
           @Index(name = "idx_pi_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pi_vendor", columnList = "tenant_id, vendor_id"),
           @Index(name = "idx_pi_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class PurchaseInvoice extends TenantEntity {

    @Column(name = "pi_number", nullable = false, length = 50)
    private String piNumber;

    @Column(name = "vendor_invoice_number", length = 100)
    private String vendorInvoiceNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "goods_receipt_id")
    private UUID goodsReceiptId;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PIStatus status = PIStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "freight_charges", precision = 18, scale = 2)
    private BigDecimal freightCharges = BigDecimal.ZERO;

    // editable charges per Purchase doc (invoice detail)
    @Column(name = "excise_duty", precision = 18, scale = 2)
    private BigDecimal exciseDuty = BigDecimal.ZERO;

    @Column(name = "pf_charge", precision = 18, scale = 2)
    private BigDecimal pfCharge = BigDecimal.ZERO;

    // TDS (Tax Deducted at Source) fields
    @Column(name = "tds_applicable", nullable = false)
    private boolean tdsApplicable = false;

    @Column(name = "tds_section", length = 50)
    private String tdsSection; // e.g., 194C, 194J, 194H

    @Column(name = "tds_percent", precision = 8, scale = 4)
    private BigDecimal tdsPercent = BigDecimal.ZERO;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "net_payable", precision = 18, scale = 2)
    private BigDecimal netPayable = BigDecimal.ZERO; // totalAmount - tdsAmount

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 18, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(length = 1000)
    private String notes;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    public enum PIStatus {
        DRAFT, APPROVED, PARTIALLY_PAID, PAID, CANCELLED
    }
}
