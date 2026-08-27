package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "SalesDeliveryOrder")
@Table(name = "sales_delivery_orders",
        indexes = {@Index(name = "idx_sales_do_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CustomerDeliveryOrder extends TenantEntity {

    @Column(length = 100)
    private String doNumber;

    private LocalDate doDate;

    @Column(length = 100)
    private String salesOrderNumber;

    @Column(length = 200)
    private String customerName;

    @Column(length = 50)
    private String lorryNumber;

    @Column(length = 200)
    private String carrier;

    @Column(precision = 12, scale = 2)
    private BigDecimal freightToPay;

    @Column(length = 200)
    private String deliveredFrom;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";
}
