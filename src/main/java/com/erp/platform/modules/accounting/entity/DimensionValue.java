package com.erp.platform.modules.accounting.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "dimension_values",
       indexes = {@Index(name = "idx_dim_value_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_dim_value_dimension", columnList = "dimension_id")})
@Getter
@Setter
public class DimensionValue extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dimension_id", nullable = false)
    @JsonIgnore
    private Dimension dimension;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private boolean active = true;
}
