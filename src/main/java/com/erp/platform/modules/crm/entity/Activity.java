package com.erp.platform.modules.crm.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities",
       indexes = {@Index(name = "idx_activity_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Activity extends TenantEntity {

    @Column(length = 15)
    private String type; // CALL, EMAIL, MEETING, TASK, NOTE

    @Column(length = 200)
    private String subject;

    @Column(length = 2000)
    private String description;

    @Column(length = 15)
    private String status; // PLANNED, COMPLETED, CANCELLED

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 30)
    private String referenceType; // LEAD, CUSTOMER

    @Column(name = "assigned_to")
    private UUID assignedTo;
}
