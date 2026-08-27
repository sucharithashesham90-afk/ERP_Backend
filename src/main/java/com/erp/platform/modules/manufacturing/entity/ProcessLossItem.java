package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "process_loss_items",
       indexes = {
           @Index(name = "idx_pli_loss", columnList = "process_loss_id")
       })
@Getter
@Setter
public class ProcessLossItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_loss_id", nullable = false)
    @JsonIgnore
    private ProcessLoss processLoss;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType; // BYPRODUCT or WASTE

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_name", length = 200)
    private String referenceName;

    @Column(name = "selected", nullable = false)
    private boolean selected = true;
}
