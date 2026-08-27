package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Key/value configuration for the Sales module, including the privilege-driven
 * Sales Application Features toggles (category = FEATURE).
 */
@Entity
@Table(name = "sales_configs",
       indexes = {
           @Index(name = "idx_salescfg_tenant", columnList = "tenant_id"),
           @Index(name = "idx_salescfg_key", columnList = "tenant_id, config_key")
       })
@Getter
@Setter
public class SalesConfig extends TenantEntity {

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", length = 1000)
    private String configValue;

    @Column(length = 30)
    private String category; // GENERAL, FEATURE

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
