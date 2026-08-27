package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateLocationRequest;
import com.erp.platform.modules.inventory.dto.LocationDto;
import com.erp.platform.modules.inventory.entity.Location;
import com.erp.platform.modules.inventory.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final TenantContext tenantContext;

    public PageResponse<LocationDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(locationRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toDto));
    }

    public List<LocationDto> listActive() {
        UUID tenantId = tenantContext.current();
        return locationRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullOrderByName(tenantId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public LocationDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LocationDto create(CreateLocationRequest request) {
        UUID tenantId = tenantContext.current();
        if (locationRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, request.getName())) {
            throw AppException.badRequest("Location name already exists: " + request.getName());
        }
        Location location = new Location();
        location.setTenantId(tenantId);
        location.setName(request.getName().trim());
        location.setCode(request.getCode());
        location.setDescription(request.getDescription());
        location.setActive(request.isActive());
        location = locationRepository.save(location);
        log.info("Location created: id={}, name={}", location.getId(), location.getName());
        return toDto(location);
    }

    @Transactional
    public LocationDto update(UUID id, CreateLocationRequest request) {
        UUID tenantId = tenantContext.current();
        Location location = findOrThrow(id);
        if (locationRepository.existsByTenantIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                tenantId, request.getName(), id)) {
            throw AppException.badRequest("Location name already exists: " + request.getName());
        }
        location.setName(request.getName().trim());
        location.setCode(request.getCode());
        location.setDescription(request.getDescription());
        location.setActive(request.isActive());
        location = locationRepository.save(location);
        log.info("Location updated: id={}", id);
        return toDto(location);
    }

    @Transactional
    public void delete(UUID id) {
        Location location = findOrThrow(id);
        location.setDeletedAt(LocalDateTime.now());
        locationRepository.save(location);
        log.info("Location soft-deleted: id={}", id);
    }

    private Location findOrThrow(UUID id) {
        return locationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Location not found: " + id));
    }

    private LocationDto toDto(Location l) {
        LocationDto dto = new LocationDto();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setName(l.getName());
        dto.setCode(l.getCode());
        dto.setDescription(l.getDescription());
        dto.setActive(l.isActive());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }
}
