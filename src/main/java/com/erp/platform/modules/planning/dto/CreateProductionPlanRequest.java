package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionPlan.PlanType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductionPlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Plan type is required")
    private PlanType planType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String notes;

    private UUID salesPlanId;

    private UUID seedCategoryId;
    private String seedCategoryName;
    private UUID seasonPeriodId;
    private String seasonPeriodName;

    @Valid
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        private String productName;

        @NotNull(message = "Target quantity is required")
        private BigDecimal targetQuantity;

        private String unit;

        private int priorityOrder;

        private String notes;
    }
}
