package com.erp.platform.modules.hr.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TadaClaimDto {

    private UUID id;
    private String employeeId;
    private String employeeName;
    private LocalDate claimDate;
    private String travelType;
    private String fromPlace;
    private String toPlace;
    private Integer travelDays;
    private BigDecimal dailyAllowanceRate;
    private BigDecimal travelAllowance;
    private BigDecimal totalAmount;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
