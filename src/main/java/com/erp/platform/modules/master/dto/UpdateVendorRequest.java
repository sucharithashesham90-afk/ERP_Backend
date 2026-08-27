package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Vendor.VendorCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVendorRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Email(message = "Valid email required")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String mobile;

    @Size(max = 150)
    private String contactPerson;

    @Size(max = 500)
    private String address;

    @Size(max = 500)
    private String address2;

    @Size(max = 100)
    private String district;
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 50)
    private String taxNumber;

    @Size(max = 20)
    private String panNumber;

    @Size(max = 20)
    private String fax;

    @Size(max = 100)
    private String bankName;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 20)
    private String bankRoutingCode;

    private VendorCategory category;

    private Integer paymentTermsDays;

    @Size(max = 1000)
    private String notes;

    private Boolean active;
    private String photo;
}
