package com.erp.platform.modules.auth.entity;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends AuditableEntity {

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_allowed_modules", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "module_key", length = 50)
    private Set<String> allowedModules = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_allowed_screens", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "screen_path", length = 120)
    private Set<String> allowedScreens = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
