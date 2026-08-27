package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "process_templates",
       indexes = {
           @Index(name = "idx_ptmpl_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ptmpl_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class ProcessTemplate extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(name = "product_category", length = 200)
    private String productCategory;

    @Column(nullable = false)
    private boolean active = true;
}
