package com.erp.platform.modules.workflow.dto;

import com.erp.platform.modules.workflow.entity.ApprovalInstance.ApprovalStatus;
import com.erp.platform.modules.workflow.entity.ApprovalStep.StepStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ApprovalInstanceDto {

    private UUID id;
    private String documentType;
    private UUID documentId;
    private String documentNumber;
    private int currentLevel;
    private int maxLevel;
    private ApprovalStatus status;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private String notes;
    private List<StepDto> steps;
    private LocalDateTime createdAt;

    @Data
    public static class StepDto {
        private UUID id;
        private int level;
        private String approverType;
        private String approverValue;
        private String assignedTo;
        private StepStatus status;
        private LocalDateTime actionDate;
        private LocalDateTime dueDate;
        private String comments;
        private LocalDateTime escalatedAt;
    }
}
