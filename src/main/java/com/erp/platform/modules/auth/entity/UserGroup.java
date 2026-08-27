package com.erp.platform.modules.auth.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_groups",
       indexes = { @Index(name = "idx_ug_tenant", columnList = "tenant_id") })
@Getter
@Setter
public class UserGroup extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "group_leader", length = 150)
    private String groupLeader;

    @Column(name = "group_email", length = 150)
    private String groupEmail;

    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    // Comma-separated role names that members of this group can be assigned
    @Column(name = "allowed_roles", length = 2000)
    private String allowedRoles;
}
