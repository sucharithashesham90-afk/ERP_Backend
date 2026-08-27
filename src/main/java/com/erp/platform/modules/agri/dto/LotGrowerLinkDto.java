package com.erp.platform.modules.agri.dto;
import java.util.UUID;

import java.math.BigDecimal;

public record LotGrowerLinkDto(
    UUID id,
    String linkNumber,
    String lotNumber,
    String growerCode,
    String growerName,
    String village,
    String organizer,
    BigDecimal contractedQtyKgs,
    BigDecimal suppliedQtyKgs,
    String season,
    String cropName,
    String remarks
) {}
