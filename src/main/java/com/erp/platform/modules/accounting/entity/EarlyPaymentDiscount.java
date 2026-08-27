package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "early_payment_discounts",
       indexes = {
           @Index(name = "idx_epd_tenant", columnList = "tenant_id"),
           @Index(name = "idx_epd_invoice", columnList = "invoice_id"),
           @Index(name = "idx_epd_customer", columnList = "customer_id, applied")
       })
@Getter
@Setter
public class EarlyPaymentDiscount extends TenantEntity {

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "payment_term_id")
    private UUID paymentTermId;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "discount_eligible_until")
    private LocalDate discountEligibleUntil;

    @Column(name = "invoice_amount", precision = 18, scale = 2)
    private BigDecimal invoiceAmount = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 8, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean applied = false;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @Column(name = "voucher_reference", length = 100)
    private String voucherReference;
}
