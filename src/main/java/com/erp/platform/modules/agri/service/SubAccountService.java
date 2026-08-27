package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSubAccountRequest;
import com.erp.platform.modules.agri.dto.SubAccountDto;
import com.erp.platform.modules.agri.entity.SubAccount;
import com.erp.platform.modules.agri.repository.SubAccountRepository;
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
public class SubAccountService {

    private final SubAccountRepository subAccountRepository;
    private final TenantContext tenantContext;

    public PageResponse<SubAccountDto> list(Pageable pageable) {
        return PageResponse.of(subAccountRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SubAccountDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SubAccount entity = subAccountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SubAccount not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SubAccountDto create(CreateSubAccountRequest request) {
        UUID tenantId = tenantContext.current();
        SubAccount entity = new SubAccount();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setSubAccountType(request.getSubAccountType());
        entity.setParentAccountCode(request.getParentAccountCode());
        entity.setDescription(request.getDescription());
        entity.setPlantVariantId(request.getPlantVariantId());
        entity.setPlantVariantName(request.getPlantVariantName());
        entity.setActive(request.isActive());
        return toDto(subAccountRepository.save(entity));
    }

    @Transactional
    public SubAccountDto update(UUID id, CreateSubAccountRequest request) {
        UUID tenantId = tenantContext.current();
        SubAccount entity = subAccountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SubAccount not found: " + id));
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setSubAccountType(request.getSubAccountType());
        entity.setParentAccountCode(request.getParentAccountCode());
        entity.setDescription(request.getDescription());
        entity.setPlantVariantId(request.getPlantVariantId());
        entity.setPlantVariantName(request.getPlantVariantName());
        entity.setActive(request.isActive());
        return toDto(subAccountRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SubAccount entity = subAccountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SubAccount not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        subAccountRepository.save(entity);
    }

    private SubAccountDto toDto(SubAccount entity) {
        SubAccountDto dto = new SubAccountDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setSubAccountType(entity.getSubAccountType());
        dto.setParentAccountCode(entity.getParentAccountCode());
        dto.setDescription(entity.getDescription());
        dto.setPlantVariantId(entity.getPlantVariantId());
        dto.setPlantVariantName(entity.getPlantVariantName());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

