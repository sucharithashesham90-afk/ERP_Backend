package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateFarmerRequest {

    @NotBlank
    private String name;

    private String fathersName;
    private String nearestRlyStn;
    private String telegraphOff;
    private UUID stateId;
    private String stateName;
    private UUID districtId;
    private String districtName;
    private String zip;
    private String farmerCode;
    private boolean shareholder = false;
    private String folioNo;
    private String mobileNo;
    private String farmerType;
    private String accountHeads;
    private String adharCard;
    private String bankName;
    private String bankAccountNo;
    private String bankIfsc;
    private String bankBranch;
    private String documentType;
    private UUID villageId;
    private String villageName;
    private BigDecimal distanceFromHighway;
    private String distanceFromHighwayUnit;
    private String nearestTown;
    private String talukaOrTehsil;
    private boolean active = true;
    private boolean organizer = false;
    private String phoneNumber;
    private BigDecimal distanceFromPlant;
    private String distanceFromPlantUnit;
    private UUID mandalId;
    private String mandalName;
    private List<FarmerLandRecordDto> landRecords;
    private String photo;
}
