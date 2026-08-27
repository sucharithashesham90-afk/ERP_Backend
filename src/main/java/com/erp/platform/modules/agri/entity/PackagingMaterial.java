package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AgriPackagingMaterial")
@Table(name = "agri_packaging_materials", indexes = {@Index(name = "idx_pm_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackagingMaterial extends TenantEntity {

    @Column(name = "material_code", length = 50, nullable = false)
    private String materialCode;

    @Column(name = "material_name", length = 200)
    private String materialName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "class_of_seed", length = 50)
    private String classOfSeed;

    @Column(name = "hybrid")
    private boolean hybrid = false;

    @Column(name = "active")
    private boolean active = true;
}
