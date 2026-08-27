package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateLotStatusRecordRequest;
import com.erp.platform.modules.agri.dto.LotStatusRecordDto;
import com.erp.platform.modules.agri.entity.LotStatusRecord;
import com.erp.platform.modules.agri.repository.LotStatusRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LotStatusRecordService {

    private final LotStatusRecordRepository lotStatusRecordRepository;
    private final TenantContext tenantContext;

    public PageResponse<LotStatusRecordDto> list(Pageable pageable) {
        return PageResponse.of(lotStatusRecordRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public LotStatusRecordDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        LotStatusRecord entity = lotStatusRecordRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("LotStatusRecord not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public LotStatusRecordDto create(CreateLotStatusRecordRequest request) {
        UUID tenantId = tenantContext.current();
        LotStatusRecord entity = new LotStatusRecord();
        entity.setTenantId(tenantId);
        entity.setLotNumber(request.getLotNumber());
        entity.setRegistrationNumber(request.getRegistrationNumber());
        entity.setCropName(request.getCropName());
        entity.setVarietyName(request.getVarietyName());
        entity.setFieldProducerName(request.getFieldProducerName());
        entity.setCurrentStatus(request.getCurrentStatus());
        entity.setQualityStatus(request.getQualityStatus());
        entity.setInventoryKgs(request.getInventoryKgs());
        entity.setProcessedKgs(request.getProcessedKgs());
        entity.setRemarks(request.getRemarks());
        entity.setStatusDate(request.getStatusDate());
        return toDto(lotStatusRecordRepository.save(entity));
    }

    @Transactional
    public LotStatusRecordDto update(UUID id, CreateLotStatusRecordRequest request) {
        UUID tenantId = tenantContext.current();
        LotStatusRecord entity = lotStatusRecordRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("LotStatusRecord not found: " + id));
        entity.setLotNumber(request.getLotNumber());
        entity.setRegistrationNumber(request.getRegistrationNumber());
        entity.setCropName(request.getCropName());
        entity.setVarietyName(request.getVarietyName());
        entity.setFieldProducerName(request.getFieldProducerName());
        entity.setCurrentStatus(request.getCurrentStatus());
        entity.setQualityStatus(request.getQualityStatus());
        entity.setInventoryKgs(request.getInventoryKgs());
        entity.setProcessedKgs(request.getProcessedKgs());
        entity.setRemarks(request.getRemarks());
        entity.setStatusDate(request.getStatusDate());
        return toDto(lotStatusRecordRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        LotStatusRecord entity = lotStatusRecordRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("LotStatusRecord not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        lotStatusRecordRepository.save(entity);
    }

    private LotStatusRecordDto toDto(LotStatusRecord entity) {
        LotStatusRecordDto dto = new LotStatusRecordDto();
        dto.setId(entity.getId());
        dto.setLotNumber(entity.getLotNumber());
        dto.setRegistrationNumber(entity.getRegistrationNumber());
        dto.setCropName(entity.getCropName());
        dto.setVarietyName(entity.getVarietyName());
        dto.setFieldProducerName(entity.getFieldProducerName());
        dto.setCurrentStatus(entity.getCurrentStatus());
        dto.setQualityStatus(entity.getQualityStatus());
        dto.setInventoryKgs(entity.getInventoryKgs());
        dto.setProcessedKgs(entity.getProcessedKgs());
        dto.setRemarks(entity.getRemarks());
        dto.setStatusDate(entity.getStatusDate());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

