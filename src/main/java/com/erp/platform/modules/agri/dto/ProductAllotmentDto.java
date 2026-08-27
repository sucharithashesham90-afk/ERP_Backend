package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductAllotmentDto {
    private UUID id;
    private String allotmentNumber;
    private UUID dealerId;
    private String dealerName;
    private UUID plantVariantId;
    private String plantVariantName;
    private UUID salesSchemeId;
    private String schemeName;
    private LocalDate allotmentDate;
    private String season;
    private BigDecimal allottedQuantity;
    private String allottedUom;
    private BigDecimal rate;
    private BigDecimal specialDiscountPercent;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
