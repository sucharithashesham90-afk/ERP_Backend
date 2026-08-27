package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "process_orders",
       indexes = {
           @Index(name = "idx_pord_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pord_work_order", columnList = "work_order_id"),
           @Index(name = "idx_pord_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class ProcessOrder extends TenantEntity {

    @Column(name = "process_number", nullable = false, length = 50)
    private String processNumber;

    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;

    @Column(name = "process_type", length = 50)
    private String processType;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "input_material_id")
    private UUID inputMaterialId;

    @Column(name = "output_product_id")
    private UUID outputProductId;

    @Column(name = "input_quantity", precision = 18, scale = 4)
    private BigDecimal inputQuantity = BigDecimal.ZERO;

    @Column(name = "output_quantity", precision = 18, scale = 4)
    private BigDecimal outputQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "PCS";

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(length = 100)
    private String machine;

    @Column(length = 1000)
    private String notes;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "location_name", length = 150)
    private String locationName;
}
