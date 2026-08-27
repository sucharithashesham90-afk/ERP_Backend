package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionJobAllocation.AllocateeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateProductionJobAllocationRequest {

    @NotNull
    private UUID jobId;

    @NotNull
    private AllocateeType allocateeType;

    private String subType;

    private UUID referenceId;
    private String referenceName;

    private BigDecimal quantityKgs;
    private BigDecimal acreageAcres;
    private BigDecimal advancePaid;
}
