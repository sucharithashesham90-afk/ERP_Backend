package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.inventory.entity.InventoryIssue;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import com.erp.platform.modules.inventory.repository.InventoryIssueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/issues")
@RequiredArgsConstructor
@Tag(name = "Inventory - Issues", description = "Stock issue documents")
public class InventoryIssueController {

    private final InventoryIssueRepository repo;
    private final GodownRepository godownRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List inventory issues")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID godownId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String issueNumber,
            @RequestParam(required = false) String status) {
        var pageable = PageRequest.of(page, size);
        var result = repo.search(tenantContext.current(), blankToNull(location), godownId, dateFrom, dateTo,
                blankToNull(issueNumber), blankToNull(status), pageable).map(this::toMap);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create inventory issue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        InventoryIssue e = new InventoryIssue();
        e.setTenantId(tenantId);
        e.setIssueNumber(generateNumber(tenantId));
        apply(e, req, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toMap(repo.save(e)), "Issue created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Update inventory issue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        InventoryIssue e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Issue not found: " + id));
        apply(e, req, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e)), "Issue updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Cancel (deactivate) inventory issue — kept for audit, not deleted")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        InventoryIssue e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Issue not found: " + id));
        // Audit record: never hard/soft-deleted or zeroed — only marked CANCELLED so it stays visible.
        e.setStatus("CANCELLED");
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Issue cancelled (kept for audit)"));
    }

    private void apply(InventoryIssue e, Map<String, Object> req, UUID tenantId) {
        String location = PayloadUtils.str(req, "location");
        if (location == null) throw AppException.badRequest("Location is required");
        UUID godownId = PayloadUtils.uuid(req, "godownId");
        if (godownId == null) throw AppException.badRequest("Godown is required");

        e.setLocation(location);
        e.setGodownId(godownId);
        e.setGodownName(resolveGodownName(tenantId, godownId));
        e.setNetId(PayloadUtils.uuid(req, "netId"));
        LocalDate issueDate = PayloadUtils.date(req, "issueDate");
        e.setIssueDate(issueDate != null ? issueDate : LocalDate.now());
        e.setIssuedBy(PayloadUtils.str(req, "issuedBy"));
        e.setTruckInvolved(PayloadUtils.bool(req, "isTruckInvolved"));
        e.setIssueTo(PayloadUtils.str(req, "issueTo"));
        e.setQuantity(PayloadUtils.decimal(req, "quantity"));
        e.setUnit(PayloadUtils.str(req, "unit"));
        e.setLotNumber(PayloadUtils.str(req, "lotNumber"));
        e.setNotes(PayloadUtils.str(req, "notes"));
    }

    private String resolveGodownName(UUID tenantId, UUID godownId) {
        if (godownId == null) return null;
        return godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, godownId)
                .map(g -> g.getName()).orElse(null);
    }

    private String generateNumber(UUID tenantId) {
        long seq = repo.countByTenantId(tenantId) + 1;
        return String.format("ISS-%05d", seq);
    }

    private Map<String, Object> toMap(InventoryIssue e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("issueNumber", e.getIssueNumber());
        m.put("location", e.getLocation());
        m.put("godownId", e.getGodownId());
        m.put("godownName", e.getGodownName());
        m.put("netId", e.getNetId());
        m.put("issueDate", e.getIssueDate() == null ? null : e.getIssueDate().toString());
        m.put("issuedBy", e.getIssuedBy());
        m.put("isTruckInvolved", e.isTruckInvolved());
        m.put("issueTo", e.getIssueTo());
        m.put("quantity", e.getQuantity());
        m.put("unit", e.getUnit());
        m.put("lotNumber", e.getLotNumber());
        m.put("notes", e.getNotes());
        m.put("status", e.getStatus());
        m.put("createdAt", e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
        return m;
    }
}
