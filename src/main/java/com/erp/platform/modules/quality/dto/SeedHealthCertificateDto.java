package com.erp.platform.modules.quality.dto;
import java.util.UUID;

import java.time.LocalDate;

public record SeedHealthCertificateDto(
    UUID id,
    String certificateNumber,
    LocalDate issueDate,
    LocalDate expiryDate,
    String lotNumber,
    String cropName,
    String issuingAuthority,
    String testingLaboratory,
    String healthStatus,
    Boolean treatmentRequired,
    String remarks,
    String status
) {}
