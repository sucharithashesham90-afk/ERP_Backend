package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_reconciliations",
       indexes = {
           @Index(name = "idx_bankrecon_tenant", columnList = "tenant_id"),
           @Index(name = "idx_bankrecon_account", columnList = "bank_account_id")
       })
@Getter
@Setter
public class BankReconciliation extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BankAccount bankAccount;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "statement_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal statementBalance = BigDecimal.ZERO;

    @Column(name = "system_balance", precision = 18, scale = 2)
    private BigDecimal systemBalance = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal difference = BigDecimal.ZERO;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ReconciliationStatus status = ReconciliationStatus.DRAFT;

    @Column(length = 1000)
    private String notes;

    public enum ReconciliationStatus {
        DRAFT, IN_PROGRESS, RECONCILED
    }
}
