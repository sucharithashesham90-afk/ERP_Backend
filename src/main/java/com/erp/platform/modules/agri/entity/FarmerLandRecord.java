package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agri_farmer_land_records",
        indexes = {
            @Index(name = "idx_farmer_land_tenant", columnList = "tenant_id"),
            @Index(name = "idx_farmer_land_farmer",  columnList = "farmer_id")
        })
@Getter
@Setter
public class FarmerLandRecord extends TenantEntity {

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(name = "village_id")
    private UUID villageId;

    @Column(name = "village_name", length = 200)
    private String villageName;

    @Column(name = "plot_survey_no", length = 100)
    private String plotSurveyNo;

    @Column(name = "acreage", precision = 10, scale = 3)
    private BigDecimal acreage;

    @Column(name = "land_type", length = 100)
    private String landType;
}
