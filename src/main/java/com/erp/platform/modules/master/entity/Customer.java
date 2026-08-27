package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "customers",
       indexes = {
           @Index(name = "idx_customer_tenant", columnList = "tenant_id"),
           @Index(name = "idx_customer_email", columnList = "tenant_id, email"),
           @Index(name = "idx_customer_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class Customer extends TenantEntity {

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

    @Column(name = "billing_address", length = 500)
    private String billingAddress;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

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

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", precision = 18, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private CustomerCategory category = CustomerCategory.RETAIL;

    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays = 30;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    // ---- Sales module spec fields ----

    @Column(name = "sales_area", length = 200)
    private String salesArea;

    @Column(name = "sales_person", length = 200)
    private String salesPerson;

    @Column(name = "address_line1", length = 300)
    private String addressLine1;

    @Column(name = "address_line2", length = 300)
    private String addressLine2;

    @Column(length = 100)
    private String district;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "bank_account_number", length = 40)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 200)
    private String bankName;

    @Column(name = "bank_branch", length = 200)
    private String bankBranch;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "preferred_courier", length = 200)
    private String preferredCourier;

    @Column(name = "account_division", length = 50)
    private String accountDivision;

    /** Multi-select (PVT_LTD, COOPERATIVE, THIRD_PARTY) stored as a comma-separated list. */
    @Column(name = "customer_type", length = 200)
    private String customerType;

    /** Multi-select account heads stored as a comma-separated list. */
    @Column(name = "account_heads", length = 500)
    private String accountHeads;

    /** Sub-dealers list (name/address/state/city/zip/phone) stored as JSON. */
    @Column(name = "sub_dealers_json", columnDefinition = "text")
    private String subDealersJson;

    /** Deposit details (chequeNumber/depositDate/amount/chequeValidityDate) stored as JSON. */
    @Column(name = "deposits_json", columnDefinition = "text")
    private String depositsJson;

    public enum CustomerCategory {
        RETAIL, WHOLESALE, DISTRIBUTOR, DIRECT
    }
}
