package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePhysicalCountRequest {
    private UUID warehouseId;
    private LocalDate countDate;
    private String notes;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private UUID productId;
        private String productName;
        private BigDecimal countedQuantity;
        private String unit;
        private String remarks;
    }
}
