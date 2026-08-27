package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "waste_types", indexes = {@Index(name = "idx_waste_type_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WasteType extends TenantEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active")
    private boolean active = true;
}
