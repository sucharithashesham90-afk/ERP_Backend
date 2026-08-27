package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_defaults", indexes = {@Index(name = "idx_account_default_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AccountDefault extends TenantEntity {

    @Column(name = "default_key", length = 200)
    private String defaultKey;

    @Column(name = "default_value", length = 1000)
    private String defaultValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "period_type", length = 20)
    private String periodType;

    @Column(name = "active")
    private boolean active = true;
}
