package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateMaterialTransferNoteRequest;
import com.erp.platform.modules.inventory.dto.MaterialTransferNoteDto;
import com.erp.platform.modules.inventory.entity.MaterialTransferNote;
import com.erp.platform.modules.inventory.repository.MaterialTransferNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialTransferNoteService {

    private final MaterialTransferNoteRepository materialTransferNoteRepository;
    private final TenantContext tenantContext;

    public PageResponse<MaterialTransferNoteDto> list(Pageable pageable) {
        return PageResponse.of(materialTransferNoteRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public MaterialTransferNoteDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        MaterialTransferNote entity = materialTransferNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("MaterialTransferNote not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public MaterialTransferNoteDto create(CreateMaterialTransferNoteRequest request) {
        UUID tenantId = tenantContext.current();
        MaterialTransferNote entity = new MaterialTransferNote();
        entity.setTenantId(tenantId);
        entity.setMtnNumber(request.getMtnNumber());
        entity.setMtnDate(request.getMtnDate());
        entity.setFromLocation(request.getFromLocation());
        entity.setToLocation(request.getToLocation());
        entity.setPurpose(request.getPurpose());
        entity.setProductId(request.getProductId());
        entity.setProductName(request.getProductName());
        entity.setQuantityKgs(request.getQuantityKgs());
        entity.setStatus(request.getStatus());
        entity.setApprovedBy(request.getApprovedBy());
        entity.setRemarks(request.getRemarks());
        return toDto(materialTransferNoteRepository.save(entity));
    }

    @Transactional
    public MaterialTransferNoteDto update(UUID id, CreateMaterialTransferNoteRequest request) {
        UUID tenantId = tenantContext.current();
        MaterialTransferNote entity = materialTransferNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("MaterialTransferNote not found: " + id));
        entity.setMtnNumber(request.getMtnNumber());
        entity.setMtnDate(request.getMtnDate());
        entity.setFromLocation(request.getFromLocation());
        entity.setToLocation(request.getToLocation());
        entity.setPurpose(request.getPurpose());
        entity.setProductId(request.getProductId());
        entity.setProductName(request.getProductName());
        entity.setQuantityKgs(request.getQuantityKgs());
        entity.setStatus(request.getStatus());
        entity.setApprovedBy(request.getApprovedBy());
        entity.setRemarks(request.getRemarks());
        return toDto(materialTransferNoteRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        MaterialTransferNote entity = materialTransferNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("MaterialTransferNote not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        materialTransferNoteRepository.save(entity);
    }

    private MaterialTransferNoteDto toDto(MaterialTransferNote entity) {
        MaterialTransferNoteDto dto = new MaterialTransferNoteDto();
        dto.setId(entity.getId());
        dto.setMtnNumber(entity.getMtnNumber());
        dto.setMtnDate(entity.getMtnDate());
        dto.setFromLocation(entity.getFromLocation());
        dto.setToLocation(entity.getToLocation());
        dto.setPurpose(entity.getPurpose());
        dto.setProductName(entity.getProductName());
        dto.setQuantityKgs(entity.getQuantityKgs());
        dto.setStatus(entity.getStatus());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

