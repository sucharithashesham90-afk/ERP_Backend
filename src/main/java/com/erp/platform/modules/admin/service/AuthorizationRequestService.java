package com.erp.platform.modules.admin.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.AuthorizationRequestDto;
import com.erp.platform.modules.admin.dto.CreateAuthorizationRequestRequest;
import com.erp.platform.modules.admin.entity.AuthorizationRequest;
import com.erp.platform.modules.admin.repository.AuthorizationRequestRepository;
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
public class AuthorizationRequestService {

    private final AuthorizationRequestRepository authorizationRequestRepository;
    private final TenantContext tenantContext;

    public PageResponse<AuthorizationRequestDto> list(Pageable pageable) {
        return PageResponse.of(authorizationRequestRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public AuthorizationRequestDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        AuthorizationRequest entity = authorizationRequestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AuthorizationRequest not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public AuthorizationRequestDto create(CreateAuthorizationRequestRequest request) {
        UUID tenantId = tenantContext.current();
        AuthorizationRequest entity = new AuthorizationRequest();
        entity.setTenantId(tenantId);
        entity.setRequestedBy(request.getRequestedBy());
        entity.setRequestedFor(request.getRequestedFor());
        entity.setModule(request.getModule());
        entity.setAction(request.getAction());
        entity.setJustification(request.getJustification());
        entity.setStatus(request.getStatus());
        entity.setReviewedBy(request.getReviewedBy());
        entity.setReviewedAt(request.getReviewedAt());
        entity.setReviewComments(request.getReviewComments());
        return toDto(authorizationRequestRepository.save(entity));
    }

    @Transactional
    public AuthorizationRequestDto update(UUID id, CreateAuthorizationRequestRequest request) {
        UUID tenantId = tenantContext.current();
        AuthorizationRequest entity = authorizationRequestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AuthorizationRequest not found: " + id));
        entity.setRequestedBy(request.getRequestedBy());
        entity.setRequestedFor(request.getRequestedFor());
        entity.setModule(request.getModule());
        entity.setAction(request.getAction());
        entity.setJustification(request.getJustification());
        entity.setStatus(request.getStatus());
        entity.setReviewedBy(request.getReviewedBy());
        entity.setReviewedAt(request.getReviewedAt());
        entity.setReviewComments(request.getReviewComments());
        return toDto(authorizationRequestRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        AuthorizationRequest entity = authorizationRequestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("AuthorizationRequest not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        authorizationRequestRepository.save(entity);
    }

    private AuthorizationRequestDto toDto(AuthorizationRequest entity) {
        AuthorizationRequestDto dto = new AuthorizationRequestDto();
        dto.setId(entity.getId());
        dto.setRequestedBy(entity.getRequestedBy());
        dto.setRequestedFor(entity.getRequestedFor());
        dto.setModule(entity.getModule());
        dto.setAction(entity.getAction());
        dto.setJustification(entity.getJustification());
        dto.setStatus(entity.getStatus());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setReviewComments(entity.getReviewComments());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

