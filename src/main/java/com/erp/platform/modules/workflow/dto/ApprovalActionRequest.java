package com.erp.platform.modules.workflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalActionRequest {

    private String action; // APPROVE or REJECT
    private String assignedTo;
    private String comments;
    private LocalDateTime dueDate;
}
