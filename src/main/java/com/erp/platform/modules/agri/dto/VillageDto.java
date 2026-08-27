package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class VillageDto {

    private UUID id;
    private String name;
    private String villageCode;

    private UUID stateId;
    private String stateName;
    private UUID districtId;
    private String districtName;
    private UUID mandalId;
    private String mandalName;
    private String zip;

    private UUID productionAreaId;
    private String productionAreaName;

    private String inchargeIds;
    private String inchargeNames;

    private String telegraphOffice;
    private String nearestRailwayStn;
    private String nearestPostOffice;
    private String nearestTown;

    private boolean active;
    private LocalDateTime createdAt;
}
