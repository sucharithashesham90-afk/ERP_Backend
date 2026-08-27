package com.erp.platform.modules.hr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PayGradeDto {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal providentFundPercent;
    private BigDecimal otPremiumFactor;
    // Lombok's getter for isXxx makes Jackson drop the "is", sending lossOfPayApplicable while the
    // screen reads isLossOfPayApplicable. The value saved fine; only the name coming back was wrong.
    @JsonProperty("isLossOfPayApplicable")
    private boolean isLossOfPayApplicable;
    @JsonProperty("isOverTimeApplicable")
    private boolean isOverTimeApplicable;
    private boolean active;
    private LocalDateTime createdAt;
}
