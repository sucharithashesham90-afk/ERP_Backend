package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.ExpiryOverviewDto;
import com.erp.platform.modules.inventory.dto.FefoAllocationDto;
import com.erp.platform.modules.inventory.entity.LotMovement;
import com.erp.platform.modules.inventory.entity.LotMovement.LotMovType;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.entity.LotStock.LotStockStatus;
import com.erp.platform.modules.inventory.repository.LotMovementRepository;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * First-expiry-first-out stock allocation.
 *
 * Dispatch draws from the lot that expires soonest, so short-dated stock leaves the warehouse
 * before it becomes a write-off. Where FIFO would order by when a lot arrived, FEFO orders by
 * when it dies — for seed, produce and anything with a shelf life those are rarely the same
 * order, and only the second one prevents waste.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FefoService {

    /** Lots that may be dispatched. QUARANTINE and EXPIRED are deliberately excluded. */
    private static final Set<LotStockStatus> DISPATCHABLE =
            EnumSet.of(LotStockStatus.AVAILABLE, LotStockStatus.RELEASED);

    private static final int CRITICAL_DAYS = 30;
    private static final int DEFAULT_HORIZON_DAYS = 90;

    private final LotStockRepository lotStockRepository;
    private final LotMovementRepository lotMovementRepository;
    /** Seed lots live in a separate table; the expiry sweep has to cover both. */
    private final com.erp.platform.modules.inventory.repository.StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;

    // ── Allocation ───────────────────────────────────────────────────────────

    /**
     * Works out which lots would cover {@code quantity} of a product, soonest expiry first.
     * Nothing is deducted — this is safe to call to show a user the pick before they commit.
     *
     * @param warehouseId restricts the pick to one warehouse; null draws from all of them
     */
    public FefoAllocationDto plan(UUID productId, UUID warehouseId, BigDecimal quantity) {
        UUID tenantId = tenantContext.current();
        if (productId == null) throw AppException.badRequest("Product is required for a FEFO pick");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Quantity must be greater than zero");
        }

        FefoAllocationDto plan = new FefoAllocationDto();
        plan.setProductId(productId);
        plan.setWarehouseId(warehouseId);
        plan.setRequestedQuantity(quantity);

        List<LotStock> candidates = dispatchableLots(tenantId, productId, warehouseId, plan);
        LocalDate today = LocalDate.now();

        BigDecimal remaining = quantity;
        for (LotStock lot : candidates) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal free = freeQuantity(lot);
            if (free.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal take = free.min(remaining);
            plan.getLines().add(toLine(lot, free, take, today));
            remaining = remaining.subtract(take);
            if (plan.getProductName() == null) plan.setProductName(lot.getProductName());
        }

        BigDecimal allocated = quantity.subtract(remaining);
        plan.setAllocatedQuantity(allocated);
        plan.setShortfallQuantity(remaining.max(BigDecimal.ZERO));
        plan.setFullyAllocated(remaining.compareTo(BigDecimal.ZERO) <= 0);
        if (!plan.isFullyAllocated()) {
            plan.getWarnings().add("Short by " + remaining.stripTrailingZeros().toPlainString()
                    + " — not enough dispatchable lot stock on hand");
        }
        return plan;
    }

    /**
     * Deducts the planned quantities from their lots and records an ISSUE movement against each,
     * so a dispatch is traceable to the exact lots that left. Call inside the dispatching
     * transaction: a shortfall throws, rolling the whole dispatch back rather than shipping short.
     */
    @Transactional
    public FefoAllocationDto consume(UUID productId, UUID warehouseId, BigDecimal quantity,
                                     String referenceType, UUID referenceId, String referenceNumber) {
        UUID tenantId = tenantContext.current();
        FefoAllocationDto plan = plan(productId, warehouseId, quantity);

        if (!plan.isFullyAllocated()) {
            throw AppException.insufficientStock(
                    "FEFO pick short by " + plan.getShortfallQuantity().stripTrailingZeros().toPlainString()
                            + " for product " + (plan.getProductName() != null ? plan.getProductName() : productId)
                            + " — dispatch cancelled");
        }

        for (FefoAllocationDto.FefoLine line : plan.getLines()) {
            LotStock lot = lotStockRepository
                    .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, line.getLotStockId())
                    .orElseThrow(() -> AppException.notFound("Lot stock not found: " + line.getLotStockId()));

            BigDecimal before = lot.getQuantityOnHand() != null ? lot.getQuantityOnHand() : BigDecimal.ZERO;
            BigDecimal after = before.subtract(line.getAllocatedQuantity());
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                // Another dispatch consumed the lot between planning and committing.
                throw AppException.insufficientStock(
                        "Lot " + lot.getLotNumber() + " no longer holds enough stock — dispatch cancelled");
            }
            lot.setQuantityOnHand(after);
            if (after.compareTo(BigDecimal.ZERO) == 0) lot.setStatus(LotStockStatus.EXHAUSTED);
            lotStockRepository.save(lot);

            LotMovement movement = new LotMovement();
            movement.setTenantId(tenantId);
            movement.setLotNumber(lot.getLotNumber());
            movement.setProductId(lot.getProductId());
            movement.setProductName(lot.getProductName());
            movement.setWarehouseId(lot.getWarehouseId());
            movement.setMovementType(LotMovType.ISSUE);
            movement.setMovementDate(LocalDate.now());
            movement.setQuantity(line.getAllocatedQuantity().negate());
            movement.setBalanceBefore(before);
            movement.setBalanceAfter(after);
            movement.setReferenceType(referenceType);
            movement.setReferenceId(referenceId);
            movement.setReferenceNumber(referenceNumber);
            movement.setNotes("FEFO pick — expiry "
                    + (lot.getExpiryDate() != null ? lot.getExpiryDate() : "none"));
            lotMovementRepository.save(movement);

            log.info("FEFO issued {} from lot {} (expiry {}) for {} {}",
                    line.getAllocatedQuantity(), lot.getLotNumber(), lot.getExpiryDate(),
                    referenceType, referenceNumber);
        }
        return plan;
    }

    /**
     * Best-effort variant for dispatch paths that already deducted aggregate stock. A product with
     * no lots at all is normal — not everything is lot-tracked — so that is skipped quietly; a
     * genuine shortfall on a lot-tracked product still throws.
     */
    @Transactional
    public FefoAllocationDto consumeIfLotTracked(UUID productId, UUID warehouseId, BigDecimal quantity,
                                                 String referenceType, UUID referenceId, String referenceNumber) {
        UUID tenantId = tenantContext.current();
        if (productId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) return null;
        boolean lotTracked = !lotStockRepository
                .findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId).isEmpty();
        if (!lotTracked) return null;
        return consume(productId, warehouseId, quantity, referenceType, referenceId, referenceNumber);
    }

    // ── Expiry reporting ─────────────────────────────────────────────────────

    /** Shelf-life overview across every lot holding stock, bucketed by closeness to expiry. */
    public ExpiryOverviewDto expiryOverview(Integer horizonDays, UUID warehouseId) {
        UUID tenantId = tenantContext.current();
        int horizon = horizonDays != null && horizonDays > 0 ? horizonDays : DEFAULT_HORIZON_DAYS;
        LocalDate today = LocalDate.now();

        ExpiryOverviewDto out = new ExpiryOverviewDto();
        out.setHorizonDays(horizon);
        out.setAsOf(today);

        ExpiryOverviewDto.Bucket expired = bucket("Expired");
        ExpiryOverviewDto.Bucket within30 = bucket("0–30 days");
        ExpiryOverviewDto.Bucket within60 = bucket("31–60 days");
        ExpiryOverviewDto.Bucket withinHorizon = bucket("61–" + horizon + " days");
        ExpiryOverviewDto.Bucket beyond = bucket("Beyond " + horizon + " days");
        ExpiryOverviewDto.Bucket noExpiry = bucket("No expiry set");

        for (LotStock lot : lotStockRepository.findByTenantIdAndDeletedAtIsNull(tenantId)) {
            BigDecimal qty = lot.getQuantityOnHand() != null ? lot.getQuantityOnHand() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (warehouseId != null && !warehouseId.equals(lot.getWarehouseId())) continue;

            BigDecimal value = qty.multiply(lot.getUnitCost() != null ? lot.getUnitCost() : BigDecimal.ZERO);

            if (lot.getExpiryDate() == null) {
                add(noExpiry, qty, value);
                out.setNoExpiryQuantity(out.getNoExpiryQuantity().add(qty));
                out.setNoExpiryLots(out.getNoExpiryLots() + 1);
                continue;
            }

            long days = ChronoUnit.DAYS.between(today, lot.getExpiryDate());
            if (days < 0) {
                add(expired, qty, value);
                out.setExpiredQuantity(out.getExpiredQuantity().add(qty));
                out.setExpiredValue(out.getExpiredValue().add(value));
                out.setExpiredLots(out.getExpiredLots() + 1);
            } else if (days <= 30) {
                add(within30, qty, value);
                countExpiring(out, qty, value);
            } else if (days <= 60) {
                add(within60, qty, value);
                countExpiring(out, qty, value);
            } else if (days <= horizon) {
                add(withinHorizon, qty, value);
                countExpiring(out, qty, value);
            } else {
                add(beyond, qty, value);
                out.setHealthyQuantity(out.getHealthyQuantity().add(qty));
                out.setHealthyLots(out.getHealthyLots() + 1);
            }

            if (days <= horizon) out.getLots().add(toExpiringLot(lot, qty, value, days));
        }

        out.getLots().sort(Comparator.comparing(
                ExpiryOverviewDto.ExpiringLot::getDaysToExpiry,
                Comparator.nullsLast(Comparator.naturalOrder())));
        out.setBuckets(List.of(expired, within30, within60, withinHorizon, beyond, noExpiry));
        return out;
    }

    /**
     * Flags lots whose expiry has passed so FEFO stops offering them and they show as a write-off
     * candidate. Quantities are left untouched — writing the stock off is a stock adjustment and
     * stays a deliberate act.
     */
    @Transactional
    public int markExpiredLots() {
        UUID tenantId = tenantContext.current();
        LocalDate today = LocalDate.now();
        int marked = 0;
        for (LotStock lot : lotStockRepository.findByTenantIdAndDeletedAtIsNull(tenantId)) {
            if (lot.getExpiryDate() == null) continue;
            if (!lot.getExpiryDate().isBefore(today)) continue;
            if (lot.getStatus() == LotStockStatus.EXPIRED || lot.getStatus() == LotStockStatus.EXHAUSTED) continue;
            BigDecimal qty = lot.getQuantityOnHand() != null ? lot.getQuantityOnHand() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            lot.setStatus(LotStockStatus.EXPIRED);
            lotStockRepository.save(lot);
            marked++;
        }
        // Seed stock lives in the other lot table, and dispatch reads that one — sweeping only
        // LotStock would leave expired seed lots looking dispatchable on the screens that matter.
        for (var lot : stockLotRepository.findByTenantIdAndDeletedAtIsNull(
                tenantId, org.springframework.data.domain.Pageable.unpaged())) {
            if (lot.getExpiryDate() == null || !lot.getExpiryDate().isBefore(today)) continue;
            if (Boolean.TRUE.equals(lot.getExpired())) continue;
            BigDecimal qty = lot.getQuantity() != null ? lot.getQuantity() : BigDecimal.ZERO;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            lot.setExpired(Boolean.TRUE);
            stockLotRepository.save(lot);
            marked++;
        }

        if (marked > 0) log.info("FEFO sweep marked {} lot(s) EXPIRED", marked);
        return marked;
    }

    // ── Internals ────────────────────────────────────────────────────────────

    /**
     * Dispatchable lots for a product, ordered soonest-expiry first. Lots with no expiry sort
     * last: an item that never expires should not jump ahead of one that is about to.
     */
    private List<LotStock> dispatchableLots(UUID tenantId, UUID productId, UUID warehouseId,
                                            FefoAllocationDto plan) {
        LocalDate today = LocalDate.now();
        List<LotStock> all = lotStockRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId);

        long quarantined = 0, expired = 0;
        List<LotStock> usable = new ArrayList<>();
        for (LotStock lot : all) {
            if (warehouseId != null && !warehouseId.equals(lot.getWarehouseId())) continue;
            if (freeQuantity(lot).compareTo(BigDecimal.ZERO) <= 0) continue;
            if (lot.getStatus() == LotStockStatus.QUARANTINE) { quarantined++; continue; }
            if (lot.getStatus() == LotStockStatus.EXPIRED) { expired++; continue; }
            if (!DISPATCHABLE.contains(lot.getStatus())) continue;
            // Past its expiry but not yet swept — still must not ship.
            if (lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today)) { expired++; continue; }
            usable.add(lot);
        }

        if (plan != null) {
            if (quarantined > 0) plan.getWarnings().add(quarantined + " lot(s) skipped — quarantined");
            if (expired > 0) plan.getWarnings().add(expired + " lot(s) skipped — past expiry");
        }

        usable.sort(Comparator
                .comparing(LotStock::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(LotStock::getProductionDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(l -> l.getCreatedAt() != null ? l.getCreatedAt() : null,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return usable;
    }

    /** On hand less anything already reserved for another order. */
    private BigDecimal freeQuantity(LotStock lot) {
        BigDecimal onHand = lot.getQuantityOnHand() != null ? lot.getQuantityOnHand() : BigDecimal.ZERO;
        BigDecimal reserved = lot.getQuantityReserved() != null ? lot.getQuantityReserved() : BigDecimal.ZERO;
        return onHand.subtract(reserved).max(BigDecimal.ZERO);
    }

    private FefoAllocationDto.FefoLine toLine(LotStock lot, BigDecimal free, BigDecimal take, LocalDate today) {
        FefoAllocationDto.FefoLine line = new FefoAllocationDto.FefoLine();
        line.setLotStockId(lot.getId());
        line.setLotNumber(lot.getLotNumber());
        line.setWarehouseId(lot.getWarehouseId());
        line.setWarehouseName(lot.getWarehouseName());
        line.setStorageLocationName(lot.getStorageLocationName());
        line.setProductionDate(lot.getProductionDate());
        line.setExpiryDate(lot.getExpiryDate());
        line.setDaysToExpiry(lot.getExpiryDate() != null
                ? ChronoUnit.DAYS.between(today, lot.getExpiryDate()) : null);
        line.setAvailableQuantity(free);
        line.setAllocatedQuantity(take);
        line.setUnitCost(lot.getUnitCost());
        line.setStatus(lot.getStatus() != null ? lot.getStatus().name() : null);
        return line;
    }

    private ExpiryOverviewDto.ExpiringLot toExpiringLot(LotStock lot, BigDecimal qty, BigDecimal value, long days) {
        ExpiryOverviewDto.ExpiringLot e = new ExpiryOverviewDto.ExpiringLot();
        e.setLotStockId(lot.getId());
        e.setLotNumber(lot.getLotNumber());
        e.setProductId(lot.getProductId());
        e.setProductName(lot.getProductName());
        e.setWarehouseName(lot.getWarehouseName());
        e.setExpiryDate(lot.getExpiryDate());
        e.setDaysToExpiry(days);
        e.setQuantityOnHand(qty);
        e.setValue(value);
        e.setStatus(lot.getStatus() != null ? lot.getStatus().name() : null);
        e.setSeverity(days < 0 ? "EXPIRED" : days <= CRITICAL_DAYS ? "CRITICAL" : "WARNING");
        return e;
    }

    private void countExpiring(ExpiryOverviewDto out, BigDecimal qty, BigDecimal value) {
        out.setExpiringQuantity(out.getExpiringQuantity().add(qty));
        out.setExpiringValue(out.getExpiringValue().add(value));
        out.setExpiringLots(out.getExpiringLots() + 1);
    }

    private static ExpiryOverviewDto.Bucket bucket(String label) {
        ExpiryOverviewDto.Bucket b = new ExpiryOverviewDto.Bucket();
        b.setLabel(label);
        return b;
    }

    private static void add(ExpiryOverviewDto.Bucket b, BigDecimal qty, BigDecimal value) {
        b.setLotCount(b.getLotCount() + 1);
        b.setQuantity(b.getQuantity().add(qty));
        b.setValue(b.getValue().add(value));
    }
}
