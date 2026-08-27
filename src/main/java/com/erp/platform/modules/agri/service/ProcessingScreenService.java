package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProcessingScreenRequest;
import com.erp.platform.modules.agri.dto.ProcessingScreenDto;
import com.erp.platform.modules.agri.entity.ProcessingScreen;
import com.erp.platform.modules.agri.repository.ProcessingScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProcessingScreenService {

    private final ProcessingScreenRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessingScreenDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProcessingScreenDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProcessingScreenDto create(CreateProcessingScreenRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessingScreen entity = new ProcessingScreen();
        entity.setTenantId(tenantId);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setMeshSize(request.meshSize());
        entity.setScreenType(request.screenType());
        entity.setMaterial(request.material());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("ProcessingScreen created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProcessingScreenDto update(UUID id, CreateProcessingScreenRequest request) {
        ProcessingScreen entity = findOrThrow(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setMeshSize(request.meshSize());
        entity.setScreenType(request.screenType());
        entity.setMaterial(request.material());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProcessingScreen entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProcessingScreen findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProcessingScreen not found: " + id));
    }

    private ProcessingScreenDto toDto(ProcessingScreen e) {
        return new ProcessingScreenDto(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getMeshSize(),
                e.getScreenType(),
                e.getMaterial(),
                e.getDescription(),
                e.isActive()
        );
    }
}
