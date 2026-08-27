package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Packing type master (Purchase Configuration → Packing Type). */
@Entity
@Table(name = "packing_types", indexes = {@Index(name = "idx_packingtype_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackingType extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
