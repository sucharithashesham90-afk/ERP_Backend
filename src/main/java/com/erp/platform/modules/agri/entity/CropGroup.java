package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crop_groups", indexes = {@Index(name = "idx_crop_group_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CropGroup extends TenantEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
