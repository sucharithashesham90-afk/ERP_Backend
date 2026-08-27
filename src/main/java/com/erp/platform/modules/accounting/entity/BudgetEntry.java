package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budget_entries",
       indexes = {@Index(name = "idx_budget_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_budget_year_month", columnList = "tenant_id, period_year, period_month")})
@Getter
@Setter
public class BudgetEntry extends TenantEntity {

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_code", length = 30)
    private String accountCode;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "dimension_value_id")
    private UUID dimensionValueId;

    @Column(name = "budget_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "actual_amount", precision = 18, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "variance", precision = 18, scale = 2)
    private BigDecimal variance;

    @Column(length = 500)
    private String notes;

    @PrePersist
    @PreUpdate
    public void computeVariance() {
        if (budgetAmount != null && actualAmount != null) {
            this.variance = budgetAmount.subtract(actualAmount);
        }
    }
}
