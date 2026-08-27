package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "price_lists",
       indexes = {
           @Index(name = "idx_pricelist_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pricelist_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class PriceList extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30, unique = false)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(length = 10)
    private String currency = "INR";

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean active = true;
}
