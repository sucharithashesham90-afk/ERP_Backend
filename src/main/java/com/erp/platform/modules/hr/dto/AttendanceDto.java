package com.erp.platform.modules.hr.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceDto {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private LocalDate date;
    private String status;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workingHours;
    private String notes;
    private LocalDateTime createdAt;
}
