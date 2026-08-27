package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AgriProductionArea")
@Table(name = "agri_production_areas",
        indexes = {@Index(name = "idx_prod_area_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProductionArea extends TenantEntity {

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "village", length = 100)
    private String village;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "total_area_acres", precision = 12, scale = 3)
    private java.math.BigDecimal totalAreaAcres;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
