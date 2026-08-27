package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "process_template_steps",
       indexes = {
           @Index(name = "idx_pts_template", columnList = "template_id"),
           @Index(name = "idx_pts_seq", columnList = "template_id, sequence_number")
       })
@Getter
@Setter
public class ProcessTemplateStep extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProcessTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProcessStep step;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "is_optional", nullable = false)
    private boolean isOptional = false;

    @Column(length = 1000)
    private String notes;
}
