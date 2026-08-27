package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateLocationTransferRequest {

    private UUID fromLocationId;
    private UUID toLocationId;
    private UUID warehouseId;
    private UUID productId;
    private String productName;
    private String lotNumber;
    private BigDecimal quantity;
    private LocalDate transferDate;
    private String reason;
    private String performedBy;
}
