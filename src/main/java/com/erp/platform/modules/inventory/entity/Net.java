package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A Net (bin / compartment) inside a Godown. Backs /api/v1/inventory/nets.
 */
@Entity
@Table(name = "nets",
       indexes = {@Index(name = "idx_net_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_net_godown", columnList = "godown_id")})
@Getter
@Setter
public class Net extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 150)
    private String location;

    @Column(name = "godown_id")
    private UUID godownId;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(length = 200)
    private String landmark;

    /** Free-form "L*W*H". */
    @Column(length = 100)
    private String dimension;

    @Column(name = "position_in_godown", length = 150)
    private String positionInGodown;

    @Column(nullable = false)
    private boolean active = true;
}
