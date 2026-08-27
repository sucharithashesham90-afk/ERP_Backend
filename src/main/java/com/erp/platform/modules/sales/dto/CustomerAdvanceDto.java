package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.CustomerAdvance.AdvanceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerAdvanceDto {
    private UUID id;
    private UUID tenantId;
    private String advanceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate paymentDate;
    private String paymentMode;
    private String referenceNumber;
    private BigDecimal amount;
    private BigDecimal amountApplied;
    private BigDecimal amountAvailable;
    private AdvanceStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
