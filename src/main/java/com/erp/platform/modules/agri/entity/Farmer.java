package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agri_farmers", indexes = {@Index(name = "idx_farmer_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Farmer extends TenantEntity {

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "photo", columnDefinition = "text")
    private String photo;

    @Column(name = "fathers_name", length = 200)
    private String fathersName;

    @Column(name = "nearest_rly_stn", length = 200)
    private String nearestRlyStn;

    @Column(name = "telegraph_off", length = 200)
    private String telegraphOff;

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

    @Column(name = "farmer_code", length = 50)
    private String farmerCode;

    @Column(name = "is_shareholder")
    private boolean shareholder = false;

    @Column(name = "folio_no", length = 50)
    private String folioNo;

    @Column(name = "mobile_no", length = 20)
    private String mobileNo;

    @Column(name = "farmer_type", length = 100)
    private String farmerType;

    @Column(name = "account_heads", columnDefinition = "TEXT")
    private String accountHeads;

    @Column(name = "adhar_card", length = 20)
    private String adharCard;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    @Column(name = "bank_branch", length = 200)
    private String bankBranch;

    @Column(name = "document_type", length = 100)
    private String documentType;

    @Column(name = "village_id")
    private UUID villageId;

    @Column(name = "village_name", length = 200)
    private String villageName;

    @Column(name = "distance_from_highway", precision = 8, scale = 2)
    private BigDecimal distanceFromHighway;

    @Column(name = "distance_from_highway_unit", length = 10)
    private String distanceFromHighwayUnit;

    @Column(name = "nearest_town", length = 200)
    private String nearestTown;

    @Column(name = "taluka_or_tehsil", length = 200)
    private String talukaOrTehsil;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "is_organizer")
    private boolean organizer = false;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "distance_from_plant", precision = 8, scale = 2)
    private BigDecimal distanceFromPlant;

    @Column(name = "distance_from_plant_unit", length = 10)
    private String distanceFromPlantUnit;

    @Column(name = "mandal_id")
    private UUID mandalId;

    @Column(name = "mandal_name", length = 200)
    private String mandalName;

    @Column(name = "organizer_id")
    private UUID organizerId;
}
