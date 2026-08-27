package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "service_order_items",
       indexes = {
           @Index(name = "idx_svcitem_tenant", columnList = "tenant_id"),
           @Index(name = "idx_svcitem_order", columnList = "service_order_id")
       })
@Getter
@Setter
public class ServiceOrderItem extends TenantEntity {

    /**
     * The order this line belongs to, never written out.
     *
     * <p>The order serialises its items and each item pointed back at the order, so writing one out
     * walked the cycle until Jackson gave up. The list request then failed and the screen, which
     * treats a failed load as an empty result, showed nothing — an order that had just been saved
     * looked as though it had not been. Nothing needs the parent on a line: it is the thing that
     * contains it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    @JsonIgnore
    private ServiceOrder serviceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ServiceDefinition service;

    @Column(length = 500)
    private String description;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_rate", precision = 18, scale = 4)
    private BigDecimal unitRate = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 5, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
