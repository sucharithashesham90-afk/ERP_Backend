package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.service.LotTrackingService;
import com.erp.platform.modules.quality.entity.QualityInspection;
import com.erp.platform.modules.quality.repository.QualityInspectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QualityInspectionService {

    private final QualityInspectionRepository inspectionRepository;
    private final LotTrackingService lotTrackingService;
    private final TenantContext tenantContext;

    public PageResponse<QualityInspection> list(String type, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(type)) {
            return PageResponse.of(inspectionRepository.findByTenantIdAndInspectionTypeAndDeletedAtIsNull(tenantId, type, pageable));
        }
        return PageResponse.of(inspectionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public QualityInspection create(QualityInspection request) {
        UUID tenantId = tenantContext.current();
        request.setTenantId(tenantId);
        request.setInspectionNumber(generateNumber());
        if (request.getInspectionDate() == null) request.setInspectionDate(LocalDate.now());
        if (request.getStatus() == null) request.setStatus("PENDING");
        QualityInspection saved = inspectionRepository.save(request);
        log.info("Quality inspection created: id={}, number={}", saved.getId(), saved.getInspectionNumber());
        return saved;
    }

    @Transactional
    public QualityInspection update(UUID id, QualityInspection request) {
        UUID tenantId = tenantContext.current();
        QualityInspection existing = inspectionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Quality inspection not found: " + id));
        if (request.getInspectionType() != null) existing.setInspectionType(request.getInspectionType());
        if (request.getProductName() != null) existing.setProductName(request.getProductName());
        if (request.getInspectionDate() != null) existing.setInspectionDate(request.getInspectionDate());
        if (request.getInspectorName() != null) existing.setInspectorName(request.getInspectorName());
        if (request.getResult() != null) existing.setResult(request.getResult());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getRemarks() != null) existing.setRemarks(request.getRemarks());
        if (request.getSampleSize() != null) existing.setSampleSize(request.getSampleSize());
        if (request.getDefectsFound() != null) existing.setDefectsFound(request.getDefectsFound());
        if (request.getLotId() != null) existing.setLotId(request.getLotId());
        if (request.getLotNumber() != null) existing.setLotNumber(request.getLotNumber());
        QualityInspection saved = inspectionRepository.save(existing);
        // Auto-quarantine linked lot when inspection result is FAIL
        if ("FAIL".equals(saved.getResult()) && saved.getLotId() != null) {
            try {
                lotTrackingService.quarantineLot(saved.getLotId(),
                        "Failed quality inspection: " + saved.getInspectionNumber()
                        + (saved.getDefectsFound() != null ? " (" + saved.getDefectsFound() + " defects)" : ""));
                log.info("Lot {} quarantined due to failed inspection {}", saved.getLotId(), saved.getInspectionNumber());
            } catch (Exception e) {
                log.warn("Lot quarantine skipped for inspection {}: {}", saved.getInspectionNumber(), e.getMessage());
            }
        }
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        QualityInspection inspection = inspectionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Quality inspection not found: " + id));
        inspection.setDeletedAt(LocalDateTime.now());
        inspectionRepository.save(inspection);
    }

    private String generateNumber() {
        return "QI-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
