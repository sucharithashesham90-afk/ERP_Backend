package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discount_schemes",
       indexes = {
           @Index(name = "idx_discount_tenant", columnList = "tenant_id"),
           @Index(name = "idx_discount_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class DiscountScheme extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(name = "scheme_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private SchemeType schemeType = SchemeType.FLAT_PERCENTAGE;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "customer_type", length = 30)
    private String customerType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public enum SchemeType {
        FLAT_PERCENTAGE, SLAB_QUANTITY, SLAB_VALUE, BUY_X_GET_Y
    }
}
