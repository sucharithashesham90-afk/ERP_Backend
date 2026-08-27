package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "processing_screens",
       indexes = {
           @Index(name = "idx_pscreen_tenant", columnList = "tenant_id")
       })
@Getter
@Setter
public class MfgProcessingScreen extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
