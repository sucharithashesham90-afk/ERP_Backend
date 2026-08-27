package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A single processing/manufacturing feature flag for a tenant. Backs /api/v1/manufacturing/app-features. */
@Entity
@Table(name = "manufacturing_app_features",
       uniqueConstraints = @UniqueConstraint(name = "uk_mfgfeat_tenant_key", columnNames = {"tenant_id", "feature_key"}),
       indexes = {@Index(name = "idx_mfgfeat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ManufacturingAppFeature extends TenantEntity {

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled = true;
}
