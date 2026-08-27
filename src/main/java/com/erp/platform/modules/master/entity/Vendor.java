package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "vendors",
       indexes = {
           @Index(name = "idx_vendor_tenant", columnList = "tenant_id"),
           @Index(name = "idx_vendor_email", columnList = "tenant_id, email")
       })
@Getter
@Setter
public class Vendor extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "photo", columnDefinition = "text")
    private String photo;

    @Column(length = 30)
    private String code;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 500)
    private String address;

    @Column(name = "address2", length = 500)
    private String address2;

    /** Sits between state and city in the address, matching the districts master. */
    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(length = 20)
    private String fax;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_routing_code", length = 20)
    private String bankRoutingCode;

    @Column(name = "outstanding_balance", precision = 18, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private VendorCategory category = VendorCategory.SUPPLIER;

    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays = 30;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    public enum VendorCategory {
        SUPPLIER, CONTRACTOR, SERVICE_PROVIDER, TRANSPORT
    }
}
