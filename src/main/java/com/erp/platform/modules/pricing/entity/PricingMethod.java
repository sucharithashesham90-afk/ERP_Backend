package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mst_pricing_methods", indexes = {@Index(name = "idx_mst_pm_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PricingMethod extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(name = "method_type", length = 30)
    private String methodType;

    @Column(length = 500)
    private String description;

    private boolean active = true;
}
