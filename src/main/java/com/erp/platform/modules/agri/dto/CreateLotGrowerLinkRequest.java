package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;

public record CreateLotGrowerLinkRequest(
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
