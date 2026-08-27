package com.erp.platform.modules.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceItemDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String description;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String packType;
    private BigDecimal packSize;
    private BigDecimal forwardingCharges;
    private BigDecimal surchargePercent;
    private BigDecimal surchargeAmount;
}
