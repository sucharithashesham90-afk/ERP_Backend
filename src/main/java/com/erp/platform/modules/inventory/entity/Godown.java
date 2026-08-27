package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A Godown (storage building) at a location. Nets (bins) live inside a Godown.
 * Backs the /api/v1/inventory/godowns screen.
 */
@Entity
@Table(name = "godowns",
       indexes = {@Index(name = "idx_godown_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Godown extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 150)
    private String location;

    @Column(name = "machine_availability", nullable = false)
    private boolean machineAvailability = false;

    /** Account/material groups allowed in this godown — stored comma-separated. */
    @Column(name = "groups_csv", length = 1000)
    private String groupsCsv;

    /** Free-form "L*W*H". */
    @Column(length = 100)
    private String area;

    @Column(name = "cold_storage", nullable = false)
    private boolean coldStorage = false;

    @Column(name = "packing_material_storage", nullable = false)
    private boolean packingMaterialStorage = false;

    @Column(length = 20)
    private String ownership = "OWN";

    @Column(name = "storage_capacity", length = 50)
    private String storageCapacity;

    @Column(name = "storage_capacity_uom", length = 30)
    private String storageCapacityUom;

    @Column(nullable = false)
    private boolean active = true;
}
