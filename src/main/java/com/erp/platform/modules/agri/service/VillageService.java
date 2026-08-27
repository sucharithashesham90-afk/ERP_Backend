package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateVillageRequest;
import com.erp.platform.modules.agri.dto.VillageDto;
import com.erp.platform.modules.agri.entity.Village;
import com.erp.platform.modules.agri.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VillageService {

    private final VillageRepository villageRepository;
    private final TenantContext tenantContext;

    public PageResponse<VillageDto> list(Pageable pageable) {
        return PageResponse.of(villageRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public VillageDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        Village entity = villageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Village not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public VillageDto create(CreateVillageRequest request) {
        Village entity = new Village();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        return toDto(villageRepository.save(entity));
    }

    @Transactional
    public VillageDto update(UUID id, CreateVillageRequest request) {
        UUID tenantId = tenantContext.current();
        Village entity = villageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Village not found: " + id));
        applyRequest(entity, request);
        return toDto(villageRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        Village entity = villageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Village not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        villageRepository.save(entity);
    }

    private void applyRequest(Village e, CreateVillageRequest r) {
        e.setName(r.getName());
        e.setVillageCode(r.getVillageCode());
        e.setStateId(r.getStateId());
        e.setStateName(r.getStateName());
        e.setDistrictId(r.getDistrictId());
        e.setDistrictName(r.getDistrictName());
        e.setMandalId(r.getMandalId());
        e.setMandalName(r.getMandalName());
        e.setZip(r.getZip());
        e.setProductionAreaId(r.getProductionAreaId());
        e.setProductionAreaName(r.getProductionAreaName());
        e.setInchargeIds(r.getInchargeIds());
        e.setInchargeNames(r.getInchargeNames());
        e.setTelegraphOffice(r.getTelegraphOffice());
        e.setNearestRailwayStn(r.getNearestRailwayStn());
        e.setNearestPostOffice(r.getNearestPostOffice());
        e.setNearestTown(r.getNearestTown());
        e.setActive(r.isActive());
    }

    private VillageDto toDto(Village e) {
        VillageDto dto = new VillageDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setVillageCode(e.getVillageCode());
        dto.setStateId(e.getStateId());
        dto.setStateName(e.getStateName());
        dto.setDistrictId(e.getDistrictId());
        dto.setDistrictName(e.getDistrictName());
        dto.setMandalId(e.getMandalId());
        dto.setMandalName(e.getMandalName());
        dto.setZip(e.getZip());
        dto.setProductionAreaId(e.getProductionAreaId());
        dto.setProductionAreaName(e.getProductionAreaName());
        dto.setInchargeIds(e.getInchargeIds());
        dto.setInchargeNames(e.getInchargeNames());
        dto.setTelegraphOffice(e.getTelegraphOffice());
        dto.setNearestRailwayStn(e.getNearestRailwayStn());
        dto.setNearestPostOffice(e.getNearestPostOffice());
        dto.setNearestTown(e.getNearestTown());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}

