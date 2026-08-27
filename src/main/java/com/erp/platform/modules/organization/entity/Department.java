package com.erp.platform.modules.organization.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "departments",
       indexes = {@Index(name = "idx_dept_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Department extends TenantEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private boolean active = true;
}
