package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TreatmentDto(
        UUID id,
        String code,
        String name,
        String chemicalName,
        String treatmentType,
        BigDecimal dosagePerKg,
        String unit,
        String description,
        boolean active
) {}
