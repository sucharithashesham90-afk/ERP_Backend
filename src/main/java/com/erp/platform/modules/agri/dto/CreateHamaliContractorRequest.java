package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateHamaliContractorRequest(
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
