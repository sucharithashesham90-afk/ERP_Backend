package com.erp.platform.modules.dispatch.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "dispatch_packages",
        indexes = {
                @Index(name = "idx_dispatch_pkg_dispatch", columnList = "dispatch_id")
        })
@Getter
@Setter
public class DispatchPackage extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private Dispatch dispatch;

    @Column(name = "package_number", length = 50)
    private String packageNumber;

    @Column(name = "package_type", length = 20)
    private String packageType;

    @Column(precision = 10, scale = 3)
    private BigDecimal length;

    @Column(precision = 10, scale = 3)
    private BigDecimal width;

    @Column(precision = 10, scale = 3)
    private BigDecimal height;

    @Column(name = "gross_weight", precision = 18, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "net_weight", precision = 18, scale = 3)
    private BigDecimal netWeight;

    @Column(name = "seal_number", length = 100)
    private String sealNumber;
}
