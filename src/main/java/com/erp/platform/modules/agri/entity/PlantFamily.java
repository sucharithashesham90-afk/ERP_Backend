package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plant_families",
       indexes = {@Index(name = "idx_plant_family_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PlantFamily extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30, unique = false)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String scientificName;

    @Column(nullable = false)
    private boolean active = true;
}
