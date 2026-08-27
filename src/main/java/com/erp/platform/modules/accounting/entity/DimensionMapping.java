package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "dimension_mappings",
       indexes = {@Index(name = "idx_dim_mapping_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_dim_mapping_reference", columnList = "tenant_id, reference_id")})
@Getter
@Setter
public class DimensionMapping extends TenantEntity {

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dimension_id", nullable = false)
    private Dimension dimension;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dimension_value_id", nullable = false)
    private DimensionValue dimensionValue;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String notes;
}
