package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts",
       indexes = {
           @Index(name = "idx_account_tenant", columnList = "tenant_id"),
           @Index(name = "idx_account_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class Account extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String type; // ASSET, LIABILITY, EQUITY, INCOME, EXPENSE

    @Column(name = "sub_type", length = 30)
    private String subType;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String description;

    // ---- Ledger definition (per Accounting module spec) ----

    /** Account group this ledger is filed under (group definition code / name). */
    @Column(name = "group_code", length = 30)
    private String groupCode;

    @Column(name = "group_name", length = 100)
    private String groupName;

    /** Ledger code shown on the ledger screen (distinct from the short chart-of-accounts code). */
    @Column(name = "ledger_code", length = 30)
    private String ledgerCode;

    /** Sales area (populated from the Sales module). */
    @Column(name = "sales_area", length = 100)
    private String salesArea;

    /** Overdraft flag; when true, {@link #overdraftAmount} is captured. */
    @Column(name = "overdraft", nullable = false, columnDefinition = "boolean not null default false")
    private boolean overdraft = false;

    @Column(name = "overdraft_amount", precision = 18, scale = 2)
    private BigDecimal overdraftAmount = BigDecimal.ZERO;

    /** Account division (e.g. "All"). */
    @Column(name = "account_division", length = 50)
    private String accountDivision;

    /** Bank ledger flag; enables bank payment / receipt. */
    @Column(name = "is_bank", nullable = false, columnDefinition = "boolean not null default false")
    private boolean bank = false;

    /** Bank code, captured when {@link #bank} is true. */
    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    /** Contra ledger flag. */
    @Column(name = "is_contra", nullable = false, columnDefinition = "boolean not null default false")
    private boolean contra = false;

    /** Show opening balance on Trial Balance / Balance Sheet. */
    @Column(name = "show_opening_balance", nullable = false, columnDefinition = "boolean not null default false")
    private boolean showOpeningBalance = false;

    /** Central ledger: shown everywhere irrespective of group. */
    @Column(name = "is_central", nullable = false, columnDefinition = "boolean not null default false")
    private boolean central = false;

    /** Ledger follows a sub-account structure. */
    @Column(name = "sub_account", nullable = false, columnDefinition = "boolean not null default false")
    private boolean subAccount = false;

    /** Home location (populated from the Locations screen). */
    @Column(name = "location", length = 100)
    private String location;
}
