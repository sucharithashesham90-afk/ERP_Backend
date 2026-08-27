package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pay_grades", indexes = {@Index(name = "idx_pay_grade_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PayGrade extends TenantEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "min_salary", precision = 18, scale = 4)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 18, scale = 4)
    private BigDecimal maxSalary;

    @Column(name = "provident_fund_percent", precision = 10, scale = 4)
    private BigDecimal providentFundPercent;

    @Column(name = "ot_premium_factor", precision = 10, scale = 4)
    private BigDecimal otPremiumFactor;

    @Column(name = "is_loss_of_pay_applicable")
    @JsonProperty("isLossOfPayApplicable")
    private boolean isLossOfPayApplicable = false;

    @Column(name = "is_over_time_applicable")
    @JsonProperty("isOverTimeApplicable")
    private boolean isOverTimeApplicable = false;

    @Column(name = "active")
    private boolean active = true;
}
