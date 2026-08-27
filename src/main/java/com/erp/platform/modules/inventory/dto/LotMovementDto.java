package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.LotMovement.LotMovType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LotMovementDto {

    private UUID id;
    private UUID tenantId;
    private String lotNumber;
    private UUID productId;
    private String productName;
    private UUID warehouseId;
    private LotMovType movementType;
    private LocalDate movementDate;
    private BigDecimal quantity;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String referenceType;
    private UUID referenceId;
    private String referenceNumber;
    private String notes;
    private String partyName;   // enriched: customer for DELIVERY_NOTE, customer for SALES_RETURN
    private String extraInfo;   // enriched: additional context (return reason, carrier, etc.)
    private LocalDateTime createdAt;
}
