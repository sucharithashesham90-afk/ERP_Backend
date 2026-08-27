package com.erp.platform.modules.agri.dto;
import java.util.UUID;

import java.math.BigDecimal;

public record HamaliContractorDto(
    UUID id,
    String contractorCode,
    String contractorName,
    String contractorType,
    String phoneNumber,
    String address,
    UUID stateId,
    String stateName,
    UUID districtId,
    String districtName,
    UUID mandalId,
    String mandalName,
    String zipCode,
    BigDecimal ratePerBag,
    BigDecimal ratePerKg,
    String season,
    String location,
    Boolean active
) {}
