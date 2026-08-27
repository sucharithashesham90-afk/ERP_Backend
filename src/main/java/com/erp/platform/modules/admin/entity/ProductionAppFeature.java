package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "production_app_features",
        indexes = {@Index(name = "idx_prod_app_feat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProductionAppFeature extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String featureKey;

    @Column(nullable = false, length = 200)
    private String featureName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(length = 100)
    private String moduleContext;
}
