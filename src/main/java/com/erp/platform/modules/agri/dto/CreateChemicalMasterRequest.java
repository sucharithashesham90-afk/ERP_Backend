package com.erp.platform.modules.agri.dto;

public record CreateChemicalMasterRequest(
        String chemicalCode,
        String chemicalName,
        String chemicalType,
        String manufacturer,
        String registrationNumber,
        Integer withdrawalPeriodDays,
        String description,
        boolean active
) {}
