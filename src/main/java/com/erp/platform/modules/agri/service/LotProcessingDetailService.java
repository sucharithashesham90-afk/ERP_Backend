package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateLotProcessingDetailRequest;
import com.erp.platform.modules.agri.dto.LotProcessingDetailDto;
import com.erp.platform.modules.agri.entity.LotProcessingDetail;
import com.erp.platform.modules.agri.repository.LotProcessingDetailRepository;
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
public class LotProcessingDetailService {

    private final LotProcessingDetailRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<LotProcessingDetailDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public LotProcessingDetailDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LotProcessingDetailDto create(CreateLotProcessingDetailRequest request) {
        UUID tenantId = tenantContext.current();
        LotProcessingDetail entity = new LotProcessingDetail();
        entity.setTenantId(tenantId);
        entity.setJobNumber(request.jobNumber());
        entity.setProcessingDate(request.processingDate());
        entity.setLotNumber(request.lotNumber());
        entity.setLocation(request.location());
        entity.setInputLotNumber(request.inputLotNumber());
        entity.setOutputLotNumber(request.outputLotNumber());
        entity.setInputQuantityKgs(request.inputQuantityKgs());
        entity.setOutputQuantityKgs(request.outputQuantityKgs());
        entity.setProcessType(request.processType());
        entity.setMachineName(request.machineName());
        entity.setOperatorName(request.operatorName());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity = repository.save(entity);
        log.info("LotProcessingDetail created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public LotProcessingDetailDto update(UUID id, CreateLotProcessingDetailRequest request) {
        LotProcessingDetail entity = findOrThrow(id);
        entity.setJobNumber(request.jobNumber());
        entity.setProcessingDate(request.processingDate());
        entity.setLotNumber(request.lotNumber());
        entity.setLocation(request.location());
        entity.setInputLotNumber(request.inputLotNumber());
        entity.setOutputLotNumber(request.outputLotNumber());
        entity.setInputQuantityKgs(request.inputQuantityKgs());
        entity.setOutputQuantityKgs(request.outputQuantityKgs());
        entity.setProcessType(request.processType());
        entity.setMachineName(request.machineName());
        entity.setOperatorName(request.operatorName());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        LotProcessingDetail entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private LotProcessingDetail findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("LotProcessingDetail not found: " + id));
    }

    private LotProcessingDetailDto toDto(LotProcessingDetail e) {
        return new LotProcessingDetailDto(
                e.getId(),
                e.getJobNumber(),
                e.getProcessingDate(),
                e.getLotNumber(),
                e.getLocation(),
                e.getInputLotNumber(),
                e.getOutputLotNumber(),
                e.getInputQuantityKgs(),
                e.getOutputQuantityKgs(),
                e.getProcessType(),
                e.getMachineName(),
                e.getOperatorName(),
                e.getStatus()
        );
    }
}
