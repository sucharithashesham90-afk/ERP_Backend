package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateProductionAreaRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        String village,
        String district,
        String state,
        BigDecimal totalAreaAcres,
        boolean active
) {}
