package com.erp.platform.modules.planning.dto;

import com.erp.platform.modules.planning.entity.ProductionPlan.PlanStatus;
import com.erp.platform.modules.planning.entity.ProductionPlan.PlanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProductionPlanDto {

    private UUID id;
    private UUID tenantId;
    private String planNumber;
    private String name;
    private PlanType planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
    private BigDecimal totalTargetQty;
    private BigDecimal totalProducedQty;
    private String notes;
    private UUID salesPlanId;
    private String salesPlanName;
    private UUID seedCategoryId;
    private String seedCategoryName;
    private UUID seasonPeriodId;
    private String seasonPeriodName;
    private List<ItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal targetQuantity;
        private BigDecimal producedQuantity;
        private String unit;
        private int priorityOrder;
        private String notes;
    }
}
