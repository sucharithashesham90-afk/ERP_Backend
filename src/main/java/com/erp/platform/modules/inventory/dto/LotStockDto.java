package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.LotStock.LotSource;
import com.erp.platform.modules.inventory.entity.LotStock.LotStockStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LotStockDto {

    private UUID id;
    private UUID tenantId;
    private String lotNumber;
    private UUID productId;
    private String productName;
    private UUID warehouseId;
    private String warehouseName;
    private UUID storageLocationId;
    private String storageLocationName;
    private LotSource sourceType;
    private UUID sourceId;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal quantityOnHand;
    private BigDecimal quantityReserved;
    private BigDecimal unitCost;
    private LotStockStatus status;
    private LocalDateTime createdAt;
}
