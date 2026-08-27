package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "discount_slabs",
       indexes = {
           @Index(name = "idx_dslab_tenant", columnList = "tenant_id"),
           @Index(name = "idx_dslab_scheme", columnList = "scheme_id")
       })
@Getter
@Setter
public class DiscountSlab extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DiscountScheme scheme;

    @Column(name = "min_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "free_quantity", precision = 18, scale = 4)
    private BigDecimal freeQuantity = BigDecimal.ZERO;
}
