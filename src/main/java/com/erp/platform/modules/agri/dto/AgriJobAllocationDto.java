package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class AgriJobAllocationDto {
    private UUID id;
    private String allocationType;
    private UUID allocateeId;
    private String allocateeName;
    private BigDecimal quantityKgs;
    private BigDecimal acreageAcres;
    private BigDecimal expectedYieldKgs;
    private String tempLotNumber;
}
