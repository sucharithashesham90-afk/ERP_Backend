package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AgriProcessingScreen")
@Table(name = "agri_processing_screens",
        indexes = {@Index(name = "idx_agri_proc_screen_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessingScreen extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String meshSize;

    @Column(length = 100)
    private String screenType;

    @Column(length = 100)
    private String material;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
