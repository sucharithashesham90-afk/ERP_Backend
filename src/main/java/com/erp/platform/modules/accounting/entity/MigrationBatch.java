package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "migration_batches",
       indexes = {
           @Index(name = "idx_mb_tenant", columnList = "tenant_id"),
           @Index(name = "idx_mb_batch_number", columnList = "tenant_id, batch_number")
       })
@Getter
@Setter
public class MigrationBatch extends TenantEntity {

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(length = 500)
    private String description;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.DRAFT;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "processed_records")
    private int processedRecords;

    @Column(name = "error_records")
    private int errorRecords;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String notes;

    public enum BatchStatus {
        DRAFT, IN_PROGRESS, COMPLETED, FAILED
    }
}
