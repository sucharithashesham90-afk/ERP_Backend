package com.erp.platform.modules.auth.entity;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "module_key", length = 50)
    private String moduleKey;

    @Column(length = 20)
    private String action;

    private String description;
}
