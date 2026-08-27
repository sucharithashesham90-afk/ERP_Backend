package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriDispatchPlan")
@Table(name = "agri_dispatch_plans", indexes = {@Index(name = "idx_dp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DispatchPlan extends TenantEntity {

    @Column(name = "plan_number", length = 50, nullable = false)
    private String planNumber;

    @Column(name = "plan_date")
    private LocalDate planDate;

    @Column(name = "sales_order_number", length = 50)
    private String salesOrderNumber;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "planned_qty_kgs", precision = 15, scale = 3)
    private BigDecimal plannedQtyKgs;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "planned_dispatch_date")
    private LocalDate plannedDispatchDate;

    @Column(name = "status", length = 20)
    private String status = "PLANNED";
}
