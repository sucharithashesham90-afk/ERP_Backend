package com.erp.platform.modules.sales.dto;

import java.time.LocalDate;

public record CreateCustomerComplaintRequest(
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
