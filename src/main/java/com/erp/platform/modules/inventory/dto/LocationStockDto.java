package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LocationStockDto {

    private UUID id;
    private UUID tenantId;
    private UUID locationId;
    private String locationCode;
    private UUID warehouseId;
    private UUID productId;
    private String productName;
    private String lotNumber;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal unitCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
