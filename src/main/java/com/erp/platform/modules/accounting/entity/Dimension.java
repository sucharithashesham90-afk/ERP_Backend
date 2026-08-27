package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dimensions",
       indexes = {@Index(name = "idx_dimension_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Dimension extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean mandatory = false;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "dimension", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DimensionValue> values = new ArrayList<>();
}
