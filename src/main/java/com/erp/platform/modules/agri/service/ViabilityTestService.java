package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateViabilityTestRequest;
import com.erp.platform.modules.agri.dto.ViabilityTestDto;
import com.erp.platform.modules.agri.entity.ViabilityTest;
import com.erp.platform.modules.agri.repository.PlantVariantRepository;
import com.erp.platform.modules.agri.repository.ViabilityTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ViabilityTestService {

    private final ViabilityTestRepository repository;
    private final PlantVariantRepository variantRepository;
    private final TenantContext tenantContext;

    public PageResponse<ViabilityTestDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ViabilityTest> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ViabilityTestDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ViabilityTestDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ViabilityTestDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ViabilityTestDto create(CreateViabilityTestRequest req) {
        UUID tenantId = tenantContext.current();
        ViabilityTest entity = new ViabilityTest();
        entity.setTenantId(tenantId);
        applyRequest(entity, req, tenantId);
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public ViabilityTestDto update(UUID id, CreateViabilityTestRequest req) {
        UUID tenantId = tenantContext.current();
        ViabilityTest entity = findOrThrow(id);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ViabilityTest entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(ViabilityTest e, CreateViabilityTestRequest req, UUID tenantId) {
        e.setTestNumber(req.getTestNumber());
        e.setLotNumber(req.getLotNumber());
        e.setTestDate(req.getTestDate());
        e.setResultDate(req.getResultDate());
        e.setTestLab(req.getTestLab());
        e.setTestedBy(req.getTestedBy());
        e.setViabilityRate(req.getViabilityRate());
        e.setQualityGrade(req.getQualityGrade());
        e.setMoistureContent(req.getMoistureContent());
        e.setInertMatter(req.getInertMatter());
        e.setOtherSeeds(req.getOtherSeeds());
        e.setTestResult(req.getTestResult());
        e.setStatus(req.getStatus() != null ? req.getStatus() : "PENDING");
        e.setSampleSize(req.getSampleSize());
        e.setRemarks(req.getRemarks());
        if (req.getPlantVariantId() != null) {
            variantRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantVariantId())
                    .ifPresent(e::setPlantVariant);
        }
    }

    private ViabilityTest findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Viability test not found: " + id));
    }

    private ViabilityTestDto toDto(ViabilityTest e) {
        ViabilityTestDto dto = new ViabilityTestDto();
        dto.setId(e.getId());
        dto.setTestNumber(e.getTestNumber());
        dto.setLotNumber(e.getLotNumber());
        dto.setTestDate(e.getTestDate());
        dto.setResultDate(e.getResultDate());
        dto.setTestLab(e.getTestLab());
        dto.setTestedBy(e.getTestedBy());
        dto.setViabilityRate(e.getViabilityRate());
        dto.setQualityGrade(e.getQualityGrade());
        dto.setMoistureContent(e.getMoistureContent());
        dto.setInertMatter(e.getInertMatter());
        dto.setOtherSeeds(e.getOtherSeeds());
        dto.setTestResult(e.getTestResult());
        dto.setStatus(e.getStatus());
        dto.setSampleSize(e.getSampleSize());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getPlantVariant() != null) {
            dto.setPlantVariantId(e.getPlantVariant().getId());
            dto.setPlantVariantName(e.getPlantVariant().getName());
        }
        return dto;
    }
}
