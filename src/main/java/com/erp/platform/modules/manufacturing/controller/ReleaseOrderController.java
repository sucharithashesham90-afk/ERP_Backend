package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.ReleaseOrder;
import com.erp.platform.modules.manufacturing.repository.ReleaseOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manufacturing/release-orders")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Release Orders", description = "Release orders raised from Process Job outputs")
public class ReleaseOrderController {

    private final ReleaseOrderRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List release orders")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a release order from a Process Job output line")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ReleaseOrder ro = new ReleaseOrder();
        ro.setTenantId(tenantId);
        ro.setRoNumber(str(req, "roNumber") != null && !str(req, "roNumber").isBlank()
                ? str(req, "roNumber") : generateNumber(tenantId));
        if (str(req, "processJobId") != null && !str(req, "processJobId").isBlank())
            ro.setProcessJobId(UUID.fromString(str(req, "processJobId")));
        ro.setJobNumber(str(req, "jobNumber"));
        ro.setProductName(str(req, "productName"));
        ro.setOutputLotNumber(str(req, "outputLotNumber"));
        ro.setQuantity(decimal(req, "quantity"));
        ro.setGodownName(str(req, "godownName"));
        ro.setNetCompartmentName(str(req, "netCompartmentName"));
        ro.setMrpValue(decimal(req, "mrpValue"));
        ro.setStatus("CREATED");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(ro)),
                "Release order " + ro.getRoNumber() + " created"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a release order")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ReleaseOrder ro = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Release order not found: " + id));
        ro.setDeletedAt(java.time.LocalDateTime.now());
        repo.save(ro);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String generateNumber(UUID tenantId) {
        long n = repo.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("RO-%d-%05d", LocalDate.now().getYear(), n);
    }

    private static String str(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? null : v.toString();
    }

    private static BigDecimal decimal(Map<String, Object> r, String k) {
        String s = str(r, k);
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Map<String, Object> toMap(ReleaseOrder r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("roNumber", r.getRoNumber());
        m.put("jobNumber", r.getJobNumber() == null ? "" : r.getJobNumber());
        m.put("productName", r.getProductName() == null ? "" : r.getProductName());
        m.put("outputLotNumber", r.getOutputLotNumber() == null ? "" : r.getOutputLotNumber());
        m.put("quantity", r.getQuantity());
        m.put("godownName", r.getGodownName() == null ? "" : r.getGodownName());
        m.put("netCompartmentName", r.getNetCompartmentName() == null ? "" : r.getNetCompartmentName());
        m.put("status", r.getStatus());
        m.put("createdAt", r.getCreatedAt() == null ? "" : r.getCreatedAt().toString());
        return m;
    }
}
