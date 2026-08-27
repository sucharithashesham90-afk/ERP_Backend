package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.GodownDto;
import com.erp.platform.modules.inventory.entity.Godown;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GodownService {

    private final GodownRepository godownRepository;
    private final TenantContext tenantContext;

    public PageResponse<GodownDto> list(String location, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = StringUtils.hasText(location)
                ? godownRepository.findByTenantIdAndLocationAndDeletedAtIsNull(tenantId, location, pageable)
                : godownRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    @Transactional
    public GodownDto create(GodownDto req) {
        UUID tenantId = tenantContext.current();
        validate(req);
        Godown g = new Godown();
        g.setTenantId(tenantId);
        apply(g, req);
        g = godownRepository.save(g);
        log.info("Godown created: id={}, name={}", g.getId(), g.getName());
        return toDto(g);
    }

    @Transactional
    public GodownDto update(UUID id, GodownDto req) {
        UUID tenantId = tenantContext.current();
        validate(req);
        Godown g = godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Godown not found: " + id));
        apply(g, req);
        g = godownRepository.save(g);
        log.info("Godown updated: id={}", id);
        return toDto(g);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        Godown g = godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Godown not found: " + id));
        g.setDeletedAt(java.time.LocalDateTime.now());
        godownRepository.save(g);
        log.info("Godown deleted: id={}", id);
    }

    private void validate(GodownDto req) {
        if (!StringUtils.hasText(req.getName())) throw AppException.badRequest("Godown name is required");
        if (!StringUtils.hasText(req.getLocation())) throw AppException.badRequest("Location is required");
    }

    private void apply(Godown g, GodownDto req) {
        g.setName(req.getName().trim());
        g.setLocation(req.getLocation());
        g.setMachineAvailability(req.isMachineAvailability());
        g.setGroupsCsv(req.getGroups() == null ? null : String.join(",", req.getGroups()));
        g.setArea(req.getArea());
        g.setColdStorage(req.isColdStorage());
        g.setPackingMaterialStorage(req.isPackingMaterialStorage());
        g.setOwnership(StringUtils.hasText(req.getOwnership()) ? req.getOwnership() : "OWN");
        g.setStorageCapacity(req.getStorageCapacity());
        g.setStorageCapacityUom(req.getStorageCapacityUom());
        // active defaults true; only override when caller explicitly manages it
        g.setActive(req.getId() == null ? true : req.isActive());
    }

    private GodownDto toDto(Godown g) {
        GodownDto d = new GodownDto();
        d.setId(g.getId());
        d.setName(g.getName());
        d.setLocation(g.getLocation());
        d.setMachineAvailability(g.isMachineAvailability());
        d.setGroups(parseGroups(g.getGroupsCsv()));
        d.setArea(g.getArea());
        d.setColdStorage(g.isColdStorage());
        d.setPackingMaterialStorage(g.isPackingMaterialStorage());
        d.setOwnership(g.getOwnership());
        d.setStorageCapacity(g.getStorageCapacity());
        d.setStorageCapacityUom(g.getStorageCapacityUom());
        d.setActive(g.isActive());
        d.setCreatedAt(g.getCreatedAt());
        return d;
    }

    private List<String> parseGroups(String csv) {
        if (!StringUtils.hasText(csv)) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
