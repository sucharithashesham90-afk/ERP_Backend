package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_targets",
       indexes = {
           @Index(name = "idx_target_tenant", columnList = "tenant_id"),
           @Index(name = "idx_target_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class SalesTarget extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String period;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SalesTerritory territory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_rep_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SalesRepresentative salesRep;

    @Column(name = "target_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal targetAmount = BigDecimal.ZERO;

    @Column(name = "target_quantity", precision = 18, scale = 4)
    private BigDecimal targetQuantity = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 18, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TargetStatus status = TargetStatus.ACTIVE;

    @Column(length = 1000)
    private String notes;

    public enum TargetStatus {
        ACTIVE, ACHIEVED, MISSED, CANCELLED
    }
}
