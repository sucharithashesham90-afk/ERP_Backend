package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "geo_districts",
       indexes = {
           @Index(name = "idx_geodist_tenant", columnList = "tenant_id"),
           @Index(name = "idx_geodist_state",  columnList = "tenant_id, state_id")
       })
@Getter
@Setter
public class GeoDistrict extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "state_name", length = 200)
    private String stateName;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "country_name", length = 200)
    private String countryName;

    @Column(nullable = false)
    private boolean active = true;
}
