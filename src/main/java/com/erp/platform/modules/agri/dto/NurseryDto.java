package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record NurseryDto(
        UUID id,
        String code,
        String name,
        String contactPerson,
        String phone,
        String address,
        String village,
        String district,
        String state,
        BigDecimal capacityKgs,
        boolean active
) {}
