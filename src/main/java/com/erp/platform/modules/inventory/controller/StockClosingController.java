package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.inventory.entity.StockClosing;
import com.erp.platform.modules.inventory.repository.InventoryIssueRepository;
import com.erp.platform.modules.inventory.repository.InventoryReceiptRepository;
import com.erp.platform.modules.inventory.repository.StockClosingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/stock-closings")
@RequiredArgsConstructor
@Tag(name = "Inventory - Stock Closings", description = "Period stock closing")
public class StockClosingController {

    private final StockClosingRepository repo;
    private final InventoryReceiptRepository receiptRepo;
    private final InventoryIssueRepository issueRepo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List stock closing history")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "closingDate"));
        var result = repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Preview stock as of a date before closing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> preview(@RequestParam String date) {
        UUID tenantId = tenantContext.current();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("date", date);
        m.put("totalLots", 0);
        m.put("totalBags", 0);
        m.put("totalReceipts", receiptRepo.countByTenantId(tenantId));
        m.put("totalIssues", issueRepo.countByTenantId(tenantId));
        return ResponseEntity.ok(ApiResponse.success(m));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN','MANAGER','INVENTORY_MANAGER')")
    @Transactional
    @Operation(summary = "Close stock as of a date")
    public ResponseEntity<ApiResponse<Map<String, Object>>> close(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        LocalDate closingDate = PayloadUtils.date(req, "closingDate");
        if (closingDate == null) throw AppException.badRequest("Closing date is required");

        StockClosing e = new StockClosing();
        e.setTenantId(tenantId);
        e.setClosingDate(closingDate);
        e.setClosedBy(currentUser());
        e.setClosedAt(LocalDateTime.now());
        e.setStatus("CLOSED");
        e.setTotalReceipts(receiptRepo.countByTenantId(tenantId));
        e.setTotalIssues(issueRepo.countByTenantId(tenantId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toMap(repo.save(e)), "Stock closed"));
    }

    private String currentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> toMap(StockClosing e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("closingDate", e.getClosingDate() == null ? null : e.getClosingDate().toString());
        m.put("closedBy", e.getClosedBy());
        m.put("closedAt", e.getClosedAt() == null ? null : e.getClosedAt().toString());
        m.put("status", e.getStatus());
        m.put("totalLots", e.getTotalLots());
        m.put("totalBags", e.getTotalBags());
        m.put("totalReceipts", e.getTotalReceipts());
        m.put("totalIssues", e.getTotalIssues());
        return m;
    }
}
