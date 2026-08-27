package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StockTransferDto {
    private UUID id;
    private UUID tenantId;
    private String transferNumber;
    private UUID fromWarehouseId;
    private String fromWarehouseName;
    private UUID toWarehouseId;
    private String toWarehouseName;
    private LocalDate transferDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
