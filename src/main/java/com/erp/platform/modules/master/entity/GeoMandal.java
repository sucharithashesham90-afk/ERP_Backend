package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "geo_mandals",
       indexes = {
           @Index(name = "idx_geomandal_tenant",   columnList = "tenant_id"),
           @Index(name = "idx_geomandal_district", columnList = "tenant_id, district_id")
       })
@Getter
@Setter
public class GeoMandal extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(name = "district_id")
    private UUID districtId;

    @Column(name = "district_name", length = 200)
    private String districtName;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "state_name", length = 200)
    private String stateName;

    @Column(nullable = false)
    private boolean active = true;
}
