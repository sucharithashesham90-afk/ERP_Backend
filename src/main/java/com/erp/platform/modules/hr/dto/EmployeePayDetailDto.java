package com.erp.platform.modules.hr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EmployeePayDetailDto {

    private UUID id;
    private String employeeId;
    private String employeeName;
    private String modeOfSalaryPayment;
    private Integer salaryDay;
    private LocalDate employeeStartingDate;
    private BigDecimal grossSalary;
    private BigDecimal providentFundPercent;
    private BigDecimal otPremiumFactor;
    // Lombok's getter for isXxx makes Jackson drop the "is", sending lossOfPayApplicable while the
    // screen reads isLossOfPayApplicable. The value saved fine; only the name coming back was wrong.
    @JsonProperty("isLossOfPayApplicable")
    private boolean isLossOfPayApplicable;
    @JsonProperty("isOverTimeApplicable")
    private boolean isOverTimeApplicable;
    private String payGradeId;
    private LocalDateTime createdAt;
}
