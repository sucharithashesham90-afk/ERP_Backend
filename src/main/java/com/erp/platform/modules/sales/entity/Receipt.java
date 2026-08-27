package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receipts",
       indexes = {@Index(name = "idx_receipt_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Receipt extends TenantEntity {

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ReceiptStatus status = ReceiptStatus.COMPLETED;

    @Column(length = 1000)
    private String notes;

    public enum ReceiptStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
