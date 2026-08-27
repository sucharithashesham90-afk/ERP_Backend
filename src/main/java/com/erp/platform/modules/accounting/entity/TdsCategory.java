package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tds_categories",
       indexes = {@Index(name = "idx_tds_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class TdsCategory extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 8, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "surcharge_rate", precision = 8, scale = 4)
    private BigDecimal surchargeRate = BigDecimal.ZERO;

    @Column(name = "education_cess_rate", precision = 8, scale = 4)
    private BigDecimal educationCessRate = BigDecimal.ZERO;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    /** TDS applies only once payments to a party pass this figure in the year. */
    @Column(name = "threshold_amount", precision = 18, scale = 2)
    private BigDecimal thresholdAmount = BigDecimal.ZERO;

    @Column(name = "section_code", length = 20)
    private String sectionCode;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
