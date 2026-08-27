package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePossibleDeductionRequest;
import com.erp.platform.modules.agri.dto.PossibleDeductionDto;
import com.erp.platform.modules.agri.entity.PossibleDeduction;
import com.erp.platform.modules.agri.repository.PossibleDeductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PossibleDeductionService {

    private final PossibleDeductionRepository possibleDeductionRepository;
    private final TenantContext tenantContext;

    public PageResponse<PossibleDeductionDto> list(Pageable pageable) {
        return PageResponse.of(possibleDeductionRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public PossibleDeductionDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        PossibleDeduction entity = possibleDeductionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PossibleDeduction not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public PossibleDeductionDto create(CreatePossibleDeductionRequest request) {
        PossibleDeduction entity = new PossibleDeduction();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        return toDto(possibleDeductionRepository.save(entity));
    }

    @Transactional
    public PossibleDeductionDto update(UUID id, CreatePossibleDeductionRequest request) {
        UUID tenantId = tenantContext.current();
        PossibleDeduction entity = possibleDeductionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PossibleDeduction not found: " + id));
        applyRequest(entity, request);
        return toDto(possibleDeductionRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        PossibleDeduction entity = possibleDeductionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PossibleDeduction not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        possibleDeductionRepository.save(entity);
    }

    private void applyRequest(PossibleDeduction e, CreatePossibleDeductionRequest r) {
        e.setName(r.getName());
        e.setDescription(r.getDescription());
        e.setType(r.getType());
        e.setUnits(r.getUnits());
        e.setActive(r.isActive());
    }

    private PossibleDeductionDto toDto(PossibleDeduction e) {
        PossibleDeductionDto dto = new PossibleDeductionDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setType(e.getType());
        dto.setUnits(e.getUnits());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
