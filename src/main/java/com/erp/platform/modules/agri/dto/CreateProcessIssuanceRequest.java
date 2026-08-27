package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProcessIssuanceRequest(
        String processNumber,
        LocalDate processDate,
        String lotNumber,
        String processType,
        String byProductName,
        BigDecimal forwardWeightKgs,
        BigDecimal expiredKgs,
        BigDecimal lostKgs,
        BigDecimal rejectedKgs,
        BigDecimal retainedKgs,
        String location,
        String status,
        String remarks
) {}
