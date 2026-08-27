package com.erp.platform.modules.agri.dto;
import java.util.UUID;

import java.math.BigDecimal;

public record GrowerPlotDto(
    UUID id,
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
