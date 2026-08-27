package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.CreateQualityTestResultRequest;
import com.erp.platform.modules.quality.dto.QualityTestResultDto;
import com.erp.platform.modules.quality.entity.QualityTestResult;
import com.erp.platform.modules.quality.repository.QualityTestResultRepository;
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
public class QualityTestResultService {

    private final QualityTestResultRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<QualityTestResultDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PageResponse<QualityTestResultDto> search(String lot, String product,
            java.time.LocalDate from, java.time.LocalDate to, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.search(tenantId,
                lot == null ? "" : lot.trim(), product == null ? "" : product.trim(),
                from, to, pageable).map(this::toDto));
    }

    public QualityTestResultDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public QualityTestResultDto create(CreateQualityTestResultRequest request) {
        UUID tenantId = tenantContext.current();
        QualityTestResult entity = new QualityTestResult();
        entity.setTenantId(tenantId);
        entity.setTestResultNumber(request.testResultNumber());
        entity.setTestDate(request.testDate());
        entity.setLotNumber(request.lotNumber());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setTestType(request.testType());
        entity.setGerminationPercent(request.germinationPercent());
        entity.setPurityPercent(request.purityPercent());
        entity.setMoisturePercent(request.moisturePercent());
        entity.setInertMatterPercent(request.inertMatterPercent());
        entity.setTestedBy(request.testedBy());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity.setRemarks(request.remarks());
        entity = repository.save(entity);
        log.info("QualityTestResult created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public QualityTestResultDto update(UUID id, CreateQualityTestResultRequest request) {
        QualityTestResult entity = findOrThrow(id);
        entity.setTestResultNumber(request.testResultNumber());
        entity.setTestDate(request.testDate());
        entity.setLotNumber(request.lotNumber());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setTestType(request.testType());
        entity.setGerminationPercent(request.germinationPercent());
        entity.setPurityPercent(request.purityPercent());
        entity.setMoisturePercent(request.moisturePercent());
        entity.setInertMatterPercent(request.inertMatterPercent());
        entity.setTestedBy(request.testedBy());
        entity.setStatus(request.status());
        entity.setRemarks(request.remarks());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        QualityTestResult entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private QualityTestResult findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("QualityTestResult not found: " + id));
    }

    private QualityTestResultDto toDto(QualityTestResult e) {
        return new QualityTestResultDto(
                e.getId(),
                e.getTestResultNumber(),
                e.getTestDate(),
                e.getLotNumber(),
                e.getCropName(),
                e.getVarietyName(),
                e.getTestType(),
                e.getGerminationPercent(),
                e.getPurityPercent(),
                e.getMoisturePercent(),
                e.getInertMatterPercent(),
                e.getTestedBy(),
                e.getStatus(),
                e.getRemarks()
        );
    }
}
