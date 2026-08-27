package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "AgriGrowerPlot")
@Table(name = "agri_grower_plots", indexes = {
    @Index(name = "idx_grower_plot_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class GrowerPlot extends TenantEntity {

    @Column(name = "plot_code", length = 50, nullable = false)
    private String plotCode;

    @Column(name = "grower_code", length = 50)
    private String growerCode;

    @Column(name = "grower_name", length = 200)
    private String growerName;

    @Column(name = "village", length = 200)
    private String village;

    @Column(name = "survey_number", length = 100)
    private String surveyNumber;

    @Column(name = "plot_area_acres", precision = 10, scale = 3)
    private BigDecimal plotAreaAcres;

    @Column(name = "soil_type", length = 100)
    private String soilType;

    @Column(name = "irrigation_type", length = 100)
    private String irrigationType;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "active")
    private Boolean active = true;
}
