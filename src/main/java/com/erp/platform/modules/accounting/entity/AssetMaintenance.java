package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asset_maintenance",
       indexes = {
           @Index(name = "idx_am_tenant", columnList = "tenant_id"),
           @Index(name = "idx_am_asset",  columnList = "asset_id"),
           @Index(name = "idx_am_date",   columnList = "tenant_id, maintenance_date")
       })
@Getter
@Setter
public class AssetMaintenance extends TenantEntity {

    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "asset_code", length = 50)
    private String assetCode;

    @Column(name = "asset_name", length = 200)
    private String assetName;

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(name = "maintenance_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private MaintenanceType maintenanceType = MaintenanceType.PREVENTIVE;

    @Column(length = 1000)
    private String description;

    @Column(precision = 18, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(length = 200)
    private String vendor;

    @Column(name = "performed_by", length = 200)
    private String performedBy;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(length = 500)
    private String remarks;

    public enum MaintenanceType {
        PREVENTIVE, CORRECTIVE, BREAKDOWN, UPGRADE
    }

    public enum MaintenanceStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
