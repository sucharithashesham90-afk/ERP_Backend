package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customer_advances",
       indexes = {
           @Index(name = "idx_ca_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ca_customer", columnList = "tenant_id, customer_id"),
           @Index(name = "idx_ca_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class CustomerAdvance extends TenantEntity {

    @Column(name = "advance_number", nullable = false, length = 50)
    private String advanceNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_mode", length = 30)
    private String paymentMode; // CASH, CHEQUE, BANK_TRANSFER, UPI

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "amount_applied", precision = 18, scale = 2)
    private BigDecimal amountApplied = BigDecimal.ZERO;

    @Column(name = "amount_available", precision = 18, scale = 2)
    private BigDecimal amountAvailable = BigDecimal.ZERO;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private AdvanceStatus status = AdvanceStatus.AVAILABLE;

    @Column(length = 1000)
    private String notes;

    public enum AdvanceStatus {
        AVAILABLE, PARTIALLY_APPLIED, FULLY_APPLIED, REFUNDED
    }
}
