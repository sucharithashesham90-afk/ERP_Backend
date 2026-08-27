package com.erp.platform.modules.hr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePayGradeRequest {

    @NotBlank
    private String name;

    private String description;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal providentFundPercent;
    private BigDecimal otPremiumFactor;
    @JsonProperty("isLossOfPayApplicable")
    private boolean isLossOfPayApplicable = false;
    @JsonProperty("isOverTimeApplicable")
    private boolean isOverTimeApplicable = false;
    private boolean active = true;
}
