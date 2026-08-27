package com.erp.platform.modules.intake.dto;

import com.erp.platform.modules.intake.entity.IntakeSchedule.ScheduleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class IntakeScheduleDto {

    private UUID id;
    private UUID tenantId;
    private String scheduleNumber;
    private UUID vendorId;
    private String vendorName;
    private LocalDate scheduledDate;
    private ScheduleStatus status;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private BigDecimal expectedQuantity;
    private BigDecimal actualQuantity;
    private UUID warehouseId;
    private String warehouseName;
    private String notes;
    private List<ItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal expectedQuantity;
        private BigDecimal actualQuantity;
        private String unit;
        private UUID purchaseOrderId;
        private String notes;
    }
}
