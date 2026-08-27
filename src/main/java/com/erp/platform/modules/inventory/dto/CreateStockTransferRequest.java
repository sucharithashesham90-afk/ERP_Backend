package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateStockTransferRequest {
    private UUID fromWarehouseId;
    private UUID toWarehouseId;
    private LocalDate transferDate;
    private String notes;
    private List<TransferItem> items;

    @Data
    public static class TransferItem {
        private UUID productId;
        private Double quantity;
    }
}
