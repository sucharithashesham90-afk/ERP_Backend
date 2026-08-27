package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Per-ledger, per-location opening balance (the location-wise list on the Ledger screen).
 * A ledger may carry at most one opening-balance row per location.
 */
@Entity
@Table(name = "ledger_opening_balances",
       indexes = {
           @Index(name = "idx_lob_tenant", columnList = "tenant_id"),
           @Index(name = "idx_lob_account", columnList = "tenant_id, account_id")
       })
@Getter
@Setter
public class LedgerOpeningBalance extends TenantEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(length = 100)
    private String location;

    /** Yes/No mirror of the ledger's sub-account structure at the time of saving. */
    @Column(name = "sub_account", nullable = false)
    private boolean subAccount = false;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /** DEBIT or CREDIT. */
    @Column(name = "balance_type", length = 10)
    private String balanceType = "DEBIT";

    @Column(name = "open_bal_date")
    private LocalDate openBalDate;

    /** Whether this opening balance is shown at that particular location. */
    @Column(nullable = false)
    private boolean visible = true;
}
