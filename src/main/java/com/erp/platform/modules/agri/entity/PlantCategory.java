package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plant_categories",
       indexes = {@Index(name = "idx_plant_cat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PlantCategory extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_family_id")
    private PlantFamily plantFamily;

    @Column(length = 100)
    private String scientificName;

    @Column(length = 50)
    private String defaultSeason;

    @Column(nullable = false)
    private boolean active = true;
}
