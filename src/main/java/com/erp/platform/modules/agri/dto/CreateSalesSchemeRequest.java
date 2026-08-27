package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSalesSchemeRequest {
    @NotBlank
    private String schemeName;
    private String schemeCode;
    private String season;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String schemeType;
    private BigDecimal discountPercent;
    private BigDecimal flatDiscountAmount;
    private String remarks;
    private boolean active = true;
}
