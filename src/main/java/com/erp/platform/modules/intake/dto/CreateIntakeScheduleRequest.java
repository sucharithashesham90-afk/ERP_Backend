package com.erp.platform.modules.intake.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateIntakeScheduleRequest {

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    private String vendorName;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private String vehicleNumber;

    private String driverName;

    private String driverPhone;

    private BigDecimal expectedQuantity;

    private UUID warehouseId;

    private String warehouseName;

    private String notes;

    @Valid
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        private String productName;

        @NotNull(message = "Expected quantity is required")
        private BigDecimal expectedQuantity;

        private String unit;

        private UUID purchaseOrderId;

        private String notes;
    }
}
