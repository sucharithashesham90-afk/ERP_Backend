package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateWeighbridgeRecordRequest(
    String slipNumber,
    LocalDate weighDate,
    String vehicleNumber,
    BigDecimal grossWeight,
    BigDecimal tareWeight,
    BigDecimal netWeight,
    String location,
    String linkedIntakeSlip,
    String driverName,
    String remarks
) {}
