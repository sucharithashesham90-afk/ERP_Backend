package com.erp.platform.modules.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StockItemDto {
    private UUID id;
    private UUID tenantId;
    private UUID warehouseId;
    private String warehouseName;
    private UUID productId;
    private String productName;
    private BigDecimal quantityOnHand;
    private BigDecimal quantityReserved;
    private BigDecimal quantityAvailable;
    private BigDecimal averageCost;
    private String location;
    private String stockType;
    private String materialState;
    private String materialType;
    private LocalDateTime updatedAt;
}
