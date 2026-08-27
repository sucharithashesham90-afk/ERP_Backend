package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProductAllotmentRequest {
    @NotBlank
    private String allotmentNumber;
    private UUID dealerId;
    private UUID plantVariantId;
    private UUID salesSchemeId;
    private LocalDate allotmentDate;
    private String season;
    private BigDecimal allottedQuantity;
    private String allottedUom;
    private BigDecimal rate;
    private BigDecimal specialDiscountPercent;
    private String status;
    private String remarks;
}
