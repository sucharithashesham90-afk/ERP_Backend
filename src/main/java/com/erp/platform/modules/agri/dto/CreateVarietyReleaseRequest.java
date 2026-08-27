package com.erp.platform.modules.agri.dto;

import java.time.LocalDate;

public record CreateVarietyReleaseRequest(
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
