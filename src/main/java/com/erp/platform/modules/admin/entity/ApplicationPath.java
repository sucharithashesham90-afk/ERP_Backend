package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "application_paths",
       indexes = { @Index(name = "idx_apppath_tenant", columnList = "tenant_id") })
@Getter
@Setter
public class ApplicationPath extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(length = 100)
    private String pathType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
