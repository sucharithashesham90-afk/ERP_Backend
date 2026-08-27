package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DeductionItemRequest {
    private UUID possibleDeductionId;
    private String deductionName;
    /** PERCENTAGE | PER_UNIT | WEIGHT | FIXED */
    private String deductionType;
    /** Rate per unit / percentage value */
    private BigDecimal rate;
    /** Quantity of units / weight kg (used for PER_UNIT and WEIGHT types) */
    private BigDecimal quantity;
    /** Amount override (used for FIXED type; ignored for computed types) */
    private BigDecimal amount;
    private String remarks;
}
