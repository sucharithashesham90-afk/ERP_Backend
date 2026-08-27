package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "designations",
       indexes = {@Index(name = "idx_desig_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Designation extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "department_id")
    private java.util.UUID departmentId;

    @Column(nullable = false)
    private boolean active = true;
}
