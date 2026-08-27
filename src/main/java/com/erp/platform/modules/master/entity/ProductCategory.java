package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_categories",
       indexes = {@Index(name = "idx_prodcat_tenant", columnList = "tenant_id")})
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})   // a self-parented row can be listed as a proxy
public class ProductCategory extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    @Column(nullable = false)
    private boolean active = true;
}
