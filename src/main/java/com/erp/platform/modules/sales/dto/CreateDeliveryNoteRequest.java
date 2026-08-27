package com.erp.platform.modules.sales.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateDeliveryNoteRequest {
    private UUID salesOrderId;
    private LocalDate deliveryDate;
    private String deliveryAddress;
    private String carrierName;
    private String trackingNumber;
    private String notes;
}
