package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.ScrapEntry.ScrapStatus;
import com.erp.platform.modules.inventory.entity.ScrapEntry.ScrapType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScrapEntryDto {

    private UUID id;
    private String scrapNumber;
    private LocalDate scrapDate;
    private ScrapType scrapType;
    private String sourceType;
    private UUID sourceId;
    private UUID warehouseId;
    private String warehouseName;
    private UUID productId;
    private String productName;
    private String lotNumber;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal scrapValue;
    private BigDecimal recoveryValue;
    private String reason;
    private ScrapStatus status;
    private String approvedBy;
    private LocalDateTime disposedAt;
    private String notes;
    private ScrapDisposalDto disposal;
    private LocalDateTime createdAt;
}
