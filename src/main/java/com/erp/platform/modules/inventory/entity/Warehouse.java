package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warehouses",
       indexes = {@Index(name = "idx_warehouse_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Warehouse extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "location", length = 150)
    private String location;
}
