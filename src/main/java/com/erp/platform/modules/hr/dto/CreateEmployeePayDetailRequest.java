package com.erp.platform.modules.hr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateEmployeePayDetailRequest {

    private String employeeId;
    private String employeeName;
    private String modeOfSalaryPayment;
    private Integer salaryDay;
    private LocalDate employeeStartingDate;
    private BigDecimal grossSalary;
    private BigDecimal providentFundPercent;
    private BigDecimal otPremiumFactor;
    @JsonProperty("isLossOfPayApplicable")
    private boolean isLossOfPayApplicable = false;
    @JsonProperty("isOverTimeApplicable")
    private boolean isOverTimeApplicable = false;
    private String payGradeId;
}
