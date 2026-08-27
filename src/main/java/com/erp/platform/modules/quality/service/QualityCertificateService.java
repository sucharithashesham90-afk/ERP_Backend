package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.CreateQualityCertificateRequest;
import com.erp.platform.modules.quality.dto.QualityCertificateDto;
import com.erp.platform.modules.quality.entity.QualityCertificate;
import com.erp.platform.modules.quality.repository.QualityCertificateRepository;
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
public class QualityCertificateService {

    private final QualityCertificateRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<QualityCertificateDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public QualityCertificateDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public QualityCertificateDto create(CreateQualityCertificateRequest request) {
        UUID tenantId = tenantContext.current();
        QualityCertificate entity = new QualityCertificate();
        entity.setTenantId(tenantId);
        String certNo = request.certificateNumber();
        if (certNo == null || certNo.isBlank()) {
            long n = repository.findByTenantIdAndDeletedAtIsNull(tenantId, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
            certNo = String.format("CERT-%d-%04d", java.time.LocalDate.now().getYear(), n + 1);
        }
        entity.setCertificateNumber(certNo);
        entity.setIssueDate(request.issueDate());
        entity.setLotNumber(request.lotNumber());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setValidUpTo(request.validUpTo());
        entity.setCertifyingBody(request.certifyingBody());
        entity.setGerminationPercent(request.germinationPercent());
        entity.setPurityPercent(request.purityPercent());
        entity.setMoisturePercent(request.moisturePercent());
        entity.setStatus(request.status() != null ? request.status() : "ISSUED");
        entity = repository.save(entity);
        log.info("QualityCertificate created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public QualityCertificateDto update(UUID id, CreateQualityCertificateRequest request) {
        QualityCertificate entity = findOrThrow(id);
        if (request.certificateNumber() != null && !request.certificateNumber().isBlank()) entity.setCertificateNumber(request.certificateNumber());
        entity.setIssueDate(request.issueDate());
        entity.setLotNumber(request.lotNumber());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setValidUpTo(request.validUpTo());
        entity.setCertifyingBody(request.certifyingBody());
        entity.setGerminationPercent(request.germinationPercent());
        entity.setPurityPercent(request.purityPercent());
        entity.setMoisturePercent(request.moisturePercent());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        QualityCertificate entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private QualityCertificate findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("QualityCertificate not found: " + id));
    }

    private QualityCertificateDto toDto(QualityCertificate e) {
        return new QualityCertificateDto(
                e.getId(),
                e.getCertificateNumber(),
                e.getIssueDate(),
                e.getLotNumber(),
                e.getCropName(),
                e.getVarietyName(),
                e.getValidUpTo(),
                e.getCertifyingBody(),
                e.getGerminationPercent(),
                e.getPurityPercent(),
                e.getMoisturePercent(),
                e.getStatus()
        );
    }
}
