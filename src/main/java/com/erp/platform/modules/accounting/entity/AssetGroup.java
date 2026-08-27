package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "asset_groups",
       indexes = {
           @Index(name = "idx_ag_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ag_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class AssetGroup extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(name = "depreciation_method", length = 30)
    @Enumerated(EnumType.STRING)
    private DepreciationMethod depreciationMethod = DepreciationMethod.STRAIGHT_LINE;

    @Column(name = "useful_life_years")
    private int usefulLifeYears = 5;

    @Column(name = "depreciation_rate", precision = 8, scale = 4)
    private BigDecimal depreciationRate = BigDecimal.ZERO;

    @Column(name = "asset_ledger_id")
    private UUID assetLedgerId;

    @Column(name = "depreciation_ledger_id")
    private UUID depreciationLedgerId;

    @Column(nullable = false)
    private boolean active = true;

    public enum DepreciationMethod {
        STRAIGHT_LINE, WRITTEN_DOWN_VALUE
    }
}
