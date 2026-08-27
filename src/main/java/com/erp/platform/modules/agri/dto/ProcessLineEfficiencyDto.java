package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProcessLineEfficiencyDto(
        UUID id,
        LocalDate recordDate,
        String location,
        String godown,
        String processingLineName,
        String processType,
        BigDecimal plannedOutputKgs,
        BigDecimal actualOutputKgs,
        BigDecimal efficiencyPercent,
        String remarks
) {}
