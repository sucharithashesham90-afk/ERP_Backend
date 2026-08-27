package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "InventoryLocation")
@Table(name = "inventory_locations",
       indexes = {
           @Index(name = "idx_inv_loc_tenant", columnList = "tenant_id"),
           @Index(name = "idx_inv_loc_code",   columnList = "tenant_id, code")
       })
@Getter
@Setter
public class Location extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
