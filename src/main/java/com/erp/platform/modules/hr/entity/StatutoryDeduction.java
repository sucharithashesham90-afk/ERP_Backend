package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "statutory_deductions", indexes = {@Index(name = "idx_sd_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class StatutoryDeduction extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(name = "deduction_type", length = 30)
    private String deductionType;

    @Column(name = "employee_rate", precision = 8, scale = 4)
    private BigDecimal employeeRate = BigDecimal.ZERO;

    @Column(name = "employer_rate", precision = 8, scale = 4)
    private BigDecimal employerRate = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal ceiling = BigDecimal.ZERO;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(length = 500)
    private String description;

    private boolean active = true;
}
