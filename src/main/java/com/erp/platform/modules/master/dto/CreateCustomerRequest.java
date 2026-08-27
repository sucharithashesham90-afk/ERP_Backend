package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Customer.CustomerCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 30)
    private String code;

    @Email(message = "Valid email required")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
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

    @PositiveOrZero(message = "Credit limit must be non-negative")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    private CustomerCategory category = CustomerCategory.RETAIL;

    @Min(0)
    private Integer paymentTermsDays = 30;

    private String notes;

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
    private String photo;
}
