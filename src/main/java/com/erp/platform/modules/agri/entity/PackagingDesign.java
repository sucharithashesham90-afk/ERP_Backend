package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "AgriPackagingDesign")
@Table(name = "agri_packaging_designs",
        indexes = {@Index(name = "idx_agri_pkg_design_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackagingDesign extends TenantEntity {

    @Column(length = 200)
    private String productName;

    @Column(length = 200)
    private String varietyLabel;

    @Column(length = 100)
    private String brandCode;

    @Column(length = 200)
    private String brandName;

    @Column(length = 200)
    private String packingMaterial;

    @Column(precision = 12, scale = 3)
    private BigDecimal packingQty;

    @Column(precision = 10, scale = 3)
    private BigDecimal netWeightKg;

    @Column(precision = 10, scale = 3)
    private BigDecimal grossWeightKg;

    @Column(precision = 10, scale = 3)
    private BigDecimal dimensionLength;

    @Column(precision = 10, scale = 3)
    private BigDecimal dimensionWidth;

    @Column(precision = 10, scale = 3)
    private BigDecimal dimensionHeight;

    @Column(length = 100)
    private String packSize;

    @Column(nullable = false)
    private boolean active = true;
}
