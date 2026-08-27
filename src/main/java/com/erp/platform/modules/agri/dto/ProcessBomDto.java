package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProcessBomDto {

    private UUID id;
    private String processingType;
    private String itemCode;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private String bomType;
    private boolean active;
    private LocalDateTime createdAt;
}
