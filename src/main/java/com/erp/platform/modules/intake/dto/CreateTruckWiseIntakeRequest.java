package com.erp.platform.modules.intake.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTruckWiseIntakeRequest(
        String intakeSlipNumber,
        LocalDate intakeDate,
        String location,
        String deliveryType,
        String vehicleNumber,
        String inwardGatePassNumber,
        String lrNumber,
        String transport,
        BigDecimal totalFreight,
        BigDecimal weighBridgeQty,
        String stnNumber,
        LocalDate stnDate,
        String status
) {}
