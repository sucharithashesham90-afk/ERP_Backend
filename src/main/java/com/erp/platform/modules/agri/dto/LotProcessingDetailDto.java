package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LotProcessingDetailDto(
        UUID id,
        String jobNumber,
        LocalDate processingDate,
        String lotNumber,
        String location,
        String inputLotNumber,
        String outputLotNumber,
        BigDecimal inputQuantityKgs,
        BigDecimal outputQuantityKgs,
        String processType,
        String machineName,
        String operatorName,
        String status
) {}
