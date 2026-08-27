package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesAllotment;
import com.erp.platform.modules.sales.entity.SalesAllotment.AllotmentStatus;
import com.erp.platform.modules.sales.repository.SalesAllotmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesAllotmentService {

    private final SalesAllotmentRepository allotmentRepository;
    private final TenantContext tenantContext;

    public PageResponse<SalesAllotment> list(AllotmentStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? allotmentRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : allotmentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page);
    }

    public SalesAllotment getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public SalesAllotment create(SalesAllotment req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);
        req.setAllotmentNumber(generateNumber(tenantId));
        if (req.getStatus() == null) req.setStatus(AllotmentStatus.PENDING);
        SalesAllotment saved = allotmentRepository.save(req);
        log.info("SalesAllotment created: id={}, number={}", saved.getId(), saved.getAllotmentNumber());
        return saved;
    }

    @Transactional
    public SalesAllotment update(UUID id, SalesAllotment req) {
        SalesAllotment a = findOrThrow(id);
        if (a.getStatus() != AllotmentStatus.PENDING) {
            throw AppException.badRequest("Only PENDING allotments can be edited");
        }
        a.setCustomerId(req.getCustomerId());
        a.setCustomerName(req.getCustomerName());
        a.setProductId(req.getProductId());
        a.setProductName(req.getProductName());
        a.setWarehouseId(req.getWarehouseId());
        a.setWarehouseName(req.getWarehouseName());
        a.setSeasonId(req.getSeasonId());
        a.setSeasonName(req.getSeasonName());
        a.setQuantity(req.getQuantity());
        a.setUnit(req.getUnit());
        a.setValidFrom(req.getValidFrom());
        a.setValidTo(req.getValidTo());
        a.setNotes(req.getNotes());
        return allotmentRepository.save(a);
    }

    @Transactional
    public SalesAllotment updateStatus(UUID id, AllotmentStatus newStatus) {
        SalesAllotment a = findOrThrow(id);
        UUID tenantId = tenantContext.current();

        if (newStatus == AllotmentStatus.ACTIVE) {
            // Enforce date bounds
            if (a.getValidFrom() == null || a.getValidTo() == null) {
                throw AppException.badRequest("Valid From and Valid To dates are required before activating an allotment");
            }
            if (a.getValidFrom().isAfter(a.getValidTo())) {
                throw AppException.badRequest("Valid From date must be before Valid To date");
            }
            // Check for conflicting ACTIVE allotments for the same product + warehouse
            List<SalesAllotment> conflicts = allotmentRepository
                    .findByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(tenantId, a.getProductId(), AllotmentStatus.ACTIVE)
                    .stream()
                    .filter(other -> !other.getId().equals(a.getId()))
                    .filter(other -> other.getWarehouseId() != null && other.getWarehouseId().equals(a.getWarehouseId()))
                    .filter(other -> other.getValidFrom() != null && other.getValidTo() != null)
                    .filter(other -> !a.getValidFrom().isAfter(other.getValidTo())
                            && !a.getValidTo().isBefore(other.getValidFrom()))
                    .toList();
            if (!conflicts.isEmpty()) {
                throw AppException.badRequest(
                        "Conflicting active allotment already exists for this product/warehouse in the given date range: "
                        + conflicts.get(0).getAllotmentNumber());
            }
        }

        if (newStatus == AllotmentStatus.CANCELLED && a.getStatus() == AllotmentStatus.RELEASED) {
            throw AppException.badRequest("Cannot cancel a RELEASED allotment");
        }

        a.setStatus(newStatus);
        return allotmentRepository.save(a);
    }

    @Transactional
    public void delete(UUID id) {
        SalesAllotment a = findOrThrow(id);
        if (a.getStatus() == AllotmentStatus.ACTIVE) {
            throw AppException.badRequest("Cannot delete an ACTIVE allotment. Cancel it first.");
        }
        a.setDeletedAt(LocalDateTime.now());
        allotmentRepository.save(a);
        log.info("SalesAllotment deleted: id={}", id);
    }

    private SalesAllotment findOrThrow(UUID id) {
        return allotmentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales allotment not found: " + id));
    }

    private String generateNumber(UUID tenantId) {
        long count = allotmentRepository.countByTenantId(tenantId);
        return String.format("ALLOT-%d-%04d", Year.now().getValue(), count + 1);
    }
}
