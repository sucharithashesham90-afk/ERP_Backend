package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_categories",
       indexes = {
           @Index(name = "idx_svccat_tenant", columnList = "tenant_id"),
           @Index(name = "idx_svccat_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class ServiceCategory extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
