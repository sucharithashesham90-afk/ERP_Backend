package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateTreatmentRequest;
import com.erp.platform.modules.agri.dto.TreatmentDto;
import com.erp.platform.modules.agri.entity.Treatment;
import com.erp.platform.modules.agri.repository.TreatmentRepository;
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
public class TreatmentService {

    private final TreatmentRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<TreatmentDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public TreatmentDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public TreatmentDto create(CreateTreatmentRequest request) {
        UUID tenantId = tenantContext.current();
        Treatment entity = new Treatment();
        entity.setTenantId(tenantId);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setChemicalName(request.chemicalName());
        entity.setTreatmentType(request.treatmentType());
        entity.setDosagePerKg(request.dosagePerKg());
        entity.setUnit(request.unit());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("Treatment created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public TreatmentDto update(UUID id, CreateTreatmentRequest request) {
        Treatment entity = findOrThrow(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setChemicalName(request.chemicalName());
        entity.setTreatmentType(request.treatmentType());
        entity.setDosagePerKg(request.dosagePerKg());
        entity.setUnit(request.unit());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        Treatment entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private Treatment findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Treatment not found: " + id));
    }

    private TreatmentDto toDto(Treatment e) {
        return new TreatmentDto(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getChemicalName(),
                e.getTreatmentType(),
                e.getDosagePerKg(),
                e.getUnit(),
                e.getDescription(),
                e.isActive()
        );
    }
}
