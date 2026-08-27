package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_groups",
       indexes = {
           @Index(name = "idx_ag_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ag_code",   columnList = "tenant_id,code", unique = true)
       })
@Getter
@Setter
public class AccountGroup extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "group_type", length = 30)
    private String groupType; // ASSET, LIABILITY, INCOME, EXPENSE, CAPITAL

    @Column(name = "parent_group_code", length = 30)
    private String parentGroupCode;

    /** Primary (top-level) group. When false, a parent group must be assigned. */
    @Column(name = "is_primary", nullable = false, columnDefinition = "boolean not null default true")
    private boolean primary = true;

    /** Whether this group's balance affects the Gross Profit computation. */
    @Column(name = "effects_gross_profit", nullable = false, columnDefinition = "boolean not null default false")
    private boolean effectsGrossProfit = false;

    /** Whether ledgers under this group follow a sub-account structure. */
    @Column(name = "sub_account", nullable = false, columnDefinition = "boolean not null default false")
    private boolean subAccount = false;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
