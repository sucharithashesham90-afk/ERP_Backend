package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "batch_history",
       indexes = {
           @Index(name = "idx_batch_tenant", columnList = "tenant_id"),
           @Index(name = "idx_batch_number", columnList = "tenant_id, batch_number"),
           @Index(name = "idx_batch_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class BatchHistory extends TenantEntity {

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "remaining_quantity", precision = 18, scale = 4)
    private BigDecimal remainingQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "PCS";

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.CREATED;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(length = 1000)
    private String notes;

    public enum BatchStatus {
        CREATED, IN_TESTING, APPROVED, REJECTED, DISPATCHED, CONSUMED, EXPIRED
    }
}
