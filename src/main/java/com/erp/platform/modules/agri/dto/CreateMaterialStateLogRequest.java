package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMaterialStateLogRequest(
    String logNumber,
    LocalDate logDate,
    String lotNumber,
    String fromState,
    String toState,
    String processType,
    BigDecimal quantityKgs,
    String location,
    String operatorName,
    String remarks
) {}
