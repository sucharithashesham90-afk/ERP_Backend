package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateOrganizerRequest {

    @NotBlank
    private String name;

    private String code;

    // Addresses
    private String officeAddress;
    private String plantAddress;

    // Geographic
    private UUID villageId;
    private String villageName;
    private UUID stateId;
    private String stateName;
    private UUID districtId;
    private String districtName;
    private String zip;

    // Contact
    private String officePhone;
    private String plantPhone;
    private String contactPerson;
    private String relationshipWithCompany;
    private String mobile;
    private String email;

    // Tax / ID
    private String panNo;
    private String tinNo;
    private String vatNo;
    private String sstNo;
    private String adharNo;

    // Storage capacity
    private BigDecimal ownGodownSqft;
    private BigDecimal hiredGodownSqft;

    // Processing capacity
    private BigDecimal ownMachineTph;
    private BigDecimal hiredMachineTph;

    // Licenses
    private String regCertNo;
    private String seedLicenseNo;
    private LocalDate seedLicenseValidity;
    private String seedCertAgency;
    private String plantRegNo;

    // Banking
    private String bankAccountNo;
    private String bankBranch;
    private String bankName;
    private String bankIfsc;

    // Flags
    private boolean active = true;
    private boolean supplier = false;

    // Multi-value (comma-separated on save)
    private String productionAreaIds;
    private String productionAreaNames;
    private String experienceCrops;
    private String accountHeads;
    private String photo;
}
