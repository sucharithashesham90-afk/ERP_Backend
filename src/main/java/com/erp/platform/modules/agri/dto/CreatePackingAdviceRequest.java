package com.erp.platform.modules.agri.dto;

import java.time.LocalDate;

public record CreatePackingAdviceRequest(
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
