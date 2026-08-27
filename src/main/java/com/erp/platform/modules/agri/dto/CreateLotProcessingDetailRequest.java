package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLotProcessingDetailRequest(
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
