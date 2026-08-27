package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_statements",
       indexes = {
           @Index(name = "idx_bankstmt_tenant", columnList = "tenant_id"),
           @Index(name = "idx_bankstmt_recon", columnList = "reconciliation_id")
       })
@Getter
@Setter
public class BankStatement extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BankReconciliation reconciliation;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String reference;

    @Column(name = "debit_amount", precision = 18, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 18, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean matched = false;

    @Column(name = "matched_voucher_id")
    private UUID matchedVoucherId;
}
