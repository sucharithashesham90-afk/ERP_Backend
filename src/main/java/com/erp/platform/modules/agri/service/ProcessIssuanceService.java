package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProcessIssuanceRequest;
import com.erp.platform.modules.agri.dto.ProcessIssuanceDto;
import com.erp.platform.modules.agri.entity.ProcessIssuance;
import com.erp.platform.modules.agri.repository.ProcessIssuanceRepository;
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
public class ProcessIssuanceService {

    private final ProcessIssuanceRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessIssuanceDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProcessIssuanceDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProcessIssuanceDto create(CreateProcessIssuanceRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessIssuance entity = new ProcessIssuance();
        entity.setTenantId(tenantId);
        entity.setProcessNumber(request.processNumber());
        entity.setProcessDate(request.processDate());
        entity.setLotNumber(request.lotNumber());
        entity.setProcessType(request.processType());
        entity.setByProductName(request.byProductName());
        entity.setForwardWeightKgs(request.forwardWeightKgs());
        entity.setExpiredKgs(request.expiredKgs());
        entity.setLostKgs(request.lostKgs());
        entity.setRejectedKgs(request.rejectedKgs());
        entity.setRetainedKgs(request.retainedKgs());
        entity.setLocation(request.location());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity.setRemarks(request.remarks());
        entity = repository.save(entity);
        log.info("ProcessIssuance created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProcessIssuanceDto update(UUID id, CreateProcessIssuanceRequest request) {
        ProcessIssuance entity = findOrThrow(id);
        entity.setProcessNumber(request.processNumber());
        entity.setProcessDate(request.processDate());
        entity.setLotNumber(request.lotNumber());
        entity.setProcessType(request.processType());
        entity.setByProductName(request.byProductName());
        entity.setForwardWeightKgs(request.forwardWeightKgs());
        entity.setExpiredKgs(request.expiredKgs());
        entity.setLostKgs(request.lostKgs());
        entity.setRejectedKgs(request.rejectedKgs());
        entity.setRetainedKgs(request.retainedKgs());
        entity.setLocation(request.location());
        entity.setStatus(request.status());
        entity.setRemarks(request.remarks());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProcessIssuance entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProcessIssuance findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProcessIssuance not found: " + id));
    }

    private ProcessIssuanceDto toDto(ProcessIssuance e) {
        return new ProcessIssuanceDto(
                e.getId(),
                e.getProcessNumber(),
                e.getProcessDate(),
                e.getLotNumber(),
                e.getProcessType(),
                e.getByProductName(),
                e.getForwardWeightKgs(),
                e.getExpiredKgs(),
                e.getLostKgs(),
                e.getRejectedKgs(),
                e.getRetainedKgs(),
                e.getLocation(),
                e.getStatus(),
                e.getRemarks()
        );
    }
}
