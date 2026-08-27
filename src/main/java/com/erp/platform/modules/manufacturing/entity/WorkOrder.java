package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "work_orders",
       indexes = {
           @Index(name = "idx_wo_tenant", columnList = "tenant_id"),
           @Index(name = "idx_wo_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class WorkOrder extends TenantEntity {

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "bom_id")
    private UUID bomId;

    @Column(name = "planned_quantity", precision = 18, scale = 4)
    private BigDecimal plannedQuantity = BigDecimal.ZERO;

    @Column(name = "produced_quantity", precision = 18, scale = 4)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status = WorkOrderStatus.DRAFT;

    @Column(length = 10)
    private String priority = "MEDIUM";

    @Column(length = 1000)
    private String notes;

    @Column(name = "location_name", length = 150)
    private String locationName;

    public enum WorkOrderStatus {
        DRAFT, PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
