package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateIntakeQualityRecordRequest;
import com.erp.platform.modules.agri.dto.IntakeQualityRecordDto;
import com.erp.platform.modules.agri.entity.IntakeQualityRecord;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
import com.erp.platform.modules.agri.repository.IntakeQualityRecordRepository;
import com.erp.platform.modules.agri.repository.PlantVariantRepository;
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
public class IntakeQualityRecordService {

    private final IntakeQualityRecordRepository repository;
    private final PlantVariantRepository variantRepository;
    private final FieldProducerRepository producerRepository;
    private final TenantContext tenantContext;

    public PageResponse<IntakeQualityRecordDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<IntakeQualityRecord> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<IntakeQualityRecordDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<IntakeQualityRecordDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public IntakeQualityRecordDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public IntakeQualityRecordDto create(CreateIntakeQualityRecordRequest req) {
        UUID tenantId = tenantContext.current();
        IntakeQualityRecord entity = new IntakeQualityRecord();
        entity.setTenantId(tenantId);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public IntakeQualityRecordDto update(UUID id, CreateIntakeQualityRecordRequest req) {
        UUID tenantId = tenantContext.current();
        IntakeQualityRecord entity = findOrThrow(id);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        IntakeQualityRecord entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(IntakeQualityRecord e, CreateIntakeQualityRecordRequest req, UUID tenantId) {
        e.setIntakeSlipId(req.getIntakeSlipId());
        e.setIntakeSlipNumber(req.getIntakeSlipNumber());
        e.setRecordDate(req.getRecordDate());
        e.setGrossWeight(req.getGrossWeight());
        e.setMoistureDeductionPercent(req.getMoistureDeductionPercent());
        e.setTrashDeductionPercent(req.getTrashDeductionPercent());
        e.setNetWeight(req.getNetWeight());
        e.setInitialViabilityRate(req.getInitialViabilityRate());
        e.setInitialQualityGrade(req.getInitialQualityGrade());
        e.setQualityStatus(req.getQualityStatus());
        e.setRemarks(req.getRemarks());
        if (req.getPlantVariantId() != null) {
            variantRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantVariantId())
                    .ifPresent(e::setPlantVariant);
        }
        if (req.getFieldProducerId() != null) {
            producerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFieldProducerId())
                    .ifPresent(e::setFieldProducer);
        }
    }

    private IntakeQualityRecord findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Intake quality record not found: " + id));
    }

    private IntakeQualityRecordDto toDto(IntakeQualityRecord e) {
        IntakeQualityRecordDto dto = new IntakeQualityRecordDto();
        dto.setId(e.getId());
        dto.setIntakeSlipId(e.getIntakeSlipId());
        dto.setIntakeSlipNumber(e.getIntakeSlipNumber());
        dto.setRecordDate(e.getRecordDate());
        dto.setGrossWeight(e.getGrossWeight());
        dto.setMoistureDeductionPercent(e.getMoistureDeductionPercent());
        dto.setTrashDeductionPercent(e.getTrashDeductionPercent());
        dto.setNetWeight(e.getNetWeight());
        dto.setInitialViabilityRate(e.getInitialViabilityRate());
        dto.setInitialQualityGrade(e.getInitialQualityGrade());
        dto.setQualityStatus(e.getQualityStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getPlantVariant() != null) {
            dto.setPlantVariantId(e.getPlantVariant().getId());
            dto.setPlantVariantName(e.getPlantVariant().getName());
        }
        if (e.getFieldProducer() != null) {
            dto.setFieldProducerId(e.getFieldProducer().getId());
            dto.setFieldProducerName(e.getFieldProducer().getName());
        }
        return dto;
    }
}
