package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Sales Area Level (per the Sales module spec): a named level in the sales-area hierarchy, with a
 * parent sales area and the roles that operate at that level.
 */
@Entity
@Table(name = "sales_area_levels",
       indexes = {@Index(name = "idx_sal_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SalesAreaLevel extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    /** Parent sales area (from the sales area screen). */
    @Column(name = "parent_sales_area_id")
    private UUID parentSalesAreaId;

    @Column(name = "parent_sales_area_name", length = 200)
    private String parentSalesAreaName;

    /** Roles that operate at this level, stored as a comma-separated list of role names. */
    @Column(length = 500)
    private String roles;

    @Column(nullable = false)
    private boolean active = true;
}
