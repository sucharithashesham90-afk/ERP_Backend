package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "closing_stock_balances",
       indexes = {@Index(name = "idx_csb_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ClosingStockBalance extends TenantEntity {

    @Column(name = "period_code", nullable = false, length = 30)
    private String periodCode;

    @Column(name = "period_name", length = 100)
    private String periodName;

    @Column(name = "account_code", nullable = false, length = 30)
    private String accountCode;

    @Column(name = "account_name", length = 100)
    private String accountName;

    @Column(name = "closing_balance", precision = 20, scale = 4)
    private BigDecimal closingBalance = BigDecimal.ZERO;

    @Column(name = "balance_date")
    private LocalDate balanceDate;

    @Column(length = 20)
    private String status = "DRAFT"; // DRAFT, FINALIZED

    @Column(length = 500)
    private String narration;
}
