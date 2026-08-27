package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.ProductionJobAdvance;
import com.erp.platform.modules.agri.repository.ProductionJobAdvanceRepository;
import com.erp.platform.modules.agri.service.ProductionJobAdvanceLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advances given to growers/organizers at production-job allocation time.
 */
@RestController
@RequestMapping("/api/v1/agri/production-job-advances")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProductionJobAdvanceController {

    private final ProductionJobAdvanceRepository repository;
    private final ProductionJobAdvanceLedgerService ledgerService;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) UUID allocateeId) {
        UUID tenantId = tenantContext.current();
        if (jobId != null) {
            return ResponseEntity.ok(ApiResponse.success(repository.findByTenantIdAndJobIdAndDeletedAtIsNull(tenantId, jobId)));
        }
        if (allocateeId != null) {
            return ResponseEntity.ok(ApiResponse.success(repository.findByTenantIdAndAllocateeIdAndDeletedAtIsNull(tenantId, allocateeId)));
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable))));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ProductionJobAdvance>> grant(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        String allocateeIdStr = str(body, "allocateeId");
        BigDecimal amount = decimal(body, "amount");
        if (allocateeIdStr == null) return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "allocateeId is required"));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "A positive amount is required"));

        ProductionJobAdvance a = new ProductionJobAdvance();
        a.setTenantId(tenantId);
        a.setAdvanceNumber(String.format("ADV-%d-%04d", Year.now().getValue(),
                repository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1));
        String jobId = str(body, "jobId");
        if (jobId != null) a.setJobId(UUID.fromString(jobId));
        a.setJobNumber(str(body, "jobNumber"));
        a.setAllocateeType(str(body, "allocateeType"));
        a.setAllocateeId(UUID.fromString(allocateeIdStr));
        a.setAllocateeName(str(body, "allocateeName"));
        String pmId = str(body, "pricingMethodId");
        if (pmId != null) a.setPricingMethodId(UUID.fromString(pmId));
        a.setPricingMethodName(str(body, "pricingMethodName"));
        a.setAmount(amount);
        a.setPaymentMethod(str(body, "paymentMethod"));
        String dateStr = str(body, "advanceDate");
        a.setAdvanceDate(dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now());
        a.setReferenceNumber(str(body, "referenceNumber"));
        a.setRemarks(str(body, "remarks"));
        ProductionJobAdvance saved = repository.save(a);
        ledgerService.postAdvance(saved); // Dr Advance / Cr Cash|Bank (best-effort)
        return ResponseEntity.ok(ApiResponse.success(saved, "Advance recorded"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        repository.findById(id).filter(a -> tenantId.equals(a.getTenantId()) && a.getDeletedAt() == null).ifPresent(a -> {
            a.setDeletedAt(java.time.LocalDateTime.now());
            repository.save(a);
        });
        return ResponseEntity.ok(ApiResponse.success(null, "Advance removed"));
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null && !v.toString().isBlank() ? v.toString() : null;
    }

    private static BigDecimal decimal(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
