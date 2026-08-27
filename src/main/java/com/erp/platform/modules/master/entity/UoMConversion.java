package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "uom_conversions",
       indexes = {
           @Index(name = "idx_uom_conv_tenant", columnList = "tenant_id"),
           @Index(name = "idx_uom_conv_from", columnList = "tenant_id, from_uom_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_uom_conv_tenant_from_to",
                             columnNames = {"tenant_id", "from_uom_id", "to_uom_id"})
       })
@Getter
@Setter
public class UoMConversion extends TenantEntity {

    @Column(name = "from_uom_id", nullable = false)
    private UUID fromUomId;

    @Column(name = "from_uom_code", length = 20)
    private String fromUomCode;

    @Column(name = "to_uom_id", nullable = false)
    private UUID toUomId;

    @Column(name = "to_uom_code", length = 20)
    private String toUomCode;

    @Column(name = "conversion_factor", nullable = false, precision = 18, scale = 8)
    private BigDecimal conversionFactor;

    @Column(nullable = false)
    private boolean bidirectional = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
