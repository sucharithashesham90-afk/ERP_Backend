package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.SalesPlan.PlanStatus;
import com.erp.platform.modules.sales.entity.SalesPlan.PlanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SalesPlanDto {
    private UUID id;
    private UUID tenantId;
    private String planNumber;
    private String name;
    private PlanType planType;
    private int planYear;
    private Integer planQuarter;
    private Integer planMonth;
    private PlanStatus status;
    private BigDecimal totalTargetRevenue;
    private BigDecimal totalActualRevenue;
    private String territory;
    private UUID salesRepId;
    private String salesRepName;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String notes;
    private List<SalesPlanTargetDto> targets;
    private LocalDateTime createdAt;
}
