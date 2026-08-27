package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateVarietyReleaseRequest;
import com.erp.platform.modules.agri.dto.VarietyReleaseDto;
import com.erp.platform.modules.agri.entity.VarietyRelease;
import com.erp.platform.modules.agri.repository.VarietyReleaseRepository;
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
public class VarietyReleaseService {

    private final VarietyReleaseRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<VarietyReleaseDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public VarietyReleaseDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public VarietyReleaseDto create(CreateVarietyReleaseRequest request) {
        UUID tenantId = tenantContext.current();
        VarietyRelease entity = new VarietyRelease();
        entity.setTenantId(tenantId);
        entity.setVarietyCode(request.varietyCode());
        entity.setVarietyName(request.varietyName());
        entity.setCropName(request.cropName());
        entity.setReleaseYear(request.releaseYear());
        entity.setNotificationNumber(request.notificationNumber());
        entity.setNotificationDate(request.notificationDate());
        entity.setCertifyingBody(request.certifyingBody());
        entity.setSeedClass(request.seedClass());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("VarietyRelease created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public VarietyReleaseDto update(UUID id, CreateVarietyReleaseRequest request) {
        VarietyRelease entity = findOrThrow(id);
        entity.setVarietyCode(request.varietyCode());
        entity.setVarietyName(request.varietyName());
        entity.setCropName(request.cropName());
        entity.setReleaseYear(request.releaseYear());
        entity.setNotificationNumber(request.notificationNumber());
        entity.setNotificationDate(request.notificationDate());
        entity.setCertifyingBody(request.certifyingBody());
        entity.setSeedClass(request.seedClass());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        VarietyRelease entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private VarietyRelease findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("VarietyRelease not found: " + id));
    }

    private VarietyReleaseDto toDto(VarietyRelease e) {
        return new VarietyReleaseDto(
                e.getId(),
                e.getVarietyCode(),
                e.getVarietyName(),
                e.getCropName(),
                e.getReleaseYear(),
                e.getNotificationNumber(),
                e.getNotificationDate(),
                e.getCertifyingBody(),
                e.getSeedClass(),
                e.getDescription(),
                e.isActive()
        );
    }
}
