package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "packaging_bags",
        indexes = {@Index(name = "idx_packaging_bag_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackagingBag extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    /** FK to units_of_measure (master). Authoritative link for the purchase unit. */
    @Column(name = "purchase_unit_uom_id")
    private UUID purchaseUnitUomId;

    /** Denormalized display label of the purchase UoM, captured at selection time. */
    @Column(length = 100)
    private String purchaseUnit;

    @Column(nullable = false)
    private boolean active = true;
}
