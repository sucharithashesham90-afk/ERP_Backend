package com.erp.platform.modules.quality.service;
import java.util.UUID;

import com.erp.platform.modules.quality.dto.CreateSeedHealthCertificateRequest;
import com.erp.platform.modules.quality.dto.SeedHealthCertificateDto;
import com.erp.platform.modules.quality.entity.SeedHealthCertificate;
import com.erp.platform.modules.quality.repository.SeedHealthCertificateRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SeedHealthCertificateService {

    private final SeedHealthCertificateRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<SeedHealthCertificateDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<SeedHealthCertificate> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public SeedHealthCertificateDto create(CreateSeedHealthCertificateRequest req) {
        SeedHealthCertificate e = new SeedHealthCertificate();
        e.setTenantId(tenantContext.current());
        e.setCertificateNumber(req.certificateNumber());
        e.setIssueDate(req.issueDate());
        e.setExpiryDate(req.expiryDate());
        e.setLotNumber(req.lotNumber());
        e.setCropName(req.cropName());
        e.setIssuingAuthority(req.issuingAuthority());
        e.setTestingLaboratory(req.testingLaboratory());
        e.setHealthStatus(req.healthStatus());
        e.setTreatmentRequired(req.treatmentRequired() != null ? req.treatmentRequired() : false);
        e.setRemarks(req.remarks());
        e.setStatus(req.status() != null ? req.status() : "VALID");
        return toDto(repository.save(e));
    }

    public SeedHealthCertificateDto update(UUID id, CreateSeedHealthCertificateRequest req) {
        SeedHealthCertificate e = repository.findById(id).orElseThrow();
        e.setCertificateNumber(req.certificateNumber());
        e.setIssueDate(req.issueDate());
        e.setExpiryDate(req.expiryDate());
        e.setLotNumber(req.lotNumber());
        e.setCropName(req.cropName());
        e.setIssuingAuthority(req.issuingAuthority());
        e.setTestingLaboratory(req.testingLaboratory());
        e.setHealthStatus(req.healthStatus());
        e.setTreatmentRequired(req.treatmentRequired());
        e.setRemarks(req.remarks());
        e.setStatus(req.status());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        SeedHealthCertificate e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private SeedHealthCertificateDto toDto(SeedHealthCertificate e) {
        return new SeedHealthCertificateDto(
                e.getId(), e.getCertificateNumber(), e.getIssueDate(),
                e.getExpiryDate(), e.getLotNumber(), e.getCropName(),
                e.getIssuingAuthority(), e.getTestingLaboratory(),
                e.getHealthStatus(), e.getTreatmentRequired(),
                e.getRemarks(), e.getStatus());
    }
}

