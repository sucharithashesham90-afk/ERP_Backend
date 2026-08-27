package com.erp.platform.modules.agri.dto;

import java.util.UUID;

public record ProcessingScreenDto(
        UUID id,
        String code,
        String name,
        String meshSize,
        String screenType,
        String material,
        String description,
        boolean active
) {}
