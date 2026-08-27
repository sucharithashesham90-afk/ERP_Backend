package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Shelf-life picture across all lots on hand, bucketed by how close to expiry they are. */
@Data
public class ExpiryOverviewDto {

    /** The horizon the "expiring soon" buckets were built against. */
    private int horizonDays;
    private LocalDate asOf;

    private BigDecimal expiredQuantity = BigDecimal.ZERO;
    private BigDecimal expiringQuantity = BigDecimal.ZERO;
    private BigDecimal healthyQuantity = BigDecimal.ZERO;
    private BigDecimal noExpiryQuantity = BigDecimal.ZERO;

    private long expiredLots;
    private long expiringLots;
    private long healthyLots;
    private long noExpiryLots;

    /** Value at risk: quantity past or nearing expiry costed at the lot's unit cost. */
    private BigDecimal expiredValue = BigDecimal.ZERO;
    private BigDecimal expiringValue = BigDecimal.ZERO;

    private List<Bucket> buckets = new ArrayList<>();
    /** Lots past or nearing expiry, soonest first — the work list. */
    private List<ExpiringLot> lots = new ArrayList<>();

    @Data
    public static class Bucket {
        private String label;
        private long lotCount;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal value = BigDecimal.ZERO;
    }

    @Data
    public static class ExpiringLot {
        private UUID lotStockId;
        private String lotNumber;
        private UUID productId;
        private String productName;
        private String warehouseName;
        private LocalDate expiryDate;
        private Long daysToExpiry;
        private BigDecimal quantityOnHand;
        private BigDecimal value;
        private String status;
        /** EXPIRED, CRITICAL, WARNING or OK — drives the colour in the UI. */
        private String severity;
    }
}
