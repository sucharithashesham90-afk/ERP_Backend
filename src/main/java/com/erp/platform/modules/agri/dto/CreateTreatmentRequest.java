package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateTreatmentRequest(
        String code,
        @NotBlank String name,
        String chemicalName,
        String treatmentType,
        BigDecimal dosagePerKg,
        String unit,
        String description,
        boolean active
) {}
