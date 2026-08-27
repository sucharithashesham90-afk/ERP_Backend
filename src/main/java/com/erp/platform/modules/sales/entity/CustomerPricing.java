package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customer_pricing", indexes = {@Index(name = "idx_customer_pricing_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CustomerPricing extends TenantEntity {

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(length = 200)
    private String customerName;

    /** SKU id — the Product field on this screen sources from the SKU master, not generic products. */
    @Column(name = "product_id")
    private UUID productId;

    @Column(length = 200)
    private String productName;

    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(precision = 15, scale = 4)
    private BigDecimal price;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active = true;
}
