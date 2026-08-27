package com.erp.platform.modules.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LotTraceDto {
    private String lotNumber;
    private List<LotStockDto> stocks;
    private List<LotMovementDto> movements;
    private List<LotHistoryEventDto> events;
    private BigDecimal totalReceived;
    private BigDecimal totalIssued;
    private BigDecimal currentBalance;
}
