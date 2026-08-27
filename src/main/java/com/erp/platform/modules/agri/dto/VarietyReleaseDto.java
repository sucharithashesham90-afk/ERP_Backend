package com.erp.platform.modules.agri.dto;

import java.time.LocalDate;
import java.util.UUID;

public record VarietyReleaseDto(
        UUID id,
        String varietyCode,
        String varietyName,
        String cropName,
        Integer releaseYear,
        String notificationNumber,
        LocalDate notificationDate,
        String certifyingBody,
        String seedClass,
        String description,
        boolean active
) {}
