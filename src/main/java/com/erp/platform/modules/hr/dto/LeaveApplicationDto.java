package com.erp.platform.modules.hr.dto;

import com.erp.platform.modules.hr.entity.LeaveApplication.LeaveStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeaveApplicationDto {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    /** Resolved from the employee record, so a list of leave does not read as a column of UUIDs. */
    private String employeeName;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int days;
    private String reason;
    private LeaveStatus status;
    private UUID approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
