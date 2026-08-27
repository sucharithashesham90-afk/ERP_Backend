package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "group_ranges",
       indexes = {@Index(name = "idx_gr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class GroupRange extends TenantEntity {

    @Column(name = "group_code", nullable = false, length = 30)
    private String groupCode;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "from_code", nullable = false, length = 30)
    private String fromCode;

    @Column(name = "to_code", nullable = false, length = 30)
    private String toCode;

    @Column(length = 500)
    private String description;
}
