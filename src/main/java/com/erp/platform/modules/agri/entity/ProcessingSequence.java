package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "processing_sequences", indexes = {@Index(name = "idx_processing_sequence_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessingSequence extends TenantEntity {

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "processing_steps", length = 500)
    private String processingSteps;

    // Full step rows (seq + processStepId + processStepName) as JSON so edit can reload them.
    @Column(name = "steps_json", columnDefinition = "text")
    private String stepsJson;

    @Column(name = "active")
    private boolean active = true;
}
