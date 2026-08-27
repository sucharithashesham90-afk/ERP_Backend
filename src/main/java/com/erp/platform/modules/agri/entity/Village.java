package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "villages", indexes = {@Index(name = "idx_village_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Village extends TenantEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "village_code", length = 30)
    private String villageCode;

    // Geo hierarchy
    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "state_name", length = 200)
    private String stateName;

    @Column(name = "district_id")
    private UUID districtId;

    @Column(name = "district_name", length = 200)
    private String districtName;

    @Column(name = "mandal_id")
    private UUID mandalId;

    @Column(name = "mandal_name", length = 200)
    private String mandalName;

    @Column(name = "zip", length = 10)
    private String zip;

    // Production area
    @Column(name = "production_area_id")
    private UUID productionAreaId;

    @Column(name = "production_area_name", length = 200)
    private String productionAreaName;

    // Incharge employees (comma-separated UUIDs / names)
    @Column(name = "incharge_ids", columnDefinition = "TEXT")
    private String inchargeIds;

    @Column(name = "incharge_names", columnDefinition = "TEXT")
    private String inchargeNames;

    // Landmarks
    @Column(name = "telegraph_office", length = 200)
    private String telegraphOffice;

    @Column(name = "nearest_railway_stn", length = 200)
    private String nearestRailwayStn;

    @Column(name = "nearest_post_office", length = 200)
    private String nearestPostOffice;

    @Column(name = "nearest_town", length = 200)
    private String nearestTown;

    @Column(name = "active")
    private boolean active = true;

    // Legacy columns retained for backward compatibility
    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "taluka", length = 100)
    private String taluka;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;
}
