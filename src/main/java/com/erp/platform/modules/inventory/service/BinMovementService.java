package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.LocationStock;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.LocationStockRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.inventory.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bin-level stock movement for the warehouse scanner: put away, transfer, pick.
 *
 * <p>The existing stock services work at warehouse granularity, which is the right level for
 * valuation and for the ledger but too coarse for someone standing in front of a rack. These three
 * operations move a specific lot between specific bins, which is what a scanner does.
 *
 * <p>Every one of them refuses to mix varieties in a bin. In seed, two varieties in one location is
 * not an inconvenience to tidy up later — it is a contamination event that can invalidate both
 * lots, and it is nearly impossible to unpick after the fact because nobody can say afterwards
 * which bag came from which lot. Catching it at the moment of the scan is the only cheap time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BinMovementService {

    private final LocationStockRepository locationStockRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;

    /** What a scanned lot is and where it currently sits — the first screen of every scan. */
    public Map<String, Object> lookupLot(String lotNumber) {
        UUID tenantId = tenantContext.current();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lotNumber", lotNumber);

        List<StockLot> lots = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber);
        if (!lots.isEmpty()) {
            StockLot lot = lots.get(0);
            out.put("found", true);
            out.put("productId", lot.getProductId());
            out.put("productName", lot.getProductName());
            out.put("cropName", lot.getCropName());
            out.put("varietyName", lot.getVarietyName());
            out.put("quantity", lot.getQuantity());
            out.put("unit", lot.getUnit());
            out.put("godownName", lot.getGodownName());
            out.put("materialState", lot.getMaterialState());
            out.put("expiryDate", lot.getExpiryDate());
            out.put("expired", Boolean.TRUE.equals(lot.getExpired()));
        } else {
            out.put("found", false);
        }

        List<Map<String, Object>> bins = locationStockRepository
                .findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, lotNumber)
                .stream()
                .filter(ls -> ls.getQuantity() != null && ls.getQuantity().signum() > 0)
                .map(ls -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("locationId", ls.getLocationId());
                    m.put("locationCode", ls.getLocationCode());
                    m.put("quantity", ls.getQuantity());
                    return m;
                }).toList();
        out.put("bins", bins);
        return out;
    }

    /** What is in a bin right now — scan the bin label, see its contents. */
    public Map<String, Object> lookupBin(UUID locationId) {
        UUID tenantId = tenantContext.current();
        var loc = storageLocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, locationId)
                .orElseThrow(() -> AppException.notFound("Storage location not found"));

        List<Map<String, Object>> contents = locationStockRepository
                .findByTenantIdAndLocationIdAndDeletedAtIsNull(tenantId, locationId)
                .stream()
                .filter(ls -> ls.getQuantity() != null && ls.getQuantity().signum() > 0)
                .map(ls -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", ls.getProductId());
                    m.put("productName", ls.getProductName());
                    m.put("lotNumber", ls.getLotNumber());
                    m.put("quantity", ls.getQuantity());
                    m.put("varietyName", varietyOf(tenantId, ls.getLotNumber()));
                    return m;
                }).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("locationId", loc.getId());
        out.put("locationCode", loc.getCode());
        out.put("contents", contents);
        return out;
    }

    /** Put a scanned lot into a bin. */
    @Transactional
    public Map<String, Object> putAway(String lotNumber, UUID locationId, BigDecimal quantity, boolean override) {
        UUID tenantId = tenantContext.current();
        require(quantity);
        guardAgainstMixing(tenantId, locationId, lotNumber, override);

        LocationStock target = upsert(tenantId, locationId, lotNumber);
        target.setQuantity(nz(target.getQuantity()).add(quantity));
        locationStockRepository.save(target);

        log.info("Put away {} of lot {} into bin {}", quantity, lotNumber, locationId);
        return result("PUT_AWAY", lotNumber, quantity, null, locationId);
    }

    /** Move a lot from one bin to another. */
    @Transactional
    public Map<String, Object> transfer(String lotNumber, UUID fromLocationId, UUID toLocationId,
                                        BigDecimal quantity, boolean override) {
        UUID tenantId = tenantContext.current();
        require(quantity);
        if (fromLocationId.equals(toLocationId)) {
            throw AppException.badRequest("Source and destination bins are the same");
        }
        guardAgainstMixing(tenantId, toLocationId, lotNumber, override);

        LocationStock from = locationStockRepository
                .findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
                        tenantId, fromLocationId, productOf(tenantId, lotNumber), lotNumber)
                .orElseThrow(() -> AppException.badRequest("Lot " + lotNumber + " is not in that bin"));
        if (nz(from.getQuantity()).compareTo(quantity) < 0) {
            throw AppException.insufficientStock(
                    "Bin holds only " + nz(from.getQuantity()).stripTrailingZeros().toPlainString()
                            + " of lot " + lotNumber);
        }
        from.setQuantity(nz(from.getQuantity()).subtract(quantity));
        locationStockRepository.save(from);

        LocationStock to = upsert(tenantId, toLocationId, lotNumber);
        to.setQuantity(nz(to.getQuantity()).add(quantity));
        locationStockRepository.save(to);

        log.info("Transferred {} of lot {} from bin {} to {}", quantity, lotNumber, fromLocationId, toLocationId);
        return result("TRANSFER", lotNumber, quantity, fromLocationId, toLocationId);
    }

    /** Take stock out of a bin — for a dispatch, a production issue, a sample. */
    @Transactional
    public Map<String, Object> pick(String lotNumber, UUID locationId, BigDecimal quantity) {
        UUID tenantId = tenantContext.current();
        require(quantity);

        LocationStock from = locationStockRepository
                .findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
                        tenantId, locationId, productOf(tenantId, lotNumber), lotNumber)
                .orElseThrow(() -> AppException.badRequest("Lot " + lotNumber + " is not in that bin"));
        if (nz(from.getQuantity()).compareTo(quantity) < 0) {
            throw AppException.insufficientStock(
                    "Bin holds only " + nz(from.getQuantity()).stripTrailingZeros().toPlainString()
                            + " of lot " + lotNumber);
        }
        from.setQuantity(nz(from.getQuantity()).subtract(quantity));
        locationStockRepository.save(from);

        log.info("Picked {} of lot {} from bin {}", quantity, lotNumber, locationId);
        return result("PICK", lotNumber, quantity, locationId, null);
    }

    // ── Internals ────────────────────────────────────────────────────────────

    /**
     * Refuse to put a lot into a bin that already holds a different variety.
     *
     * <p>Overridable, because a supervisor may genuinely need to — a mixed staging bin before
     * grading, say. But it has to be a decision somebody takes and the log records, not something
     * that happens because a scanner was pointed at the wrong rack.
     */
    private void guardAgainstMixing(UUID tenantId, UUID locationId, String lotNumber, boolean override) {
        String incoming = varietyOf(tenantId, lotNumber);
        if (incoming == null || incoming.isBlank()) return;   // nothing to compare against

        for (LocationStock existing : locationStockRepository
                .findByTenantIdAndLocationIdAndDeletedAtIsNull(tenantId, locationId)) {
            if (existing.getQuantity() == null || existing.getQuantity().signum() <= 0) continue;
            if (lotNumber.equals(existing.getLotNumber())) continue;

            String held = varietyOf(tenantId, existing.getLotNumber());
            if (held != null && !held.isBlank() && !held.equalsIgnoreCase(incoming)) {
                if (override) {
                    log.warn("Variety mix overridden in bin {}: {} joining {} (lot {} joining {})",
                            locationId, incoming, held, lotNumber, existing.getLotNumber());
                    return;
                }
                throw AppException.badRequest(
                        "That bin already holds " + held + " (lot " + existing.getLotNumber()
                                + "). Putting " + incoming + " in with it would mix varieties.");
            }
        }
    }

    private String varietyOf(UUID tenantId, String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) return null;
        return stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber)
                .stream().findFirst().map(StockLot::getVarietyName).orElse(null);
    }

    private UUID productOf(UUID tenantId, String lotNumber) {
        return stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber)
                .stream().findFirst().map(StockLot::getProductId).orElse(null);
    }

    private LocationStock upsert(UUID tenantId, UUID locationId, String lotNumber) {
        UUID productId = productOf(tenantId, lotNumber);
        return locationStockRepository
                .findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
                        tenantId, locationId, productId, lotNumber)
                .orElseGet(() -> {
                    LocationStock ls = new LocationStock();
                    ls.setTenantId(tenantId);
                    ls.setLocationId(locationId);
                    ls.setLotNumber(lotNumber);
                    ls.setProductId(productId);
                    storageLocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, locationId)
                            .ifPresent(loc -> {
                                ls.setLocationCode(loc.getCode());
                                ls.setWarehouseId(loc.getWarehouseId());
                            });
                    stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber)
                            .stream().findFirst()
                            .ifPresent(lot -> ls.setProductName(lot.getProductName()));
                    ls.setQuantity(BigDecimal.ZERO);
                    return ls;
                });
    }

    private static void require(BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) throw AppException.badRequest("Quantity must be greater than zero");
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static Map<String, Object> result(String action, String lotNumber, BigDecimal qty,
                                              UUID from, UUID to) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action);
        m.put("lotNumber", lotNumber);
        m.put("quantity", qty);
        m.put("fromLocationId", from);
        m.put("toLocationId", to);
        return m;
    }
}
