package com.erp.platform.modules.sales.dto;
import java.util.UUID;

import java.time.LocalDate;

public record CustomerComplaintDto(
    UUID id,
    String complaintNumber,
    LocalDate complaintDate,
    String customerName,
    String phoneNumber,
    String productName,
    String lotNumber,
    String complaintType,
    String description,
    String assignedTo,
    LocalDate resolutionDate,
    String resolution,
    String status
) {}
