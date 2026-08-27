package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "employee_roles", indexes = {@Index(name = "idx_employee_role_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class EmployeeRole extends TenantEntity {

    @Column(length = 200)
    private String employeeName;

    @Column(length = 200)
    private String groupName;

    @Column(length = 200)
    private String roleName;

    private LocalDate effectiveDate;
}
