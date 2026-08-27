package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Bag size master (Purchase Configuration → Bag Size): a size for a bag, with a purchase unit. */
@Entity
@Table(name = "bag_sizes", indexes = {@Index(name = "idx_bagsize_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class BagSize extends TenantEntity {

    @Column(name = "bag_id")
    private UUID bagId;

    @Column(name = "bag_name", length = 200)
    private String bagName;

    @Column(nullable = false, length = 100)
    private String size;

    /** FK to units_of_measure (master). Authoritative link for the purchase unit. */
    @Column(name = "purchase_unit_uom_id")
    private UUID purchaseUnitUomId;

    /** Denormalized display label of the purchase UoM, captured at selection time. */
    @Column(name = "purchase_unit", length = 50)
    private String purchaseUnit;

    @Column(nullable = false)
    private boolean active = true;
}
