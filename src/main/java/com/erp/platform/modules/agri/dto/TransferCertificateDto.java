package com.erp.platform.modules.agri.dto;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferCertificateDto(
    UUID id,
    String tcNumber,
    LocalDate issueDate,
    String lotNumber,
    String sourcePlant,
    String destinationPlant,
    Integer numberOfBags,
    BigDecimal quantityKgs,
    String inputType,
    BigDecimal bagWeight,
    Boolean intakeDone,
    String remarks,
    String status
) {}
