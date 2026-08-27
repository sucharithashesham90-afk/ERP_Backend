package com.erp.platform.modules.sales.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.sales.entity.LocationStockTransfer;
import com.erp.platform.modules.sales.repository.LocationStockTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Posts a location-to-location stock transfer: reduces the lot at the from-location and lands the
 * same quantity in a destination lot tagged with the to-location. Pure inventory movement, lot-based;
 * total company stock is unchanged.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationTransferPostingService {

    private final LocationStockTransferRepository repository;
    private final SalesStockService salesStockService;
    private final StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;

    @Transactional
    public LocationStockTransfer post(UUID id) {
        UUID tenantId = tenantContext.current();
        LocationStockTransfer t = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        if (t.isPosted())
            throw AppException.badRequest("Transfer is already posted");
        // Move each line's lot from the source to the destination location; single-line fallback.
        if (t.getItems() != null && !t.getItems().isEmpty()) {
            boolean moved = false;
            for (var l : t.getItems()) {
                BigDecimal qty = l.getPacks();
                if (l.getLotNumber() == null || l.getLotNumber().isBlank() || qty == null || qty.signum() <= 0) continue;
                moveLot(tenantId, l.getLotNumber(), l.getProductName(), t.getToLocation(), qty);
                moved = true;
            }
            if (!moved) throw AppException.badRequest("At least one line with a lot and quantity is required");
        } else {
            if (t.getLotNumber() == null || t.getLotNumber().isBlank())
                throw AppException.badRequest("Lot number is required to move stock");
            BigDecimal qty = t.getQuantity();
            if (qty == null || qty.signum() <= 0)
                throw AppException.badRequest("Quantity must be greater than zero");
            moveLot(tenantId, t.getLotNumber(), t.getProductName(), t.getToLocation(), qty);
        }

        t.setPosted(true);
        t.setStatus("POSTED");
        log.info("Location transfer {} posted: {} line(s) moved from {} to {}",
                t.getTransferNumber(), (t.getItems() == null ? 0 : t.getItems().size()),
                t.getFromLocation(), t.getToLocation());
        return repository.save(t);
    }

    /** Issue a quantity from a lot at the source and land it in a destination lot tagged with the to-location. */
    private void moveLot(UUID tenantId, String lotNo, String productName, String toLocation, BigDecimal qty) {
        salesStockService.issueFromLot(tenantId, lotNo, qty);
        StockLot dest = new StockLot();
        dest.setTenantId(tenantId);
        dest.setLotNo(lotNo);
        dest.setProductName(productName);
        dest.setGodownName(toLocation);   // destination location tag
        dest.setQuantity(qty);
        dest.setUnit("KG");
        dest.setSource("LOCATION_TRANSFER");
        stockLotRepository.save(dest);
    }
}
