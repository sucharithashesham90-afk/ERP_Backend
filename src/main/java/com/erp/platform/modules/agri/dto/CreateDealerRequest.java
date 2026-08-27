package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateDealerRequest {
    @NotBlank
    private String name;
    private String code;
    private String contactPerson;
    private String phone;
    private String mobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String dealerType;
    private UUID dealerRegionId;
    private BigDecimal creditLimit;
    private String accountCode;
    private String licenseNumber;
    private String panNumber;
    private String gstNumber;
    private String bankAccount;
    private String bankIfscCode;
    private String bankName;
    private boolean active = true;
}
