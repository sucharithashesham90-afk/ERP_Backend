package com.erp.platform.modules.inventory.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.InwardReceipt;
import com.erp.platform.modules.inventory.repository.InwardReceiptRepository;
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

@RestController @RequestMapping("/api/v1/inventory/inward-receipts")
@RequiredArgsConstructor @Tag(name="Inventory - Inward Receipts",description="Inward receipt management")
public class InwardReceiptController {
    private final InwardReceiptRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List inward receipts")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("createdAt").descending())).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create inward receipt")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        InwardReceipt e = new InwardReceipt(); e.setTenantId(tid);
        String num = req.get("receiptNumber") != null ? req.get("receiptNumber").toString() : "IR-" + System.currentTimeMillis();
        e.setReceiptNumber(num);
        if (req.get("receiptDate") != null) e.setReceiptDate(LocalDate.parse(req.get("receiptDate").toString()));
        if (req.get("warehouseId") != null) e.setWarehouseId(UUID.fromString(req.get("warehouseId").toString()));
        e.setWarehouseName((String) req.get("warehouseName"));
        if (req.get("supplierId") != null) e.setSupplierId(UUID.fromString(req.get("supplierId").toString()));
        e.setSupplierName((String) req.get("supplierName"));
        e.setNotes((String) req.get("notes"));
        e.setStatus(req.getOrDefault("status","DRAFT").toString());
        if (req.get("totalValue") != null) e.setTotalValue(new BigDecimal(req.get("totalValue").toString()));
        if (req.get("lines") != null) e.setLinesJson(req.get("lines").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update inward receipt")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        InwardReceipt e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("status")) e.setStatus((String) req.get("status"));
        if (req.containsKey("notes")) e.setNotes((String) req.get("notes"));
        if (req.containsKey("lines") && req.get("lines") != null) e.setLinesJson(req.get("lines").toString());
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete inward receipt")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        InwardReceipt e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(InwardReceipt e) {
        Map<String,Object> m = new HashMap<>();
        m.put("id",e.getId()); m.put("receiptNumber",e.getReceiptNumber()==null?"":e.getReceiptNumber());
        m.put("receiptDate",e.getReceiptDate()==null?"":e.getReceiptDate().toString());
        m.put("warehouseName",e.getWarehouseName()==null?"":e.getWarehouseName());
        m.put("supplierName",e.getSupplierName()==null?"":e.getSupplierName());
        m.put("notes",e.getNotes()==null?"":e.getNotes());
        m.put("status",e.getStatus()); m.put("totalValue",e.getTotalValue());
        m.put("linesJson",e.getLinesJson()==null?"[]":e.getLinesJson());
        m.put("createdAt",e.getCreatedAt()==null?"":e.getCreatedAt().toString());
        return m;
    }
}
