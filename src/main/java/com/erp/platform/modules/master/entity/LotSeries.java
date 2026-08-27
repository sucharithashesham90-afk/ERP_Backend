package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lot_series", indexes = {@Index(name = "idx_ls_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LotSeries extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String prefix;

    @Column(length = 20)
    private String suffix;

    @Column(name = "next_number")
    private Long nextNumber = 1L;

    @Column(name = "increment_by")
    private Integer incrementBy = 1;

    @Column(name = "padding")
    private Integer padding = 4;

    @Column(length = 500)
    private String description;

    private boolean active = true;
}
