package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.LocationTransfer.LTStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LocationTransferDto {

    private UUID id;
    private UUID tenantId;
    private String transferNumber;
    private UUID fromLocationId;
    private String fromLocationCode;
    private UUID toLocationId;
    private String toLocationCode;
    private UUID warehouseId;
    private UUID productId;
    private String productName;
    private String lotNumber;
    private BigDecimal quantity;
    private LocalDate transferDate;
    private String reason;
    private LTStatus status;
    private String performedBy;
    private LocalDateTime createdAt;
}
