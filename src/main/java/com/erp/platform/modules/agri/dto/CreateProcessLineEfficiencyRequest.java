package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProcessLineEfficiencyRequest(
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
