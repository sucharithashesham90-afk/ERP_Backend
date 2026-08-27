package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "depreciation_entries",
       indexes = {
           @Index(name = "idx_de_tenant", columnList = "tenant_id"),
           @Index(name = "idx_de_asset", columnList = "asset_id"),
           @Index(name = "idx_de_date", columnList = "tenant_id, depreciation_date, posted")
       })
@Getter
@Setter
public class DepreciationEntry extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FixedAsset asset;

    @Column(name = "period_year")
    private int periodYear;

    @Column(name = "period_month")
    private int periodMonth;

    @Column(name = "depreciation_date")
    private LocalDate depreciationDate;

    @Column(name = "opening_value", precision = 18, scale = 2)
    private BigDecimal openingValue = BigDecimal.ZERO;

    @Column(name = "depreciation_amount", precision = 18, scale = 2)
    private BigDecimal depreciationAmount = BigDecimal.ZERO;

    @Column(name = "closing_value", precision = 18, scale = 2)
    private BigDecimal closingValue = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean posted = false;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;
}
