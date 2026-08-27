package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionJobAllocation.AllocateeType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductionJobAllocationDto {
    private UUID id;
    private UUID jobId;
    private AllocateeType allocateeType;
    private String subType;
    private UUID referenceId;
    private String referenceName;
    private BigDecimal quantityKgs;
    private BigDecimal acreageAcres;
    private BigDecimal expectedYieldKgs;
    private BigDecimal advancePaid;
    private String temporaryLotNumber;
    private boolean initiated;
    private LocalDateTime createdAt;
}
