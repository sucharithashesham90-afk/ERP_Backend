package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Stock period closing record. Backs /api/v1/inventory/stock-closings. */
@Entity
@Table(name = "stock_closings",
       indexes = {@Index(name = "idx_stockclosing_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class StockClosing extends TenantEntity {

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Column(name = "closed_by", length = 200)
    private String closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(length = 20)
    private String status = "CLOSED";

    @Column(name = "total_lots")
    private long totalLots;

    @Column(name = "total_bags")
    private long totalBags;

    @Column(name = "total_receipts")
    private long totalReceipts;

    @Column(name = "total_issues")
    private long totalIssues;
}
