package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "server_configs", indexes = {@Index(name = "idx_server_config_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ServerConfig extends TenantEntity {

    @Column(name = "config_key", length = 200)
    private String configKey;

    @Column(name = "config_value", length = 1000)
    private String configValue;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "active")
    private boolean active = true;
}
