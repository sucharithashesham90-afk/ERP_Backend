package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateNurseryRequest(
        @NotBlank String code,
        @NotBlank String name,
        String contactPerson,
        String phone,
        String address,
        String village,
        String district,
        String state,
        BigDecimal capacityKgs,
        boolean active
) {}
