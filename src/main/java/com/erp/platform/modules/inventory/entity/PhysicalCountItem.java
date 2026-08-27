package com.erp.platform.modules.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "physical_count_items",
       indexes = {
           @Index(name = "idx_pci_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pci_count", columnList = "count_id")
       })
@Getter
@Setter
public class PhysicalCountItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "count_id", nullable = false)
    @JsonIgnore
    private PhysicalCount physicalCount;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "system_quantity", precision = 18, scale = 4)
    private BigDecimal systemQuantity = BigDecimal.ZERO;

    @Column(name = "counted_quantity", precision = 18, scale = 4)
    private BigDecimal countedQuantity = BigDecimal.ZERO;

    @Column(name = "difference_quantity", precision = 18, scale = 4)
    private BigDecimal differenceQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;

    @Column(length = 500)
    private String remarks;
}
