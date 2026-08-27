package com.erp.platform.modules.organization.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompanyDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String legalName;
    private String registrationNumber;
    private String taxNumber;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String logo;
    private String logoUrl;
    private boolean pdfWatermarkEnabled;
    private String gstin;
    private String pan;
    private String cin;
    private String tan;
    private String bankName;
    private String bankAccount;
    private String bankIFSC;
    private String bankBranch;
    private String financialYearStart;
    private String industry;
    private String currency;
    private boolean active;
    private LocalDateTime createdAt;
}
