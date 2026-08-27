package com.erp.platform.modules.supplier.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "supplier_performance",
        indexes = {
                @Index(name = "idx_sper_tenant", columnList = "tenant_id"),
                @Index(name = "idx_sp_vendor", columnList = "tenant_id, vendor_id")
        })
@Getter
@Setter
public class SupplierPerformance extends TenantEntity {

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "evaluation_period", length = 50)
    private String evaluationPeriod;

    @Column(name = "evaluation_date")
    private LocalDate evaluationDate;

    @Column(name = "on_time_delivery_score", precision = 5, scale = 2)
    private BigDecimal onTimeDeliveryScore;

    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "response_score", precision = 5, scale = 2)
    private BigDecimal responseScore;

    @Column(name = "pricing_score", precision = 5, scale = 2)
    private BigDecimal pricingScore;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "total_orders")
    private int totalOrders;

    @Column(name = "on_time_orders")
    private int onTimeOrders;

    @Column(name = "late_orders")
    private int lateOrders;

    @Column(name = "rejected_lots")
    private int rejectedLots;

    @Column(name = "accepted_lots")
    private int acceptedLots;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PerfStatus status;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "evaluated_by", length = 100)
    private String evaluatedBy;

    public enum PerfStatus {
        PREFERRED, APPROVED, CONDITIONAL, BLACKLISTED
    }
}
