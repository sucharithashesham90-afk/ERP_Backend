package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_specifications",
       indexes = {
           @Index(name = "idx_prodspec_tenant", columnList = "tenant_id"),
           @Index(name = "idx_prodspec_product", columnList = "product_id")
       })
@Getter
@Setter
public class ProductSpecification extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "spec_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private SpecType specType = SpecType.CUSTOM;

    @Column(name = "attribute_name", nullable = false, length = 200)
    private String attributeName;

    @Column(name = "attribute_value", length = 500)
    private String attributeValue;

    @Column(length = 30)
    private String unit;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "is_mandatory")
    private boolean isMandatory = false;

    @Column(name = "display_order")
    private int displayOrder = 0;

    public enum SpecType {
        DIMENSION, WEIGHT, MATERIAL, CHEMICAL, ELECTRICAL, MECHANICAL, QUALITY, CERTIFICATION, CUSTOM
    }
}
