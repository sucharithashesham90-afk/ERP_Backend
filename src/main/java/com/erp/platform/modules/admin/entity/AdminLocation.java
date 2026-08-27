package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "admin_locations",
       indexes = {
           @Index(name = "idx_adminloc_tenant", columnList = "tenant_id"),
           @Index(name = "idx_adminloc_parent", columnList = "tenant_id, parent_id")
       })
@Getter
@Setter
public class AdminLocation extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(name = "location_type", length = 50)
    private String locationType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    /** For PROCESSING_PLANT: the Production Area this plant belongs to. */
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "parent_name", length = 200)
    private String parentName;
}
