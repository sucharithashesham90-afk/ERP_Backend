package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class FarmerDto {

    private UUID id;
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
    private boolean shareholder;
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
    private boolean active;
    private boolean organizer;
    private String phoneNumber;
    private BigDecimal distanceFromPlant;
    private String distanceFromPlantUnit;
    private UUID mandalId;
    private String mandalName;
    private UUID organizerId;
    private List<FarmerLandRecordDto> landRecords;
    private LocalDateTime createdAt;
    private String photo;
}
