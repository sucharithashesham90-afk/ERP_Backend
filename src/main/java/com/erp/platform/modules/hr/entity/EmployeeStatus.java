package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employee_statuses",
       indexes = {@Index(name = "idx_empst_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class EmployeeStatus extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(length = 30)
    private String category; // ACTIVE, INACTIVE, TERMINATED, ON_LEAVE

    @Column(nullable = false)
    private boolean active = true;
}
