package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SalesSchemeDto {
    private UUID id;
    private String schemeName;
    private String schemeCode;
    private String season;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String schemeType;
    private BigDecimal discountPercent;
    private BigDecimal flatDiscountAmount;
    private String remarks;
    private boolean active;
    private LocalDateTime createdAt;
}
