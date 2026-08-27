package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A single inventory feature flag for a tenant. Backs /api/v1/inventory/app-features. */
@Entity
@Table(name = "inventory_app_features",
       uniqueConstraints = @UniqueConstraint(name = "uk_invfeat_tenant_key", columnNames = {"tenant_id", "feature_key"}),
       indexes = {@Index(name = "idx_invfeat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class InventoryAppFeature extends TenantEntity {

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled = true;
}
