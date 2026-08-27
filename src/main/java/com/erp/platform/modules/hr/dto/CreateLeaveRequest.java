package com.erp.platform.modules.hr.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateLeaveRequest {

    private EmployeeRef employee;
    private UUID employeeId;
    private UUID leaveTypeId;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;

    public UUID resolveEmployeeId() {
        if (employeeId != null) return employeeId;
        return employee != null ? employee.getId() : null;
    }

    @Data
    public static class EmployeeRef {
        private UUID id;
    }
}
