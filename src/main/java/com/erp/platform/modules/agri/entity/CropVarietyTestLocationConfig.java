package com.erp.platform.modules.agri.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "crop_variety_test_location_configs",
        indexes = {@Index(name = "idx_cvt_loc_config_tenant", columnList = "tenant_id"),
                   @Index(name = "idx_cvt_loc_config_test", columnList = "crop_variety_test_id")})
@Getter
@Setter
public class CropVarietyTestLocationConfig extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_variety_test_id", nullable = false)
    @JsonIgnore
    private CropVarietyTest cropVarietyTest;

    @Column(name = "test_location_id", length = 100)
    private String testLocationId;

    @Column(name = "test_location_name", length = 200)
    private String testLocationName;

    @Column(name = "test_cost", precision = 18, scale = 2)
    private BigDecimal testCost;

    @Column(name = "test_duration", length = 100)
    private String testDuration;
}
