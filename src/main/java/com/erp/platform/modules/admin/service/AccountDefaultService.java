package com.erp.platform.modules.admin.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.AccountDefaultDto;
import com.erp.platform.modules.admin.dto.CreateAccountDefaultRequest;
import com.erp.platform.modules.admin.entity.AccountDefault;
import com.erp.platform.modules.admin.repository.AccountDefaultRepository;
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
public class AccountDefaultService {

    private final AccountDefaultRepository accountDefaultRepository;
    private final TenantContext tenantContext;

    public PageResponse<AccountDefaultDto> list(Pageable pageable) {
        return PageResponse.of(accountDefaultRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public AccountDefaultDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        AccountDefault entity = accountDefaultRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AccountDefault not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public AccountDefaultDto create(CreateAccountDefaultRequest request) {
        UUID tenantId = tenantContext.current();
        AccountDefault entity = new AccountDefault();
        entity.setTenantId(tenantId);
        entity.setDefaultKey(request.getDefaultKey());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setPeriodType(request.getPeriodType());
        entity.setActive(request.isActive());
        return toDto(accountDefaultRepository.save(entity));
    }

    @Transactional
    public AccountDefaultDto update(UUID id, CreateAccountDefaultRequest request) {
        UUID tenantId = tenantContext.current();
        AccountDefault entity = accountDefaultRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AccountDefault not found: " + id));
        entity.setDefaultKey(request.getDefaultKey());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setPeriodType(request.getPeriodType());
        entity.setActive(request.isActive());
        return toDto(accountDefaultRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        AccountDefault entity = accountDefaultRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AccountDefault not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        accountDefaultRepository.save(entity);
    }

    private AccountDefaultDto toDto(AccountDefault entity) {
        AccountDefaultDto dto = new AccountDefaultDto();
        dto.setId(entity.getId());
        dto.setDefaultKey(entity.getDefaultKey());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setPeriodType(entity.getPeriodType());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

