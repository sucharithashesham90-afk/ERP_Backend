package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates",
       indexes = {
           @Index(name = "idx_er_tenant", columnList = "tenant_id"),
           @Index(name = "idx_er_currencies", columnList = "tenant_id, base_currency, target_currency, effective_date")
       })
@Getter
@Setter
public class ExchangeRate extends TenantEntity {

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 200)
    private String notes;
}
