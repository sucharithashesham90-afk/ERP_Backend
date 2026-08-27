package com.erp.platform.modules.sales.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Lot-based stock movements for sales flows (inventory is always tracked per lot). Sales returns
 * receive stock back into a lot; issues (dispatch, dealer transfers) reduce it.
 */
@Service
@RequiredArgsConstructor
public class SalesStockService {

    private final StockLotRepository stockLotRepository;

    /** Add {@code qty} back to the lot; creates the lot if it doesn't exist. */
    public void receiveToLot(UUID tenantId, String lotNo, BigDecimal qty, String productName, String source) {
        if (lotNo == null || lotNo.isBlank() || qty == null || qty.signum() <= 0) return;
        StockLot lot = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNo)
                .stream().findFirst().orElseGet(StockLot::new);
        if (lot.getId() == null) {
            lot.setTenantId(tenantId);
            lot.setLotNo(lotNo);
            lot.setProductName(productName);
            lot.setQuantity(BigDecimal.ZERO);
            lot.setUnit("KG");
            lot.setSource(source);
        }
        BigDecimal current = lot.getQuantity() == null ? BigDecimal.ZERO : lot.getQuantity();
        lot.setQuantity(current.add(qty));
        stockLotRepository.save(lot);
    }

    /** Reduce the lot by {@code qty}; errors if the lot is missing or has insufficient stock. */
    public void issueFromLot(UUID tenantId, String lotNo, BigDecimal qty) {
        if (lotNo == null || lotNo.isBlank() || qty == null || qty.signum() <= 0) return;
        List<StockLot> lots = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNo);
        if (lots.isEmpty())
            throw AppException.badRequest("No stock lot found for lot number: " + lotNo);
        StockLot lot = lots.stream()
                .filter(l -> l.getQuantity() != null && l.getQuantity().compareTo(qty) >= 0)
                .findFirst().orElse(lots.get(0));
        BigDecimal available = lot.getQuantity() == null ? BigDecimal.ZERO : lot.getQuantity();
        if (available.compareTo(qty) < 0)
            throw AppException.badRequest("Insufficient stock in lot " + lotNo
                    + " (available " + available + ", required " + qty + ")");
        lot.setQuantity(available.subtract(qty));
        stockLotRepository.save(lot);
    }
}
