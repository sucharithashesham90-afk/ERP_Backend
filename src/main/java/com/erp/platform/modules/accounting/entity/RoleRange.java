package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Role Range: the maximum transaction amount (in INR) a role is authorised to post to the ledgers.
 * The role list is sourced from the admin/roles screen.
 */
@Entity
@Table(name = "role_ranges",
       indexes = {@Index(name = "idx_rr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class RoleRange extends TenantEntity {

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /** Maximum posting amount (INR) allowed for this role. */
    @Column(name = "range_inr", precision = 18, scale = 2)
    private BigDecimal rangeInr = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
