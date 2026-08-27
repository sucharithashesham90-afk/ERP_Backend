package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The lots a dispatch would draw from, in first-expiry-first-out order.
 *
 * A plan is produced before anything is deducted so the pick can be shown to a user, and the
 * same plan is what {@code FefoService} consumes — a preview and a real dispatch never disagree.
 */
@Data
public class FefoAllocationDto {

    private UUID productId;
    private String productName;
    private UUID warehouseId;

    private BigDecimal requestedQuantity = BigDecimal.ZERO;
    private BigDecimal allocatedQuantity = BigDecimal.ZERO;

    /** Requested minus allocated. Above zero means there is not enough dispatchable lot stock. */
    private BigDecimal shortfallQuantity = BigDecimal.ZERO;

    /** False when the lots on hand cannot cover the request. */
    private boolean fullyAllocated;

    /** Lots skipped and why, so a short pick can be explained rather than just reported. */
    private List<String> warnings = new ArrayList<>();

    private List<FefoLine> lines = new ArrayList<>();

    @Data
    public static class FefoLine {
        private UUID lotStockId;
        private String lotNumber;
        private UUID warehouseId;
        private String warehouseName;
        private String storageLocationName;
        private LocalDate productionDate;
        private LocalDate expiryDate;
        /** Negative once past expiry. Null when the lot has no expiry date. */
        private Long daysToExpiry;
        private BigDecimal availableQuantity;
        private BigDecimal allocatedQuantity;
        private BigDecimal unitCost;
        private String status;
    }
}
