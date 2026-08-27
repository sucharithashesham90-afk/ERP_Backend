package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "nurseries",
        indexes = {@Index(name = "idx_nursery_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Nursery extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 150)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String village;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(precision = 12, scale = 3)
    private BigDecimal capacityKgs;

    @Column(nullable = false)
    private boolean active = true;
}
