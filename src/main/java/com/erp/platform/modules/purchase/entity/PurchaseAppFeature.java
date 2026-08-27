package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A single purchase feature flag for a tenant. Backs /api/v1/purchase/app-features. */
@Entity
@Table(name = "purchase_app_features",
       uniqueConstraints = @UniqueConstraint(name = "uk_purfeat_tenant_key", columnNames = {"tenant_id", "feature_key"}),
       indexes = {@Index(name = "idx_purfeat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PurchaseAppFeature extends TenantEntity {

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled = true;
}
