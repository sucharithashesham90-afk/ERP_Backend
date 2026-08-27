package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateStockTransferRequest;
import com.erp.platform.modules.inventory.dto.StockTransferDto;
import com.erp.platform.modules.inventory.entity.StockTransfer;
import com.erp.platform.modules.inventory.entity.StockTransferItem;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.StockTransferRepository;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final TenantContext tenantContext;

    public PageResponse<StockTransferDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(stockTransferRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    @Transactional
    public StockTransferDto create(CreateStockTransferRequest request) {
        UUID tenantId = tenantContext.current();

        Warehouse from = warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromWarehouseId())
                .orElseThrow(() -> AppException.notFound("Source warehouse not found"));
        Warehouse to = warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToWarehouseId())
                .orElseThrow(() -> AppException.notFound("Destination warehouse not found"));

        if (from.getId().equals(to.getId())) {
            throw AppException.badRequest("Source and destination warehouse must be different");
        }
        validateSameLocation(from, to);

        StockTransfer transfer = new StockTransfer();
        transfer.setTenantId(tenantId);
        transfer.setTransferNumber(generateNumber());
        transfer.setFromWarehouseId(from.getId());
        transfer.setFromWarehouseName(from.getName());
        transfer.setToWarehouseId(to.getId());
        transfer.setToWarehouseName(to.getName());
        transfer.setTransferDate(request.getTransferDate() != null ? request.getTransferDate() : LocalDate.now());
        transfer.setNotes(request.getNotes());
        transfer.setStatus("DRAFT");

        if (request.getItems() != null) {
            List<StockTransferItem> items = new ArrayList<>();
            for (CreateStockTransferRequest.TransferItem r : request.getItems()) {
                if (r.getProductId() == null) continue;
                StockTransferItem item = new StockTransferItem();
                item.setStockTransfer(transfer);
                item.setProductId(r.getProductId());
                item.setQuantity(r.getQuantity() != null ? BigDecimal.valueOf(r.getQuantity()) : BigDecimal.ONE);
                items.add(item);
            }
            transfer.setItems(items);
        }

        transfer = stockTransferRepository.save(transfer);
        log.info("Stock transfer created: id={}, number={}", transfer.getId(), transfer.getTransferNumber());
        return toDto(transfer);
    }

    @Transactional
    public StockTransferDto update(UUID id, CreateStockTransferRequest request) {
        UUID tenantId = tenantContext.current();
        StockTransfer transfer = stockTransferRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        if (!"DRAFT".equals(transfer.getStatus())) {
            throw AppException.badRequest("Only DRAFT stock transfers can be edited");
        }
        Warehouse from = warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromWarehouseId())
                .orElseThrow(() -> AppException.notFound("Source warehouse not found"));
        Warehouse to = warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToWarehouseId())
                .orElseThrow(() -> AppException.notFound("Destination warehouse not found"));
        if (from.getId().equals(to.getId())) {
            throw AppException.badRequest("Source and destination warehouse must be different");
        }
        validateSameLocation(from, to);
        transfer.setFromWarehouseId(from.getId());
        transfer.setFromWarehouseName(from.getName());
        transfer.setToWarehouseId(to.getId());
        transfer.setToWarehouseName(to.getName());
        if (request.getTransferDate() != null) transfer.setTransferDate(request.getTransferDate());
        transfer.setNotes(request.getNotes());
        if (request.getItems() != null) {
            transfer.getItems().clear();
            for (CreateStockTransferRequest.TransferItem r : request.getItems()) {
                if (r.getProductId() == null) continue;
                StockTransferItem item = new StockTransferItem();
                item.setStockTransfer(transfer);
                item.setProductId(r.getProductId());
                item.setQuantity(r.getQuantity() != null ? BigDecimal.valueOf(r.getQuantity()) : BigDecimal.ONE);
                transfer.getItems().add(item);
            }
        }
        transfer = stockTransferRepository.save(transfer);
        log.info("Stock transfer updated: id={}", id);
        return toDto(transfer);
    }

    @Transactional
    public StockTransferDto confirm(UUID id) {
        UUID tenantId = tenantContext.current();
        StockTransfer transfer = stockTransferRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        if (!"DRAFT".equals(transfer.getStatus())) {
            throw AppException.badRequest("Only DRAFT transfers can be confirmed");
        }

        List<StockTransferItem> items = transfer.getItems();
        if (items != null) {
            for (StockTransferItem item : items) {
                if (item.getProductId() == null) continue;
                BigDecimal qty = item.getQuantity();
                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) continue;
                stockService.deductStock(item.getProductId(), transfer.getFromWarehouseId(), qty,
                        "STOCK_TRANSFER", transfer.getId(), transfer.getTransferNumber(),
                        "RAW", "GOOD", null, "TRANSFER");
                stockService.addStock(item.getProductId(), transfer.getToWarehouseId(), qty,
                        "STOCK_TRANSFER", transfer.getId(), transfer.getTransferNumber(), item.getProductName(),
                        "RAW", "GOOD", null, "TRANSFER");
            }
        }

        transfer.setStatus("COMPLETED");
        transfer = stockTransferRepository.save(transfer);
        log.info("Stock transfer confirmed: id={}, number={}", transfer.getId(), transfer.getTransferNumber());
        return toDto(transfer);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        StockTransfer transfer = stockTransferRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        transfer.setDeletedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
    }

    private StockTransferDto toDto(StockTransfer t) {
        StockTransferDto dto = new StockTransferDto();
        dto.setId(t.getId());
        dto.setTenantId(t.getTenantId());
        dto.setTransferNumber(t.getTransferNumber());
        dto.setFromWarehouseId(t.getFromWarehouseId());
        dto.setFromWarehouseName(t.getFromWarehouseName());
        dto.setToWarehouseId(t.getToWarehouseId());
        dto.setToWarehouseName(t.getToWarehouseName());
        dto.setTransferDate(t.getTransferDate());
        dto.setStatus(t.getStatus());
        dto.setNotes(t.getNotes());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private void validateSameLocation(Warehouse from, Warehouse to) {
        String fromLoc = from.getLocation();
        String toLoc = to.getLocation();
        if (fromLoc != null && !fromLoc.isBlank() && toLoc != null && !toLoc.isBlank()
                && !fromLoc.equalsIgnoreCase(toLoc)) {
            throw AppException.badRequest(
                    "Cross-location stock transfer not allowed: '" + fromLoc + "' → '" + toLoc + "'");
        }
    }

    private String generateNumber() {
        return "ST-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
