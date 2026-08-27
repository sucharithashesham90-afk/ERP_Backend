package com.erp.platform.modules.admin.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.CreateLoginTrackingRequest;
import com.erp.platform.modules.admin.dto.LoginTrackingDto;
import com.erp.platform.modules.admin.entity.LoginTracking;
import com.erp.platform.modules.admin.repository.LoginTrackingRepository;
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
public class LoginTrackingService {

    private final LoginTrackingRepository loginTrackingRepository;
    private final TenantContext tenantContext;

    public PageResponse<LoginTrackingDto> list(Pageable pageable) {
        return PageResponse.of(loginTrackingRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public LoginTrackingDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        LoginTracking entity = loginTrackingRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("LoginTracking not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public LoginTrackingDto create(CreateLoginTrackingRequest request) {
        UUID tenantId = tenantContext.current();
        LoginTracking entity = new LoginTracking();
        entity.setTenantId(tenantId);
        entity.setUserId(request.getUserId());
        entity.setUsername(request.getUsername());
        entity.setLoginTime(request.getLoginTime());
        entity.setLogoutTime(request.getLogoutTime());
        entity.setIpAddress(request.getIpAddress());
        entity.setSessionDurationMinutes(request.getSessionDurationMinutes());
        entity.setStatus(request.getStatus());
        return toDto(loginTrackingRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        LoginTracking entity = loginTrackingRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("LoginTracking not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        loginTrackingRepository.save(entity);
    }

    private LoginTrackingDto toDto(LoginTracking entity) {
        LoginTrackingDto dto = new LoginTrackingDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setLoginTime(entity.getLoginTime());
        dto.setLogoutTime(entity.getLogoutTime());
        dto.setIpAddress(entity.getIpAddress());
        dto.setSessionDurationMinutes(entity.getSessionDurationMinutes());
        dto.setStatus(entity.getStatus());
        dto.setForceLoggedOff(entity.isForceLoggedOff());
        dto.setForceLogoffBy(entity.getForceLogoffBy());
        dto.setForceLogoffAt(entity.getForceLogoffAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    @Transactional
    public LoginTrackingDto forceLogoff(UUID id, String adminUsername) {
        UUID tenantId = tenantContext.current();
        LoginTracking entity = loginTrackingRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        entity.setForceLoggedOff(true);
        entity.setForceLogoffBy(adminUsername);
        entity.setForceLogoffAt(LocalDateTime.now());
        entity.setStatus("FORCE_LOGOFF");
        entity.setLogoutTime(LocalDateTime.now());
        return toDto(loginTrackingRepository.save(entity));
    }
}

