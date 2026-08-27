package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leave_types",
       indexes = {@Index(name = "idx_lvtype_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LeaveType extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(name = "days_allowed")
    private int daysAllowed;

    @Column(name = "carry_forward")
    private boolean carryForward = false;

    private boolean paid = true;

    @Column(nullable = false)
    private boolean active = true;
}
