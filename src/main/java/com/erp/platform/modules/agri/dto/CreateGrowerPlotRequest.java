package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;

public record CreateGrowerPlotRequest(
    String plotCode,
    String growerCode,
    String growerName,
    String village,
    String surveyNumber,
    BigDecimal plotAreaAcres,
    String soilType,
    String irrigationType,
    String cropName,
    String season,
    Boolean active
) {}
