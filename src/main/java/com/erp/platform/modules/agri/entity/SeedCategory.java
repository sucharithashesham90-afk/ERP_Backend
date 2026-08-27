package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seed_categories", indexes = {@Index(name = "idx_seed_category_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SeedCategory extends TenantEntity {

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active")
    private boolean active = true;
}
