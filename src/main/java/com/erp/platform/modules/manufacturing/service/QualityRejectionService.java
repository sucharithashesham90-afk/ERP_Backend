package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.QualityRejection;
import com.erp.platform.modules.manufacturing.entity.QualityRejection.RejectionStatus;
import com.erp.platform.modules.manufacturing.repository.QualityRejectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QualityRejectionService {

    private final QualityRejectionRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<QualityRejection> list(RejectionStatus status, UUID productId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (status != null) {
            return PageResponse.of(repo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        }
        if (productId != null) {
            return PageResponse.of(repo.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId, pageable));
        }
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public QualityRejection getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public QualityRejection create(QualityRejection req) {
        req.setTenantId(tenantContext.current());
        req.setRejectionNumber(generateRejectionNumber());
        req.setStatus(RejectionStatus.OPEN);
        return repo.save(req);
    }

    @Transactional
    public QualityRejection update(UUID id, QualityRejection req) {
        QualityRejection e = findOrThrow(id);
        e.setBatchId(req.getBatchId());
        e.setBatchNumber(req.getBatchNumber());
        e.setProduct(req.getProduct());
        e.setRejectionDate(req.getRejectionDate());
        e.setQuantity(req.getQuantity());
        e.setUnit(req.getUnit());
        e.setRejectionReason(req.getRejectionReason());
        e.setFailedParameters(req.getFailedParameters());
        e.setDisposition(req.getDisposition());
        e.setDispositionDate(req.getDispositionDate());
        e.setDispositionNotes(req.getDispositionNotes());
        return repo.save(e);
    }

    @Transactional
    public QualityRejection updateDisposition(UUID id, String disposition, String notes) {
        QualityRejection e = findOrThrow(id);
        e.setDisposition(QualityRejection.Disposition.valueOf(disposition));
        e.setDispositionNotes(notes);
        e.setDispositionDate(LocalDate.now());
        if (e.getStatus() == RejectionStatus.OPEN) {
            e.setStatus(RejectionStatus.IN_REVIEW);
        }
        return repo.save(e);
    }

    @Transactional
    public QualityRejection resolve(UUID id, String correctiveAction, String preventiveAction) {
        QualityRejection e = findOrThrow(id);
        e.setCorrectiveAction(correctiveAction);
        e.setPreventiveAction(preventiveAction);
        e.setStatus(RejectionStatus.RESOLVED);
        return repo.save(e);
    }

    @Transactional
    public QualityRejection close(UUID id) {
        QualityRejection e = findOrThrow(id);
        if (e.getStatus() != RejectionStatus.RESOLVED) {
            throw AppException.businessRule("Only resolved rejections can be closed");
        }
        e.setStatus(RejectionStatus.CLOSED);
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        QualityRejection e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    private QualityRejection findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Quality rejection not found: " + id));
    }

    private String generateRejectionNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "REJ-" + date + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
