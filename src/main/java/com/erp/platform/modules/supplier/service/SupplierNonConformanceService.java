package com.erp.platform.modules.supplier.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.supplier.dto.CreateSupplierNonConformanceRequest;
import com.erp.platform.modules.supplier.dto.SupplierNonConformanceDto;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCStatus;
import com.erp.platform.modules.supplier.repository.SupplierNonConformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierNonConformanceService {

    private final SupplierNonConformanceRepository ncRepo;
    private final TenantContext tenantContext;

    public PageResponse<SupplierNonConformanceDto> list(UUID vendorId, NCStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = vendorId != null && status != null
                ? ncRepo.findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(tenantId, vendorId, status, pageable)
                : vendorId != null
                ? ncRepo.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable)
                : status != null
                ? ncRepo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : ncRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public SupplierNonConformanceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SupplierNonConformanceDto create(CreateSupplierNonConformanceRequest request) {
        UUID tenantId = tenantContext.current();
        SupplierNonConformance nc = new SupplierNonConformance();
        nc.setTenantId(tenantId);
        nc.setNcNumber(generateNumber(tenantId));
        nc.setVendorId(request.getVendorId());
        nc.setVendorName(request.getVendorName());
        nc.setPurchaseOrderId(request.getPurchaseOrderId());
        nc.setGoodsReceiptId(request.getGoodsReceiptId());
        nc.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
        nc.setIssueType(request.getIssueType());
        nc.setSeverity(request.getSeverity());
        nc.setDescription(request.getDescription());
        nc.setRootCause(request.getRootCause());
        nc.setCorrectiveAction(request.getCorrectiveAction());
        nc.setStatus(NCStatus.OPEN);
        nc = ncRepo.save(nc);
        log.info("SupplierNonConformance created: {}", nc.getNcNumber());
        return toDto(nc);
    }

    @Transactional
    public SupplierNonConformanceDto update(UUID id, CreateSupplierNonConformanceRequest request) {
        SupplierNonConformance nc = findOrThrow(id);
        if (nc.getStatus() == NCStatus.CLOSED) {
            throw AppException.badRequest("Closed non-conformances cannot be edited");
        }
        nc.setVendorId(request.getVendorId());
        nc.setVendorName(request.getVendorName());
        nc.setPurchaseOrderId(request.getPurchaseOrderId());
        nc.setGoodsReceiptId(request.getGoodsReceiptId());
        if (request.getIssueDate() != null) nc.setIssueDate(request.getIssueDate());
        nc.setIssueType(request.getIssueType());
        nc.setSeverity(request.getSeverity());
        nc.setDescription(request.getDescription());
        nc.setRootCause(request.getRootCause());
        nc.setCorrectiveAction(request.getCorrectiveAction());
        log.info("SupplierNonConformance updated: {}", nc.getNcNumber());
        return toDto(ncRepo.save(nc));
    }

    @Transactional
    public SupplierNonConformanceDto respond(UUID id, String vendorResponse) {
        SupplierNonConformance nc = findOrThrow(id);
        nc.setVendorResponse(vendorResponse);
        nc.setStatus(NCStatus.VENDOR_RESPONDED);
        return toDto(ncRepo.save(nc));
    }

    @Transactional
    public SupplierNonConformanceDto close(UUID id, String closureNote) {
        SupplierNonConformance nc = findOrThrow(id);
        if (nc.getStatus() == NCStatus.CLOSED) {
            throw AppException.businessRule("Non-conformance is already closed");
        }
        nc.setStatus(NCStatus.CLOSED);
        nc.setClosureDate(LocalDate.now());
        if (closureNote != null && !closureNote.isBlank()) {
            nc.setCorrectiveAction((nc.getCorrectiveAction() != null ? nc.getCorrectiveAction() + "\n" : "") + "Closure: " + closureNote);
        }
        return toDto(ncRepo.save(nc));
    }

    @Transactional
    public void delete(UUID id) {
        SupplierNonConformance nc = findOrThrow(id);
        nc.setDeletedAt(LocalDateTime.now());
        ncRepo.save(nc);
    }

    private SupplierNonConformanceDto toDto(SupplierNonConformance nc) {
        SupplierNonConformanceDto dto = new SupplierNonConformanceDto();
        dto.setId(nc.getId());
        dto.setTenantId(nc.getTenantId());
        dto.setNcNumber(nc.getNcNumber());
        dto.setVendorId(nc.getVendorId());
        dto.setVendorName(nc.getVendorName());
        dto.setPurchaseOrderId(nc.getPurchaseOrderId());
        dto.setGoodsReceiptId(nc.getGoodsReceiptId());
        dto.setIssueDate(nc.getIssueDate());
        dto.setIssueType(nc.getIssueType());
        dto.setSeverity(nc.getSeverity());
        dto.setDescription(nc.getDescription());
        dto.setRootCause(nc.getRootCause());
        dto.setCorrectiveAction(nc.getCorrectiveAction());
        dto.setVendorResponse(nc.getVendorResponse());
        dto.setClosureDate(nc.getClosureDate());
        dto.setStatus(nc.getStatus());
        dto.setCreatedAt(nc.getCreatedAt());
        return dto;
    }

    private SupplierNonConformance findOrThrow(UUID id) {
        return ncRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Non-conformance not found: " + id));
    }

    private String generateNumber(UUID tenantId) {
        long count = ncRepo.countByTenantId(tenantId) + 1;
        return String.format("NC-%d-%03d", Year.now().getValue(), count);
    }
}
