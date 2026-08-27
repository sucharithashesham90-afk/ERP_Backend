package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_terms",
       indexes = {
           @Index(name = "idx_pterm_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pterm_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class PaymentTerm extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(name = "due_days")
    private int dueDays = 30;

    @Column(name = "discount_days")
    private int discountDays = 0;

    @Column(name = "discount_percent", precision = 8, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "has_early_payment_discount", nullable = false)
    private boolean hasEarlyPaymentDiscount = false;

    @Column(name = "penalty_percent", precision = 8, scale = 4)
    private BigDecimal penaltyPercent = BigDecimal.ZERO;

    @Column(name = "penalty_after_days")
    private int penaltyAfterDays = 0;

    @Column(nullable = false)
    private boolean active = true;
}
