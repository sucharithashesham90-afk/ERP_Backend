package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_hierarchy",
       indexes = {@Index(name = "idx_rolehier_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class RoleHierarchy extends TenantEntity {

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "parent_role", length = 100)
    private String parentRole;

    // Lower number = higher authority (1 = CEO, 2 = Director, 3 = Manager …)
    @Column(name = "level_order", nullable = false)
    private int levelOrder = 99;

    @Column(length = 500)
    private String description;
}
