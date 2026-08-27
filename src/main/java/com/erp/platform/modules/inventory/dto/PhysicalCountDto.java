package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PhysicalCountDto {
    private UUID id;
    private UUID tenantId;
    private String countNumber;
    private UUID warehouseId;
    private String warehouseName;
    private LocalDate countDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal systemQuantity;
        private BigDecimal countedQuantity;
        private BigDecimal differenceQuantity;
        private String unit;
        private String remarks;
    }
}
