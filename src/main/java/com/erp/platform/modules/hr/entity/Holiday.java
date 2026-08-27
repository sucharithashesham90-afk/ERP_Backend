package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "holidays",
       indexes = {@Index(name = "idx_holiday_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Holiday extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 20)
    private String type; // PUBLIC, OPTIONAL, COMPANY

    @Column(length = 500)
    private String description;

    private int year;

    @Column(nullable = false)
    private boolean active = true;
}
