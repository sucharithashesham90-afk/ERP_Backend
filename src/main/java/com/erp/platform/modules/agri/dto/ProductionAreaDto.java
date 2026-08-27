package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductionAreaDto(
        UUID id,
        String code,
        String name,
        String description,
        String village,
        String district,
        String state,
        BigDecimal totalAreaAcres,
        boolean active
) {}
