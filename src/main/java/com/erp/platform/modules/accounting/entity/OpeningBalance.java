package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "opening_balances",
       indexes = {
           @Index(name = "idx_ob_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ob_migration_ref", columnList = "tenant_id, migration_ref"),
           @Index(name = "idx_ob_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class OpeningBalance extends TenantEntity {

    @Column(name = "migration_ref", length = 50)
    private String migrationRef;

    @Column(name = "balance_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private BalanceType balanceType;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "account_code", length = 30)
    private String accountCode;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "party_type", length = 20)
    private String partyType;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "debit_amount", precision = 18, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 18, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency = "INR";

    @Column(length = 100)
    private String reference;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MigrationStatus status = MigrationStatus.DRAFT;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(length = 1000)
    private String notes;

    public enum BalanceType {
        GENERAL_LEDGER, ACCOUNTS_RECEIVABLE, ACCOUNTS_PAYABLE, INVENTORY, FIXED_ASSET, BANK
    }

    public enum MigrationStatus {
        DRAFT, VALIDATED, POSTED, REVERSED
    }
}
