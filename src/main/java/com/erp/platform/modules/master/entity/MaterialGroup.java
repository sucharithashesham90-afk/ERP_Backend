package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "material_groups",
       indexes = {@Index(name = "idx_matgrp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class MaterialGroup extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    /** Self-referential parent group (drop-down populated from material groups). */
    @Column(name = "parent_group_id")
    private UUID parentGroupId;

    @Column(name = "parent_group_name", length = 150)
    private String parentGroupName;

    @Column(name = "batch_wise_receipt", columnDefinition = "boolean not null default false")
    private boolean batchWiseReceipt = false;

    @Column(name = "has_expiry_period", columnDefinition = "boolean not null default false")
    private boolean hasExpiryPeriod = false;

    /** Default expiry date, enabled when hasExpiryPeriod is set. */
    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "sellable", columnDefinition = "boolean not null default false")
    private boolean sellable = false;

    /** UOM category (drop-down populated from UOM master). */
    @Column(name = "uom_category_id")
    private UUID uomCategoryId;

    @Column(name = "uom_category_name", length = 100)
    private String uomCategoryName;

    @Column(nullable = false)
    private boolean active = true;
}
