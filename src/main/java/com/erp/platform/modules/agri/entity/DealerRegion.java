package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dealer_regions",
       indexes = {@Index(name = "idx_dealer_region_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DealerRegion extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30, unique = false)
    private String code;

    @Column(length = 300)
    private String description;

    @Column(length = 500)
    private String statesCovered;

    @Column(length = 50)
    private String activeSeason;

    @Column(nullable = false)
    private boolean active = true;
}
