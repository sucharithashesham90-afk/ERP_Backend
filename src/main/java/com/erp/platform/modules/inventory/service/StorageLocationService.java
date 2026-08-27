package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateStorageLocationRequest;
import com.erp.platform.modules.inventory.dto.LocationStockDto;
import com.erp.platform.modules.inventory.dto.StorageLocationDto;
import com.erp.platform.modules.inventory.entity.StorageLocation;
import com.erp.platform.modules.inventory.entity.StorageLocation.LocationType;
import com.erp.platform.modules.inventory.repository.LocationStockRepository;
import com.erp.platform.modules.inventory.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StorageLocationService {

    private final StorageLocationRepository locationRepo;
    private final LocationStockRepository stockRepo;
    private final TenantContext tenantContext;

    public PageResponse<StorageLocationDto> list(UUID warehouseId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = warehouseId != null
                ? locationRepo.findByTenantIdAndWarehouseIdAndDeletedAtIsNull(tenantId, warehouseId, pageable)
                : locationRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public StorageLocationDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public PageResponse<LocationStockDto> getLocationStock(UUID locationId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(stockRepo.findByTenantIdAndLocationIdAndDeletedAtIsNull(tenantId, locationId, pageable)
                .map(s -> {
                    LocationStockDto dto = new LocationStockDto();
                    dto.setId(s.getId());
                    dto.setTenantId(s.getTenantId());
                    dto.setLocationId(s.getLocationId());
                    dto.setLocationCode(s.getLocationCode());
                    dto.setWarehouseId(s.getWarehouseId());
                    dto.setProductId(s.getProductId());
                    dto.setProductName(s.getProductName());
                    dto.setLotNumber(s.getLotNumber());
                    dto.setQuantity(s.getQuantity());
                    dto.setReservedQuantity(s.getReservedQuantity());
                    BigDecimal avail = s.getQuantity().subtract(
                            s.getReservedQuantity() != null ? s.getReservedQuantity() : BigDecimal.ZERO);
                    dto.setAvailableQuantity(avail.max(BigDecimal.ZERO));
                    dto.setUnitCost(s.getUnitCost());
                    dto.setCreatedAt(s.getCreatedAt());
                    dto.setUpdatedAt(s.getUpdatedAt());
                    return dto;
                }));
    }

    @Transactional
    public StorageLocationDto create(CreateStorageLocationRequest request) {
        if (request.getWarehouseId() == null) {
            throw AppException.badRequest("Warehouse is required for a storage location");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw AppException.badRequest("Location name is required");
        }
        UUID tenantId = tenantContext.current();
        StorageLocation loc = new StorageLocation();
        loc.setTenantId(tenantId);
        mapRequest(loc, request);
        if (loc.getCode() == null || loc.getCode().isBlank()) {
            loc.setCode(buildCode(request.getAisle(), request.getRack(), request.getBin()));
        }
        loc = locationRepo.save(loc);
        log.info("StorageLocation created: {} in warehouse {}", loc.getCode(), loc.getWarehouseId());
        return toDto(loc);
    }

    @Transactional
    public StorageLocationDto update(UUID id, CreateStorageLocationRequest request) {
        StorageLocation loc = findOrThrow(id);
        mapRequest(loc, request);
        return toDto(locationRepo.save(loc));
    }

    @Transactional
    public void delete(UUID id) {
        StorageLocation loc = findOrThrow(id);
        loc.setDeletedAt(LocalDateTime.now());
        locationRepo.save(loc);
    }

    private void mapRequest(StorageLocation loc, CreateStorageLocationRequest r) {
        if (r.getWarehouseId() != null) loc.setWarehouseId(r.getWarehouseId());
        if (r.getWarehouseName() != null) loc.setWarehouseName(r.getWarehouseName());
        if (r.getCode() != null && !r.getCode().isBlank()) loc.setCode(r.getCode());
        if (r.getName() != null) loc.setName(r.getName());
        if (r.getLocationType() != null) loc.setLocationType(r.getLocationType());
        loc.setAisle(r.getAisle());
        loc.setRack(r.getRack());
        loc.setBin(r.getBin());
        loc.setCapacity(r.getCapacity());
        if (r.getCapacityUnit() != null) loc.setCapacityUnit(r.getCapacityUnit());
        if (r.getActive() != null) loc.setActive(r.getActive());
        if (r.getIsDefault() != null) loc.setDefault(r.getIsDefault());
        loc.setNotes(r.getNotes());
    }

    private String buildCode(String aisle, String rack, String bin) {
        StringBuilder sb = new StringBuilder();
        if (aisle != null && !aisle.isBlank()) sb.append(aisle.toUpperCase());
        if (rack != null && !rack.isBlank()) sb.append("-").append(rack.toUpperCase());
        if (bin != null && !bin.isBlank()) sb.append("-").append(bin.toUpperCase());
        return sb.length() > 0 ? sb.toString() : UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private StorageLocationDto toDto(StorageLocation loc) {
        StorageLocationDto dto = new StorageLocationDto();
        dto.setId(loc.getId());
        dto.setTenantId(loc.getTenantId());
        dto.setWarehouseId(loc.getWarehouseId());
        dto.setWarehouseName(loc.getWarehouseName());
        dto.setCode(loc.getCode());
        dto.setName(loc.getName());
        dto.setLocationType(loc.getLocationType());
        dto.setAisle(loc.getAisle());
        dto.setRack(loc.getRack());
        dto.setBin(loc.getBin());
        dto.setCapacity(loc.getCapacity());
        dto.setCapacityUnit(loc.getCapacityUnit());
        dto.setCurrentOccupancy(loc.getCurrentOccupancy());
        dto.setActive(loc.isActive());
        dto.setDefault(loc.isDefault());
        dto.setNotes(loc.getNotes());
        dto.setCreatedAt(loc.getCreatedAt());
        return dto;
    }

    private StorageLocation findOrThrow(UUID id) {
        return locationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Storage location not found: " + id));
    }
}
