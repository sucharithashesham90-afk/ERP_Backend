package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.SalesPlan.PlanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSalesPlanRequest {

    @NotBlank
    private String name;

    @NotNull
    private PlanType planType;

    @NotNull
    private Integer planYear;

    private Integer planQuarter;

    private Integer planMonth;

    private String territory;

    private UUID salesRepId;

    private String salesRepName;

    private String notes;

    private List<CreateSalesPlanTargetRequest> targets = new ArrayList<>();
}
