package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "storage_locations",
       indexes = {@Index(name = "idx_storage_loc_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_storage_loc_warehouse", columnList = "tenant_id, warehouse_id"),
                  @Index(name = "idx_storage_loc_code", columnList = "tenant_id, warehouse_id, code")})
@Getter
@Setter
public class StorageLocation extends TenantEntity {

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 30)
    private LocationType locationType = LocationType.BIN;

    @Column(length = 20)
    private String aisle;

    @Column(length = 20)
    private String rack;

    @Column(length = 20)
    private String bin;

    @Column(precision = 18, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit = "PCS";

    @Column(name = "current_occupancy", precision = 18, scale = 2)
    private BigDecimal currentOccupancy = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(length = 500)
    private String notes;

    public enum LocationType {
        AISLE, RACK, BIN, FLOOR, COLD_STORAGE, QUARANTINE_ZONE, DISPATCH_AREA
    }
}
