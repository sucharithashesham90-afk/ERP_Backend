package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organizers", indexes = {@Index(name = "idx_organizer_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Organizer extends TenantEntity {

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "photo", columnDefinition = "text")
    private String photo;

    @Column(name = "code", length = 50)
    private String code;

    // Addresses
    @Column(name = "office_address", length = 500)
    private String officeAddress;

    @Column(name = "plant_address", length = 500)
    private String plantAddress;

    // Geographic
    @Column(name = "village_id")
    private UUID villageId;

    @Column(name = "village_name", length = 200)
    private String villageName;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "state_name", length = 200)
    private String stateName;

    @Column(name = "district_id")
    private UUID districtId;

    @Column(name = "district_name", length = 200)
    private String districtName;

    @Column(name = "zip", length = 10)
    private String zip;

    // Contact
    @Column(name = "office_phone", length = 20)
    private String officePhone;

    @Column(name = "plant_phone", length = 20)
    private String plantPhone;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "relationship_with_company", length = 100)
    private String relationshipWithCompany;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "email", length = 100)
    private String email;

    // Tax / ID
    @Column(name = "pan_no", length = 20)
    private String panNo;

    @Column(name = "tin_no", length = 20)
    private String tinNo;

    @Column(name = "vat_no", length = 20)
    private String vatNo;

    @Column(name = "sst_no", length = 20)
    private String sstNo;

    @Column(name = "adhar_no", length = 20)
    private String adharNo;

    // Storage capacity
    @Column(name = "own_godown_sqft", precision = 12, scale = 2)
    private BigDecimal ownGodownSqft;

    @Column(name = "hired_godown_sqft", precision = 12, scale = 2)
    private BigDecimal hiredGodownSqft;

    // Processing capacity
    @Column(name = "own_machine_tph", precision = 12, scale = 2)
    private BigDecimal ownMachineTph;

    @Column(name = "hired_machine_tph", precision = 12, scale = 2)
    private BigDecimal hiredMachineTph;

    // Licenses & Registration
    @Column(name = "reg_cert_no", length = 100)
    private String regCertNo;

    @Column(name = "seed_license_no", length = 100)
    private String seedLicenseNo;

    @Column(name = "seed_license_validity")
    private LocalDate seedLicenseValidity;

    @Column(name = "seed_cert_agency", length = 200)
    private String seedCertAgency;

    @Column(name = "plant_reg_no", length = 100)
    private String plantRegNo;

    // Banking
    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "bank_branch", length = 200)
    private String bankBranch;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    // Flags
    @Column(name = "active")
    private boolean active = true;

    @Column(name = "is_supplier")
    private boolean supplier = false;

    // Multi-value (comma-separated)
    @Column(name = "production_area_ids", columnDefinition = "TEXT")
    private String productionAreaIds;

    @Column(name = "production_area_names", columnDefinition = "TEXT")
    private String productionAreaNames;

    @Column(name = "experience_crops", columnDefinition = "TEXT")
    private String experienceCrops;

    @Column(name = "account_heads", columnDefinition = "TEXT")
    private String accountHeads;

    @Column(name = "vendor_id")
    private UUID vendorId;
}
