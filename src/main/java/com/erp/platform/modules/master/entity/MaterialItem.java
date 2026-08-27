package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "material_items",
       indexes = {
           @Index(name = "idx_matitem_tenant",   columnList = "tenant_id"),
           @Index(name = "idx_matitem_material", columnList = "material_id")
       })
@Getter
@Setter
public class MaterialItem extends TenantEntity {

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "material_name", length = 200)
    private String materialName;

    @Column(name = "material_group_id")
    private UUID materialGroupId;

    @Column(name = "material_group_name", length = 200)
    private String materialGroupName;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String unit;

    @Column(name = "batch_wise_receipt", columnDefinition = "boolean not null default false")
    private boolean batchWiseReceipt = false;

    @Column(name = "has_expiry_period", columnDefinition = "boolean not null default false")
    private boolean hasExpiryPeriod = false;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "sellable", columnDefinition = "boolean not null default false")
    private boolean sellable = false;

    @Column(nullable = false)
    private boolean active = true;
}
