package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_assets",
       indexes = {
           @Index(name = "idx_fa_tenant", columnList = "tenant_id"),
           @Index(name = "idx_fa_code", columnList = "tenant_id, asset_code"),
           @Index(name = "idx_fa_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class FixedAsset extends TenantEntity {

    @Column(name = "asset_code", nullable = false, length = 50)
    private String assetCode;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_group_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetGroup assetGroup;

    @Column(length = 1000)
    private String description;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost", precision = 18, scale = 2)
    private BigDecimal purchaseCost = BigDecimal.ZERO;

    @Column(name = "salvage_value", precision = 18, scale = 2)
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "useful_life_years")
    private int usefulLifeYears = 5;

    @Column(name = "depreciation_method", length = 30)
    @Enumerated(EnumType.STRING)
    private AssetGroup.DepreciationMethod depreciationMethod = AssetGroup.DepreciationMethod.STRAIGHT_LINE;

    @Column(name = "depreciation_rate", precision = 8, scale = 4)
    private BigDecimal depreciationRate = BigDecimal.ZERO;

    @Column(name = "current_book_value", precision = 18, scale = 2)
    private BigDecimal currentBookValue = BigDecimal.ZERO;

    @Column(name = "accumulated_depreciation", precision = 18, scale = 2)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(length = 200)
    private String location;

    @Column(name = "assigned_to", length = 200)
    private String assignedTo;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "disposal_amount", precision = 18, scale = 2)
    private BigDecimal disposalAmount;

    @Column(nullable = false)
    private boolean active = true;

    public enum AssetStatus {
        ACTIVE, DISPOSED, WRITTEN_OFF, UNDER_MAINTENANCE
    }
}
