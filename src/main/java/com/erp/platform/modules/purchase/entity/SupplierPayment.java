package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "supplier_payments",
       indexes = {@Index(name = "idx_spay_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SupplierPayment extends TenantEntity {

    public enum PaymentMethod {
        BANK_TRANSFER, CHEQUE, CASH, UPI, NEFT, RTGS, DD
    }

    public enum PayStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, PROCESSED, RECONCILED, CANCELLED
    }

    @Column(name = "payment_number", nullable = false, length = 50)
    private String paymentNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "purchase_invoice_id")
    private UUID purchaseInvoiceId;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency = "INR";

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "bank_account_id")
    private UUID bankAccountId;

    @Column(name = "bank_account_name", length = 200)
    private String bankAccountName;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "tds_percent", precision = 5, scale = 2)
    private BigDecimal tdsPercent = BigDecimal.ZERO;

    @Column(name = "net_payment", precision = 18, scale = 2)
    private BigDecimal netPayment;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PayStatus status = PayStatus.DRAFT;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierPaymentAllocation> allocations = new ArrayList<>();
}
