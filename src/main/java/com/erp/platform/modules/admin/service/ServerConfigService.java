package com.erp.platform.modules.admin.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.CreateServerConfigRequest;
import com.erp.platform.modules.admin.dto.ServerConfigDto;
import com.erp.platform.modules.admin.entity.ServerConfig;
import com.erp.platform.modules.admin.repository.ServerConfigRepository;
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
public class ServerConfigService {

    private final ServerConfigRepository serverConfigRepository;
    private final TenantContext tenantContext;

    public PageResponse<ServerConfigDto> list(Pageable pageable) {
        return PageResponse.of(serverConfigRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ServerConfigDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ServerConfig entity = serverConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ServerConfig not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ServerConfigDto create(CreateServerConfigRequest request) {
        UUID tenantId = tenantContext.current();
        ServerConfig entity = new ServerConfig();
        entity.setTenantId(tenantId);
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setActive(request.isActive());
        return toDto(serverConfigRepository.save(entity));
    }

    @Transactional
    public ServerConfigDto update(UUID id, CreateServerConfigRequest request) {
        UUID tenantId = tenantContext.current();
        ServerConfig entity = serverConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ServerConfig not found: " + id));
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setActive(request.isActive());
        return toDto(serverConfigRepository.save(entity));
    }

    /** All config values under a category as a key→value map (e.g. SMTP or SMS settings). */
    public java.util.Map<String, String> getByCategory(String category) {
        return serverConfigRepository.findByTenantIdAndCategoryAndDeletedAtIsNull(tenantContext.current(), category)
                .stream().collect(Collectors.toMap(ServerConfig::getConfigKey,
                        c -> c.getConfigValue() != null ? c.getConfigValue() : "", (a, b) -> b));
    }

    /** Upsert a whole category of config values (create missing keys, update existing). */
    @Transactional
    public java.util.Map<String, String> saveByCategory(String category, java.util.Map<String, String> values) {
        UUID tenantId = tenantContext.current();
        values.forEach((key, value) -> {
            ServerConfig entity = serverConfigRepository
                    .findByTenantIdAndCategoryAndConfigKeyAndDeletedAtIsNull(tenantId, category, key)
                    .orElseGet(() -> {
                        ServerConfig e = new ServerConfig();
                        e.setTenantId(tenantId);
                        e.setCategory(category);
                        e.setConfigKey(key);
                        e.setActive(true);
                        return e;
                    });
            entity.setConfigValue(value);
            serverConfigRepository.save(entity);
        });
        return getByCategory(category);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ServerConfig entity = serverConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ServerConfig not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        serverConfigRepository.save(entity);
    }

    private ServerConfigDto toDto(ServerConfig entity) {
        ServerConfigDto dto = new ServerConfigDto();
        dto.setId(entity.getId());
        dto.setConfigKey(entity.getConfigKey());
        dto.setConfigValue(entity.getConfigValue());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

