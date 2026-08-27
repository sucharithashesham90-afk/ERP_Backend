package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Discount definition: a named discount configuration mapped to a ledger, so discounts posted
 * anywhere in the application settle against the configured ledger. Any number can be defined.
 */
@Entity
@Table(name = "discount_definitions",
       indexes = {@Index(name = "idx_dd_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DiscountDefinition extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "ledger_id")
    private UUID ledgerId;

    @Column(name = "ledger_name", length = 200)
    private String ledgerName;

    @Column(name = "ledger_code", length = 30)
    private String ledgerCode;

    @Column(nullable = false)
    private boolean active = true;
}
