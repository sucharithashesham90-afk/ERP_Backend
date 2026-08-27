package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounting_configs",
       indexes = {
           @Index(name = "idx_acfg_tenant", columnList = "tenant_id"),
           @Index(name = "idx_acfg_key",    columnList = "tenant_id,config_key", unique = true)
       })
@Getter
@Setter
public class AccountingConfig extends TenantEntity {

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", length = 500)
    private String configValue;

    @Column(length = 100)
    private String category; // GENERAL, TAX, VOUCHER, PERIOD, FEATURE

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
