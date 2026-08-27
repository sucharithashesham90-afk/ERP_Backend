package com.erp.platform.modules.agri.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PackingAdviceDto(
        UUID id,
        String adviceNumber,
        LocalDate adviceDate,
        String productName,
        String packSize,
        Integer totalBags,
        String location,
        String jobCardNumber,
        String remarks,
        String status
) {}
