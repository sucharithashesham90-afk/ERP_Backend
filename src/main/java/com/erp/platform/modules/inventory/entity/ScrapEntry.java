package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scrap_entries",
       indexes = {@Index(name = "idx_scrap_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ScrapEntry extends TenantEntity {

    public enum ScrapType {
        PRODUCTION_WASTE, EXPIRED_STOCK, DAMAGED_GOODS, QUALITY_REJECTION, PROCESSING_LOSS
    }

    public enum ScrapStatus {
        DRAFT, APPROVED, DISPOSED
    }

    @Column(name = "scrap_number", nullable = false, length = 50)
    private String scrapNumber;

    @Column(name = "scrap_date")
    private LocalDate scrapDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "scrap_type", length = 30)
    private ScrapType scrapType;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "scrap_value", precision = 18, scale = 2)
    private BigDecimal scrapValue;

    @Column(name = "recovery_value", precision = 18, scale = 2)
    private BigDecimal recoveryValue = BigDecimal.ZERO;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScrapStatus status = ScrapStatus.DRAFT;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "disposed_at")
    private LocalDateTime disposedAt;

    @Column(length = 1000)
    private String notes;
}
