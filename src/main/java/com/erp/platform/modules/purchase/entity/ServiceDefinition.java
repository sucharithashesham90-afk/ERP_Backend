package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Tax;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "service_definitions",
       indexes = {
           @Index(name = "idx_svcdef_tenant", columnList = "tenant_id"),
           @Index(name = "idx_svcdef_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class ServiceDefinition extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ServiceCategory category;

    @Column(length = 1000)
    private String description;

    @Column(length = 30)
    private String unit;

    /** Is this a work-order service (per Purchase doc). */
    @Column(name = "work_order", columnDefinition = "boolean not null default false")
    private boolean workOrder = false;

    /** Material group (drop-down populated from material groups). */
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "group_name", length = 150)
    private String groupName;

    @Column(name = "standard_rate", precision = 18, scale = 4)
    private BigDecimal standardRate = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tax taxRate;

    @Column(nullable = false)
    private boolean active = true;
}
