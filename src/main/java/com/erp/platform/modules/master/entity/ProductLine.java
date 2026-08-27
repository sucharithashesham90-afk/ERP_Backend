package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "product_lines",
       indexes = {
           @Index(name = "idx_prodline_tenant", columnList = "tenant_id"),
           @Index(name = "idx_prodline_brand", columnList = "tenant_id, brand_id")
       })
@Getter
@Setter
public class ProductLine extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(length = 200)
    private String category;

    @Column(name = "target_market", length = 500)
    private String targetMarket;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
