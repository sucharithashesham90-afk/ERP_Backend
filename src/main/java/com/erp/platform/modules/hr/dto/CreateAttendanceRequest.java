package com.erp.platform.modules.hr.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateAttendanceRequest {

    private EmployeeRef employee;
    private UUID employeeId;
    private LocalDate date;
    private String status;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String notes;

    public UUID resolveEmployeeId() {
        if (employeeId != null) return employeeId;
        return employee != null ? employee.getId() : null;
    }

    @Data
    public static class EmployeeRef {
        private UUID id;
    }
}
