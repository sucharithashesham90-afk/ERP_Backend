package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class FarmerLandRecordDto {
    private UUID id;
    private UUID villageId;
    private String villageName;
    private String plotSurveyNo;
    private BigDecimal acreage;
    private String landType;
}
