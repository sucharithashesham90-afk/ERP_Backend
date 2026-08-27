package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pricing_schemes", indexes = {@Index(name = "idx_ps_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PricingScheme extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "price_list_id")
    private UUID priceListId;

    @Column(name = "tiers_json", columnDefinition = "TEXT")
    private String tiersJson;

    private boolean active = true;
}
