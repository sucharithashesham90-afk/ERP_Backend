package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "field_producers",
       indexes = {@Index(name = "idx_field_prod_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class FieldProducer extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30, unique = false)
    private String code;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(length = 100)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String village;

    @Column(name = "village_id")
    private UUID villageId;

    @Column(name = "village_name", length = 200)
    private String villageName;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 20)
    private String landHolding;

    /** Assigned vendor/ledger account code */
    @Column(length = 30)
    private String accountCode;

    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(length = 20)
    private String panNumber;

    @Column(name = "adhar_no", length = 20)
    private String adharNo;

    @Column(name = "folio_no", length = 50)
    private String folioNo;

    @Column(name = "is_shareholder")
    private boolean shareholder = false;

    @Column(name = "is_organizer")
    private boolean organizer = false;

    @Column(length = 20)
    private String bankAccount;

    @Column(length = 20)
    private String bankIfscCode;

    @Column(length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 200)
    private String bankBranch;

    @Column(nullable = false)
    private boolean active = true;
}
