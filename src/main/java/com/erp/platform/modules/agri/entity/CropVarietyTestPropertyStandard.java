package com.erp.platform.modules.agri.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "crop_variety_test_property_standards",
        indexes = {@Index(name = "idx_cvt_prop_std_tenant", columnList = "tenant_id"),
                   @Index(name = "idx_cvt_prop_std_test", columnList = "crop_variety_test_id")})
@Getter
@Setter
public class CropVarietyTestPropertyStandard extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_variety_test_id", nullable = false)
    @JsonIgnore
    private CropVarietyTest cropVarietyTest;

    @Column(name = "property_name", length = 200, nullable = false)
    private String propertyName;

    @Column(name = "test_location_id", length = 100)
    private String testLocationId;

    @Column(name = "test_location_name", length = 200)
    private String testLocationName;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;
}
