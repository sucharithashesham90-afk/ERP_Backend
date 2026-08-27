package com.erp.platform.modules.sales.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DeliveryNoteDto {
    private UUID id;
    private UUID tenantId;
    private String deliveryNumber;
    private UUID salesOrderId;
    private String customerName;
    private LocalDate deliveryDate;
    private String deliveryAddress;
    private String carrierName;
    private String trackingNumber;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
