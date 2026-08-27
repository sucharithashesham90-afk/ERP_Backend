package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "process_losses",
       indexes = {
           @Index(name = "idx_ploss_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ploss_step", columnList = "tenant_id, process_step_id", unique = true)
       })
@Getter
@Setter
public class ProcessLoss extends TenantEntity {

    @Column(name = "process_step_id", nullable = false)
    private UUID processStepId;

    @Column(name = "process_step_name", length = 200)
    private String processStepName;

    @OneToMany(mappedBy = "processLoss", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessLossItem> items = new ArrayList<>();
}
