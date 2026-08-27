package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_pay_details", indexes = {@Index(name = "idx_employee_pay_detail_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class EmployeePayDetail extends TenantEntity {

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "mode_of_salary_payment", length = 100)
    private String modeOfSalaryPayment;

    @Column(name = "salary_day")
    private Integer salaryDay;

    @Column(name = "employee_starting_date")
    private LocalDate employeeStartingDate;

    @Column(name = "gross_salary", precision = 18, scale = 4)
    private BigDecimal grossSalary;

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

    @Column(name = "pay_grade_id", length = 100)
    private String payGradeId;
}
