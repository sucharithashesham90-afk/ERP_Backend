package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateFieldProducerRequest {
    @NotBlank
    private String name;
    private String code;
    private String fatherName;
    private String contactPerson;
    private String phone;
    private String mobile;
    private String address;
    private String village;
    private UUID villageId;
    private String villageName;
    private String district;
    private String state;
    private String country;
    private String postalCode;
    private String landHolding;
    private String accountCode;
    private String panNumber;
    private String adharNo;
    private String folioNo;
    private boolean shareholder = false;
    private boolean organizer = false;
    private String bankAccount;
    private String bankIfscCode;
    private String bankName;
    private String bankBranch;
    private boolean active = true;
}
