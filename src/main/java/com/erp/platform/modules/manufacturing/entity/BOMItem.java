package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bom_items")
@Getter
@Setter
public class BOMItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    @JsonIgnore
    private BillOfMaterials bom;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "material_name", length = 200)
    private String materialName;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(length = 20)
    private String unit;

    @Column(length = 500)
    private String notes;
}
