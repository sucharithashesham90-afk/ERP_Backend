package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "AgriQualityStandard")
@Table(name = "agri_quality_standards", indexes = {
    @Index(name = "idx_quality_standard_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class QualityStandard extends TenantEntity {

    @Column(name = "standard_code", length = 50, nullable = false)
    private String standardCode;

    @Column(name = "standard_name", length = 200)
    private String standardName;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "seed_class", length = 100)
    private String seedClass;

    @Column(name = "min_germination_percent", precision = 5, scale = 2)
    private BigDecimal minGerminationPercent;

    @Column(name = "min_purity_percent", precision = 5, scale = 2)
    private BigDecimal minPurityPercent;

    @Column(name = "max_moisture_percent", precision = 5, scale = 2)
    private BigDecimal maxMoisturePercent;

    @Column(name = "max_inert_matter_percent", precision = 5, scale = 2)
    private BigDecimal maxInertMatterPercent;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active")
    private Boolean active = true;
}
