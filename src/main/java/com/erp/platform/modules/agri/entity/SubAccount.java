package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sub_accounts", indexes = {@Index(name = "idx_sub_account_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SubAccount extends TenantEntity {

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "sub_account_type", length = 50)
    private String subAccountType;

    @Column(name = "parent_account_code", length = 50)
    private String parentAccountCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "plant_variant_id")
    private UUID plantVariantId;

    @Column(name = "plant_variant_name", length = 200)
    private String plantVariantName;

    @Column(name = "active")
    private boolean active = true;
}
