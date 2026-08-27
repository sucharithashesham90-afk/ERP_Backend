package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProcessLineEfficiencyRequest;
import com.erp.platform.modules.agri.dto.ProcessLineEfficiencyDto;
import com.erp.platform.modules.agri.entity.ProcessLineEfficiency;
import com.erp.platform.modules.agri.repository.ProcessLineEfficiencyRepository;
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
public class ProcessLineEfficiencyService {

    private final ProcessLineEfficiencyRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessLineEfficiencyDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProcessLineEfficiencyDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProcessLineEfficiencyDto create(CreateProcessLineEfficiencyRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessLineEfficiency entity = new ProcessLineEfficiency();
        entity.setTenantId(tenantId);
        entity.setRecordDate(request.recordDate());
        entity.setLocation(request.location());
        entity.setGodown(request.godown());
        entity.setProcessingLineName(request.processingLineName());
        entity.setProcessType(request.processType());
        entity.setPlannedOutputKgs(request.plannedOutputKgs());
        entity.setActualOutputKgs(request.actualOutputKgs());
        entity.setEfficiencyPercent(request.efficiencyPercent());
        entity.setRemarks(request.remarks());
        entity = repository.save(entity);
        log.info("ProcessLineEfficiency created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProcessLineEfficiencyDto update(UUID id, CreateProcessLineEfficiencyRequest request) {
        ProcessLineEfficiency entity = findOrThrow(id);
        entity.setRecordDate(request.recordDate());
        entity.setLocation(request.location());
        entity.setGodown(request.godown());
        entity.setProcessingLineName(request.processingLineName());
        entity.setProcessType(request.processType());
        entity.setPlannedOutputKgs(request.plannedOutputKgs());
        entity.setActualOutputKgs(request.actualOutputKgs());
        entity.setEfficiencyPercent(request.efficiencyPercent());
        entity.setRemarks(request.remarks());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProcessLineEfficiency entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProcessLineEfficiency findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProcessLineEfficiency not found: " + id));
    }

    private ProcessLineEfficiencyDto toDto(ProcessLineEfficiency e) {
        return new ProcessLineEfficiencyDto(
                e.getId(),
                e.getRecordDate(),
                e.getLocation(),
                e.getGodown(),
                e.getProcessingLineName(),
                e.getProcessType(),
                e.getPlannedOutputKgs(),
                e.getActualOutputKgs(),
                e.getEfficiencyPercent(),
                e.getRemarks()
        );
    }
}
