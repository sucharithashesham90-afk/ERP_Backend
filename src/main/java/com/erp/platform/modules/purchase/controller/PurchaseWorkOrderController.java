package com.erp.platform.modules.purchase.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.PurchaseWorkOrder;
import com.erp.platform.modules.purchase.repository.PurchaseWorkOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
import java.util.HashMap; import java.util.Map; import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/work-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase - Work Orders", description = "Purchase work order management")
public class PurchaseWorkOrderController {
    private final PurchaseWorkOrderRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "List purchase work orders")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("createdAt").descending())).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "Create purchase work order")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PurchaseWorkOrder e = new PurchaseWorkOrder();
        e.setTenantId(tid);
        // The UI sends orderNo/serviceDesc/supplier/amount/deliveryDate — accept those (fallback to entity names).
        String num = firstNonBlank(str(req,"orderNumber"), str(req,"orderNo"));
        e.setOrderNumber(num != null ? num : "WO-" + System.currentTimeMillis());
        // title is NOT NULL — derive from the service description (or order number) so it's never null.
        e.setTitle(firstNonBlank(str(req,"title"), str(req,"serviceDesc"), str(req,"description"), e.getOrderNumber()));
        e.setDescription(firstNonBlank(str(req,"description"), str(req,"remarks"), str(req,"serviceDesc")));
        if (req.get("vendorId") != null) e.setVendorId(UUID.fromString(req.get("vendorId").toString()));
        e.setVendorName(firstNonBlank(str(req,"vendorName"), str(req,"supplier")));
        String sched = firstNonBlank(str(req,"scheduledDate"), str(req,"deliveryDate"));
        if (sched != null) e.setScheduledDate(LocalDate.parse(sched));
        Object cost = req.get("estimatedCost") != null ? req.get("estimatedCost") : req.get("amount");
        if (cost != null && !cost.toString().isBlank()) e.setEstimatedCost(new BigDecimal(cost.toString()));
        e.setStatus(req.getOrDefault("status","DRAFT").toString());
        e.setPriority(req.getOrDefault("priority","MEDIUM").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary = "Update purchase work order")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PurchaseWorkOrder e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid, id).orElseThrow(() -> AppException.notFound("Not found"));
        String title = firstNonBlank(str(req,"title"), str(req,"serviceDesc")); if (title != null) e.setTitle(title);
        String desc = firstNonBlank(str(req,"description"), str(req,"remarks")); if (desc != null) e.setDescription(desc);
        String vendor = firstNonBlank(str(req,"vendorName"), str(req,"supplier")); if (vendor != null) e.setVendorName(vendor);
        String sched = firstNonBlank(str(req,"scheduledDate"), str(req,"deliveryDate")); if (sched != null) e.setScheduledDate(LocalDate.parse(sched));
        Object cost = req.get("estimatedCost") != null ? req.get("estimatedCost") : req.get("amount");
        if (cost != null && !cost.toString().isBlank()) e.setEstimatedCost(new BigDecimal(cost.toString()));
        if (req.containsKey("status")) e.setStatus((String) req.get("status"));
        if (req.get("actualCost") != null && !req.get("actualCost").toString().isBlank()) e.setActualCost(new BigDecimal(req.get("actualCost").toString()));
        if (req.get("completedDate") != null && !req.get("completedDate").toString().isBlank()) e.setCompletedDate(LocalDate.parse(req.get("completedDate").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary = "Delete purchase work order")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        PurchaseWorkOrder e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid, id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private static String str(Map<String,Object> r, String k) {
        Object v = r.get(k); return (v == null || v.toString().isBlank()) ? null : v.toString();
    }
    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private Map<String,Object> toMap(PurchaseWorkOrder e) {
        Map<String,Object> m = new HashMap<>();
        m.put("id",e.getId()); m.put("orderNumber",e.getOrderNumber()==null?"":e.getOrderNumber());
        m.put("title",e.getTitle()==null?"":e.getTitle()); m.put("description",e.getDescription()==null?"":e.getDescription());
        m.put("vendorName",e.getVendorName()==null?"":e.getVendorName());
        m.put("scheduledDate",e.getScheduledDate()==null?"":e.getScheduledDate().toString());
        m.put("estimatedCost",e.getEstimatedCost()); m.put("actualCost",e.getActualCost());
        m.put("status",e.getStatus()); m.put("priority",e.getPriority());
        m.put("active",e.isActive()); m.put("createdAt",e.getCreatedAt()==null?"":e.getCreatedAt().toString());
        // UI-name aliases so the list grid (orderNo/serviceDesc/supplier/amount/deliveryDate) shows data.
        m.put("orderNo", e.getOrderNumber()==null?"":e.getOrderNumber());
        m.put("serviceDesc", e.getTitle()==null?"":e.getTitle());
        m.put("supplier", e.getVendorName()==null?"":e.getVendorName());
        m.put("amount", e.getEstimatedCost());
        m.put("deliveryDate", e.getScheduledDate()==null?"":e.getScheduledDate().toString());
        m.put("remarks", e.getDescription()==null?"":e.getDescription());
        return m;
    }
}
