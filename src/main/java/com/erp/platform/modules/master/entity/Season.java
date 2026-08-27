package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "seasons",
       indexes = {@Index(name = "idx_season_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Season extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30, unique = false)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
