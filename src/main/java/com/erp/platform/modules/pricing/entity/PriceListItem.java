package com.erp.platform.modules.pricing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_list_items",
       indexes = {
           @Index(name = "idx_pli_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pli_pricelist", columnList = "price_list_id")
       })
@Getter
@Setter
public class PriceListItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PriceList priceList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "min_quantity", precision = 18, scale = 4)
    private BigDecimal minQuantity = BigDecimal.ONE;

    @Column(name = "max_quantity", precision = 18, scale = 4)
    private BigDecimal maxQuantity;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
