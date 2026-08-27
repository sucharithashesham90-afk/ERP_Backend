package com.erp.platform.modules.supplier.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.supplier.dto.CreateVendorQualificationRequest;
import com.erp.platform.modules.supplier.entity.VendorQualification;
import com.erp.platform.modules.supplier.entity.VendorQualification.QualStatus;
import com.erp.platform.modules.supplier.repository.VendorQualificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VendorQualificationService {

    private final VendorQualificationRepository repo;
    private final VendorRepository vendorRepository;
    private final TenantContext tenantContext;

    public PageResponse<VendorQualification> list(QualStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (status != null) return PageResponse.of(repo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public VendorQualification getById(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Vendor qualification not found: " + id));
    }

    @Transactional
    public VendorQualification create(CreateVendorQualificationRequest request) {
        VendorQualification vq = new VendorQualification();
        vq.setTenantId(tenantContext.current());
        vq.setSubmissionDate(LocalDate.now());
        vq.setStatus(QualStatus.PENDING);
        apply(vq, request);
        return repo.save(vq);
    }

    @Transactional
    public VendorQualification update(UUID id, CreateVendorQualificationRequest request) {
        VendorQualification vq = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Vendor qualification not found: " + id));
        apply(vq, request);
        return repo.save(vq);
    }

    @Transactional
    public VendorQualification approve(UUID id, String notes) {
        UUID tenantId = tenantContext.current();
        VendorQualification vq = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Vendor qualification not found: " + id));
        vq.setStatus(QualStatus.APPROVED);
        vq.setEvaluationDate(LocalDate.now());
        if (notes != null) vq.setNotes(notes);
        VendorQualification saved = repo.save(vq);
        if (vq.getVendorId() != null) {
            try {
                vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, vq.getVendorId())
                        .ifPresent(vendor -> {
                            vendor.setActive(true);
                            vendorRepository.save(vendor);
                            log.info("Vendor {} activated after qualification approval", vq.getVendorId());
                        });
            } catch (Exception e) {
                log.warn("Vendor activation skipped for qualification {}: {}", id, e.getMessage());
            }
        }
        return saved;
    }

    @Transactional
    public VendorQualification reject(UUID id, String notes) {
        UUID tenantId = tenantContext.current();
        VendorQualification vq = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Vendor qualification not found: " + id));
        vq.setStatus(QualStatus.REJECTED);
        vq.setEvaluationDate(LocalDate.now());
        if (notes != null) vq.setNotes(notes);
        VendorQualification saved = repo.save(vq);
        if (vq.getVendorId() != null) {
            try {
                vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, vq.getVendorId())
                        .ifPresent(vendor -> {
                            vendor.setActive(false);
                            vendorRepository.save(vendor);
                            log.info("Vendor {} deactivated after qualification rejection", vq.getVendorId());
                        });
            } catch (Exception e) {
                log.warn("Vendor deactivation skipped for qualification {}: {}", id, e.getMessage());
            }
        }
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        VendorQualification vq = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Vendor qualification not found: " + id));
        vq.setDeletedAt(LocalDateTime.now());
        repo.save(vq);
    }

    private void apply(VendorQualification vq, CreateVendorQualificationRequest r) {
        vq.setVendorId(r.getVendorId());
        vq.setVendorName(r.getVendorName());
        vq.setTechnicalScore(r.getTechnicalScore());
        vq.setFinancialScore(r.getFinancialScore());
        vq.setComplianceScore(r.getComplianceScore());
        // Compute overall score as average of the three
        vq.setOverallScore((vq.getTechnicalScore() + vq.getFinancialScore() + vq.getComplianceScore()) / 3);
        vq.setCertifications(r.getCertifications());
        vq.setEvaluatedBy(r.getEvaluatedBy());
        vq.setValidUntil(r.getValidUntil());
        vq.setNotes(r.getNotes());
    }
}
