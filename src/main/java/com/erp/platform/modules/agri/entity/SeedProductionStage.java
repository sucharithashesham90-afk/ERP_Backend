package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seed_production_stages", indexes = {@Index(name = "idx_seed_production_stage_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SeedProductionStage extends TenantEntity {

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "stage_name", length = 100, nullable = false)
    private String name;

    // Boolean (not primitive) so pre-existing rows with a NULL column read without error.
    @Column(name = "requires_approval", columnDefinition = "boolean default false")
    private Boolean requiresApproval = false;

    @Column(name = "from_stage", length = 50)
    private String fromStage;

    @Column(name = "to_stage", length = 50)
    private String toStage;

    @Column(name = "stage_order")
    private int stageOrder;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active")
    private boolean active = true;
}
