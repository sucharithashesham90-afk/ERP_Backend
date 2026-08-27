package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subcontract_orders",
       indexes = {
           @Index(name = "idx_sco_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sco_vendor", columnList = "tenant_id, vendor_id"),
           @Index(name = "idx_sco_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class SubcontractOrder extends TenantEntity {

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "received_qty", precision = 18, scale = 4)
    private BigDecimal receivedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SCOStatus status = SCOStatus.DRAFT;

    @Column(length = 1000)
    private String notes;

    public enum SCOStatus {
        DRAFT, SENT, CONFIRMED, IN_PROGRESS, RECEIVED, CANCELLED
    }
}
