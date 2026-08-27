package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateCropVarietyTestRequest;
import com.erp.platform.modules.agri.dto.CropVarietyTestDto;
import com.erp.platform.modules.agri.entity.CropVarietyTest;
import com.erp.platform.modules.agri.entity.CropVarietyTestLocationConfig;
import com.erp.platform.modules.agri.entity.CropVarietyTestPropertyStandard;
import com.erp.platform.modules.agri.repository.CropVarietyTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CropVarietyTestService {

    private final CropVarietyTestRepository cropVarietyTestRepository;
    private final TenantContext tenantContext;

    public PageResponse<CropVarietyTestDto> list(Pageable pageable) {
        return PageResponse.of(cropVarietyTestRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public List<CropVarietyTestDto> listAll() {
        return cropVarietyTestRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), org.springframework.data.domain.Pageable.unpaged())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public CropVarietyTestDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        CropVarietyTest entity = cropVarietyTestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("CropVarietyTest not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public CropVarietyTestDto create(CreateCropVarietyTestRequest request) {
        UUID tenantId = tenantContext.current();
        CropVarietyTest entity = new CropVarietyTest();
        entity.setTenantId(tenantId);
        mapFields(entity, request, tenantId);
        return toDto(cropVarietyTestRepository.save(entity));
    }

    @Transactional
    public CropVarietyTestDto update(UUID id, CreateCropVarietyTestRequest request) {
        UUID tenantId = tenantContext.current();
        CropVarietyTest entity = cropVarietyTestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("CropVarietyTest not found: " + id));
        entity.getLocationConfigs().clear();
        entity.getPropertyStandards().clear();
        mapFields(entity, request, tenantId);
        return toDto(cropVarietyTestRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        CropVarietyTest entity = cropVarietyTestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("CropVarietyTest not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        cropVarietyTestRepository.save(entity);
    }

    private void mapFields(CropVarietyTest entity, CreateCropVarietyTestRequest req, UUID tenantId) {
        entity.setCropGroupId(req.getCropGroupId());
        entity.setCropGroupName(req.getCropGroupName());
        entity.setCropId(req.getCropId());
        entity.setCropName(req.getCropName());
        entity.setVarietyId(req.getVarietyId());
        entity.setVarietyName(req.getVarietyName());
        entity.setSeedStateIds(req.getSeedStateIds());
        entity.setTestIds(req.getTestIds());
        entity.setTestNames(req.getTestNames());
        entity.setSampleQuantity(req.getSampleQuantity());
        entity.setSampleQuantityUom(req.getSampleQuantityUom());
        entity.setUpdateInventory(req.isUpdateInventory());
        entity.setMandatory(req.isMandatory());
        entity.setProcessSeedStateIds(req.getProcessSeedStateIds());
        entity.setDefined(req.isDefined());
        entity.setActive(req.isActive());

        if (req.getLocationConfigs() != null) {
            List<CropVarietyTestLocationConfig> locs = req.getLocationConfigs().stream().map(r -> {
                CropVarietyTestLocationConfig lc = new CropVarietyTestLocationConfig();
                lc.setTenantId(tenantId);
                lc.setCropVarietyTest(entity);
                lc.setTestLocationId(r.getTestLocationId());
                lc.setTestLocationName(r.getTestLocationName());
                lc.setTestCost(r.getTestCost());
                lc.setTestDuration(r.getTestDuration());
                return lc;
            }).collect(Collectors.toList());
            entity.getLocationConfigs().addAll(locs);
        }

        if (req.getPropertyStandards() != null) {
            List<CropVarietyTestPropertyStandard> stds = req.getPropertyStandards().stream().map(r -> {
                CropVarietyTestPropertyStandard ps = new CropVarietyTestPropertyStandard();
                ps.setTenantId(tenantId);
                ps.setCropVarietyTest(entity);
                ps.setPropertyName(r.getPropertyName());
                ps.setTestLocationId(r.getTestLocationId());
                ps.setTestLocationName(r.getTestLocationName());
                ps.setMinValue(r.getMinValue());
                ps.setMaxValue(r.getMaxValue());
                return ps;
            }).collect(Collectors.toList());
            entity.getPropertyStandards().addAll(stds);
        }
    }

    private CropVarietyTestDto toDto(CropVarietyTest entity) {
        CropVarietyTestDto dto = new CropVarietyTestDto();
        dto.setId(entity.getId());
        dto.setCropGroupId(entity.getCropGroupId());
        dto.setCropGroupName(entity.getCropGroupName());
        dto.setCropId(entity.getCropId());
        dto.setCropName(entity.getCropName());
        dto.setVarietyId(entity.getVarietyId());
        dto.setVarietyName(entity.getVarietyName());
        dto.setSeedStateIds(entity.getSeedStateIds());
        dto.setTestIds(entity.getTestIds());
        dto.setTestNames(entity.getTestNames());
        dto.setSampleQuantity(entity.getSampleQuantity());
        dto.setSampleQuantityUom(entity.getSampleQuantityUom());
        dto.setUpdateInventory(entity.isUpdateInventory());
        dto.setMandatory(entity.isMandatory());
        dto.setProcessSeedStateIds(entity.getProcessSeedStateIds());
        dto.setDefined(entity.isDefined());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getLocationConfigs() != null) {
            dto.setLocationConfigs(entity.getLocationConfigs().stream().map(lc -> {
                CropVarietyTestDto.LocationConfigDto lcd = new CropVarietyTestDto.LocationConfigDto();
                lcd.setId(lc.getId());
                lcd.setTestLocationId(lc.getTestLocationId());
                lcd.setTestLocationName(lc.getTestLocationName());
                lcd.setTestCost(lc.getTestCost());
                lcd.setTestDuration(lc.getTestDuration());
                return lcd;
            }).collect(Collectors.toList()));
        }

        if (entity.getPropertyStandards() != null) {
            dto.setPropertyStandards(entity.getPropertyStandards().stream().map(ps -> {
                CropVarietyTestDto.PropertyStandardDto psd = new CropVarietyTestDto.PropertyStandardDto();
                psd.setId(ps.getId());
                psd.setPropertyName(ps.getPropertyName());
                psd.setTestLocationId(ps.getTestLocationId());
                psd.setTestLocationName(ps.getTestLocationName());
                psd.setMinValue(ps.getMinValue());
                psd.setMaxValue(ps.getMaxValue());
                return psd;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
