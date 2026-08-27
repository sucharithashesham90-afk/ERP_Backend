package com.erp.platform.modules.organization.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "branches",
       indexes = {@Index(name = "idx_branch_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Branch extends TenantEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "is_head_office")
    private boolean isHeadOffice = false;

    @Column(nullable = false)
    private boolean active = true;
}
