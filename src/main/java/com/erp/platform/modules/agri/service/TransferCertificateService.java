package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateTransferCertificateRequest;
import com.erp.platform.modules.agri.dto.TransferCertificateDto;
import com.erp.platform.modules.agri.entity.TransferCertificate;
import com.erp.platform.modules.agri.repository.TransferCertificateRepository;
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
public class TransferCertificateService {

    private final TransferCertificateRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<TransferCertificateDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<TransferCertificate> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public TransferCertificateDto create(CreateTransferCertificateRequest req) {
        TransferCertificate e = new TransferCertificate();
        e.setTenantId(tenantContext.current());
        e.setTcNumber(req.tcNumber());
        e.setIssueDate(req.issueDate());
        e.setLotNumber(req.lotNumber());
        e.setSourcePlant(req.sourcePlant());
        e.setDestinationPlant(req.destinationPlant());
        e.setNumberOfBags(req.numberOfBags());
        e.setQuantityKgs(req.quantityKgs());
        e.setInputType(req.inputType());
        e.setBagWeight(req.bagWeight());
        e.setIntakeDone(req.intakeDone() != null ? req.intakeDone() : false);
        e.setRemarks(req.remarks());
        e.setStatus(req.status() != null ? req.status() : "ISSUED");
        return toDto(repository.save(e));
    }

    public TransferCertificateDto update(UUID id, CreateTransferCertificateRequest req) {
        TransferCertificate e = repository.findById(id).orElseThrow();
        e.setTcNumber(req.tcNumber());
        e.setIssueDate(req.issueDate());
        e.setLotNumber(req.lotNumber());
        e.setSourcePlant(req.sourcePlant());
        e.setDestinationPlant(req.destinationPlant());
        e.setNumberOfBags(req.numberOfBags());
        e.setQuantityKgs(req.quantityKgs());
        e.setInputType(req.inputType());
        e.setBagWeight(req.bagWeight());
        e.setIntakeDone(req.intakeDone());
        e.setRemarks(req.remarks());
        e.setStatus(req.status());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        TransferCertificate e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private TransferCertificateDto toDto(TransferCertificate e) {
        return new TransferCertificateDto(
                e.getId(), e.getTcNumber(), e.getIssueDate(), e.getLotNumber(),
                e.getSourcePlant(), e.getDestinationPlant(), e.getNumberOfBags(),
                e.getQuantityKgs(), e.getInputType(), e.getBagWeight(),
                e.getIntakeDone(), e.getRemarks(), e.getStatus());
    }
}

