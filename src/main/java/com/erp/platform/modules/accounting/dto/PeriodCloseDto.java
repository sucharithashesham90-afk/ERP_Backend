package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.PeriodClose.CloseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PeriodCloseDto {
    private UUID id;
    private int periodYear;
    private int periodMonth;
    private String periodName;
    private CloseStatus status;
    private LocalDateTime closedAt;
    private String closedBy;
    private LocalDateTime openedAt;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
