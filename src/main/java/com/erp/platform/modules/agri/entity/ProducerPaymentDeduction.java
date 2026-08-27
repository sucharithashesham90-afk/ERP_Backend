package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "producer_payment_deductions",
       indexes = {@Index(name = "idx_ppd_payment", columnList = "payment_id"),
                  @Index(name = "idx_ppd_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProducerPaymentDeduction extends TenantEntity {

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "possible_deduction_id")
    private UUID possibleDeductionId;

    @Column(name = "deduction_name", length = 100, nullable = false)
    private String deductionName;

    /**
     * PERCENTAGE — amount = grossAmount * rate / 100
     * PER_UNIT   — amount = quantity * rate
     * WEIGHT     — amount = weightKg * ratePerKg
     * FIXED      — amount is a flat fixed value
     */
    @Column(name = "deduction_type", length = 30)
    private String deductionType;

    @Column(name = "rate", precision = 15, scale = 4)
    private BigDecimal rate;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "remarks", length = 200)
    private String remarks;
}
