package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "dealers",
       indexes = {@Index(name = "idx_dealer_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Dealer extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 100)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(length = 100)
    private String email;

    @Column(length = 250)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    /** DISTRIBUTOR / DEALER / RETAILER */
    @Column(length = 20)
    private String dealerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_region_id")
    private DealerRegion dealerRegion;

    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(length = 30)
    private String accountCode;

    @Column(length = 50)
    private String licenseNumber;

    @Column(length = 20)
    private String panNumber;

    @Column(length = 20)
    private String gstNumber;

    @Column(length = 20)
    private String bankAccount;

    @Column(length = 20)
    private String bankIfscCode;

    @Column(length = 100)
    private String bankName;

    @Column(nullable = false)
    private boolean active = true;
}
