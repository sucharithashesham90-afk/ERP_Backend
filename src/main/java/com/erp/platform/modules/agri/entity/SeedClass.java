package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seed_classes", indexes = {@Index(name = "idx_seed_class_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SeedClass extends TenantEntity {

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "minimum_purity", length = 20)
    private String minimumPurity;

    @Column(name = "minimum_germination", length = 20)
    private String minimumGermination;

    @Column(name = "active")
    private boolean active = true;
}
