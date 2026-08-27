package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Customer.CustomerCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String mobile;
    private String contactPerson;
    private String billingAddress;
    private String shippingAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String taxNumber;
    private String panNumber;
    private String fax;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private CustomerCategory category;
    private Integer paymentTermsDays;
    private String notes;
    private boolean active;
    // Sales module spec
    private String salesArea;
    private String salesPerson;
    private String addressLine1;
    private String addressLine2;
    private String district;
    private String aadharNumber;
    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;
    private String ifscCode;
    private String currencyCode;
    private String preferredCourier;
    private String accountDivision;
    private String customerType;
    private String accountHeads;
    private String subDealersJson;
    private String depositsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String photo;
}
