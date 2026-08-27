package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateLocationTransferRequest;
import com.erp.platform.modules.inventory.dto.LocationTransferDto;
import com.erp.platform.modules.inventory.entity.LocationStock;
import com.erp.platform.modules.inventory.entity.LocationTransfer;
import com.erp.platform.modules.inventory.entity.LocationTransfer.LTStatus;
import com.erp.platform.modules.inventory.entity.StorageLocation;
import com.erp.platform.modules.inventory.repository.LocationStockRepository;
import com.erp.platform.modules.inventory.repository.LocationTransferRepository;
import com.erp.platform.modules.inventory.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationTransferService {

    private final LocationTransferRepository transferRepo;
    private final StorageLocationRepository locationRepo;
    private final LocationStockRepository stockRepo;
    private final TenantContext tenantContext;

    public PageResponse<LocationTransferDto> list(UUID warehouseId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = warehouseId != null
                ? transferRepo.findByTenantIdAndWarehouseIdAndDeletedAtIsNull(tenantId, warehouseId, pageable)
                : transferRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public LocationTransferDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LocationTransferDto create(CreateLocationTransferRequest request) {
        UUID tenantId = tenantContext.current();

        StorageLocation fromLoc = locationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromLocationId())
                .orElseThrow(() -> AppException.notFound("From location not found: " + request.getFromLocationId()));
        StorageLocation toLoc = locationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToLocationId())
                .orElseThrow(() -> AppException.notFound("To location not found: " + request.getToLocationId()));

        LocationTransfer transfer = new LocationTransfer();
        transfer.setTenantId(tenantId);
        transfer.setTransferNumber(generateNumber(tenantId));
        transfer.setFromLocationId(fromLoc.getId());
        transfer.setFromLocationCode(fromLoc.getCode());
        transfer.setToLocationId(toLoc.getId());
        transfer.setToLocationCode(toLoc.getCode());
        transfer.setWarehouseId(request.getWarehouseId());
        transfer.setProductId(request.getProductId());
        transfer.setProductName(request.getProductName());
        transfer.setLotNumber(request.getLotNumber());
        transfer.setQuantity(request.getQuantity());
        transfer.setTransferDate(request.getTransferDate() != null ? request.getTransferDate() : LocalDate.now());
        transfer.setReason(request.getReason());
        transfer.setPerformedBy(request.getPerformedBy());
        transfer.setStatus(LTStatus.PENDING);

        transfer = transferRepo.save(transfer);
        log.info("LocationTransfer created: {} from {} to {}", transfer.getTransferNumber(), fromLoc.getCode(), toLoc.getCode());
        return toDto(transfer);
    }

    @Transactional
    public LocationTransferDto complete(UUID id) {
        UUID tenantId = tenantContext.current();
        LocationTransfer transfer = findOrThrow(id);
        if (transfer.getStatus() != LTStatus.PENDING) {
            throw AppException.businessRule("Only PENDING transfers can be completed. Current: " + transfer.getStatus());
        }

        // Deduct from source location
        String lot = transfer.getLotNumber() != null ? transfer.getLotNumber() : "";
        stockRepo.findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
                tenantId, transfer.getFromLocationId(), transfer.getProductId(), lot)
                .ifPresent(fromStock -> {
                    BigDecimal newQty = fromStock.getQuantity().subtract(transfer.getQuantity());
                    if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                        throw AppException.businessRule("Insufficient stock at source location");
                    }
                    fromStock.setQuantity(newQty);
                    stockRepo.save(fromStock);
                });

        // Add to destination location
        LocationStock toStock = stockRepo.findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
                tenantId, transfer.getToLocationId(), transfer.getProductId(), lot)
                .orElseGet(() -> {
                    LocationStock s = new LocationStock();
                    s.setTenantId(tenantId);
                    s.setLocationId(transfer.getToLocationId());
                    s.setLocationCode(transfer.getToLocationCode());
                    s.setWarehouseId(transfer.getWarehouseId());
                    s.setProductId(transfer.getProductId());
                    s.setProductName(transfer.getProductName());
                    s.setLotNumber(lot);
                    s.setQuantity(BigDecimal.ZERO);
                    return s;
                });
        toStock.setQuantity(toStock.getQuantity().add(transfer.getQuantity()));
        stockRepo.save(toStock);

        transfer.setStatus(LTStatus.COMPLETED);
        return toDto(transferRepo.save(transfer));
    }

    @Transactional
    public LocationTransferDto update(UUID id, CreateLocationTransferRequest request) {
        UUID tenantId = tenantContext.current();
        LocationTransfer transfer = findOrThrow(id);
        if (transfer.getStatus() != LTStatus.PENDING) {
            throw AppException.businessRule("Only PENDING transfers can be edited. Current: " + transfer.getStatus());
        }
        StorageLocation fromLoc = locationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromLocationId())
                .orElseThrow(() -> AppException.notFound("From location not found: " + request.getFromLocationId()));
        StorageLocation toLoc = locationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToLocationId())
                .orElseThrow(() -> AppException.notFound("To location not found: " + request.getToLocationId()));
        transfer.setFromLocationId(fromLoc.getId());
        transfer.setFromLocationCode(fromLoc.getCode());
        transfer.setToLocationId(toLoc.getId());
        transfer.setToLocationCode(toLoc.getCode());
        transfer.setWarehouseId(request.getWarehouseId());
        transfer.setProductId(request.getProductId());
        transfer.setProductName(request.getProductName());
        transfer.setLotNumber(request.getLotNumber());
        transfer.setQuantity(request.getQuantity());
        if (request.getTransferDate() != null) transfer.setTransferDate(request.getTransferDate());
        transfer.setReason(request.getReason());
        transfer.setPerformedBy(request.getPerformedBy());
        return toDto(transferRepo.save(transfer));
    }

    @Transactional
    public LocationTransferDto cancel(UUID id) {
        LocationTransfer transfer = findOrThrow(id);
        if (transfer.getStatus() == LTStatus.COMPLETED) {
            throw AppException.businessRule("Cannot cancel a completed transfer");
        }
        transfer.setStatus(LTStatus.CANCELLED);
        return toDto(transferRepo.save(transfer));
    }

    private LocationTransferDto toDto(LocationTransfer t) {
        LocationTransferDto dto = new LocationTransferDto();
        dto.setId(t.getId());
        dto.setTenantId(t.getTenantId());
        dto.setTransferNumber(t.getTransferNumber());
        dto.setFromLocationId(t.getFromLocationId());
        dto.setFromLocationCode(t.getFromLocationCode());
        dto.setToLocationId(t.getToLocationId());
        dto.setToLocationCode(t.getToLocationCode());
        dto.setWarehouseId(t.getWarehouseId());
        dto.setProductId(t.getProductId());
        dto.setProductName(t.getProductName());
        dto.setLotNumber(t.getLotNumber());
        dto.setQuantity(t.getQuantity());
        dto.setTransferDate(t.getTransferDate());
        dto.setReason(t.getReason());
        dto.setStatus(t.getStatus());
        dto.setPerformedBy(t.getPerformedBy());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private LocationTransfer findOrThrow(UUID id) {
        return transferRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Location transfer not found: " + id));
    }

    private String generateNumber(UUID tenantId) {
        String prefix = "LT-" + Year.now().getValue() + "-";
        long count = transferRepo.countByTenantIdAndTransferNumberStartingWith(tenantId, prefix) + 1;
        return prefix + String.format("%03d", count);
    }
}
