package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FiscalYearDto {
    private UUID id;
    private UUID tenantId;
    private String periodCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
