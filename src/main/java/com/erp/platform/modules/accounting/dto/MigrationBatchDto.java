package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.MigrationBatch.BatchStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MigrationBatchDto {
    private UUID id;
    private UUID tenantId;
    private String batchNumber;
    private String description;
    private LocalDate asOfDate;
    private BatchStatus status;
    private int totalRecords;
    private int processedRecords;
    private int errorRecords;
    private LocalDateTime completedAt;
    private String notes;
    private LocalDateTime createdAt;
}
