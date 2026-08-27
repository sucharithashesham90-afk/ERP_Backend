package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.inventory.entity.InventoryReceipt;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import com.erp.platform.modules.inventory.repository.InventoryReceiptRepository;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
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
@RequestMapping("/api/v1/inventory/receipts")
@RequiredArgsConstructor
@Tag(name = "Inventory - Receipts", description = "Stock receipt documents")
public class InventoryReceiptController {

    private final InventoryReceiptRepository repo;
    private final GodownRepository godownRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List inventory receipts")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID godownId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String receiptNumber,
            @RequestParam(required = false) String status) {
        var pageable = PageRequest.of(page, size);
        var result = repo.search(tenantContext.current(), blankToNull(location), godownId, dateFrom, dateTo,
                blankToNull(receiptNumber), blankToNull(status), pageable).map(this::toMap);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create inventory receipt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        InventoryReceipt e = new InventoryReceipt();
        e.setTenantId(tenantId);
        e.setReceiptNumber(generateNumber(tenantId));
        apply(e, req, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toMap(repo.save(e)), "Receipt created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Update inventory receipt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        InventoryReceipt e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Receipt not found: " + id));
        apply(e, req, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e)), "Receipt updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Cancel (deactivate) inventory receipt — kept for audit, not deleted")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        InventoryReceipt e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Receipt not found: " + id));
        // Audit record: never hard/soft-deleted or zeroed — only marked CANCELLED so it stays visible.
        e.setStatus("CANCELLED");
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Receipt cancelled (kept for audit)"));
    }

    private void apply(InventoryReceipt e, Map<String, Object> req, UUID tenantId) {
        String location = PayloadUtils.str(req, "location");
        if (location == null) throw AppException.badRequest("Location is required");
        UUID godownId = PayloadUtils.uuid(req, "godownId");
        if (godownId == null) throw AppException.badRequest("Godown is required");

        e.setLocation(location);
        e.setGodownId(godownId);
        e.setGodownName(resolveGodownName(tenantId, godownId));
        e.setNetId(PayloadUtils.uuid(req, "netId"));
        // The form posts ids; resolve the display names here so the list does not show blanks.
        UUID supplierId = PayloadUtils.uuid(req, "supplierId");
        e.setSupplierId(supplierId);
        String supplierName = PayloadUtils.str(req, "supplierName");
        if (supplierName == null && supplierId != null) {
            supplierName = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, supplierId)
                    .map(v -> v.getName()).orElse(null);
        }
        e.setSupplierName(supplierName);

        UUID purchaseOrderId = PayloadUtils.uuid(req, "purchaseOrderId");
        e.setPurchaseOrderId(purchaseOrderId);
        String poNo = PayloadUtils.str(req, "poNo");
        if (purchaseOrderId != null) {
            PurchaseOrder po = purchaseOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, purchaseOrderId)
                    .orElseThrow(() -> AppException.notFound("Purchase order not found: " + purchaseOrderId));
            poNo = po.getPoNumber();
        }
        e.setPoNo(poNo);
        e.setReceivedBy(PayloadUtils.str(req, "receivedBy"));
        LocalDate receiptDate = PayloadUtils.date(req, "receiptDate");
        e.setReceiptDate(receiptDate != null ? receiptDate : LocalDate.now());
        e.setInGatePass(PayloadUtils.str(req, "inGatePass"));
        e.setPackDetails(PayloadUtils.str(req, "packDetails"));
        e.setTruckNumber(PayloadUtils.str(req, "truckNumber"));
        e.setDcNo(PayloadUtils.str(req, "dcNo"));
        e.setDcDate(PayloadUtils.date(req, "dcDate"));
        e.setCarrier(PayloadUtils.str(req, "carrier"));
        e.setLrNo(PayloadUtils.str(req, "lrNo"));
        e.setQuantity(PayloadUtils.decimal(req, "quantity"));
        e.setUnit(PayloadUtils.str(req, "unit"));
        e.setLotNumber(PayloadUtils.str(req, "lotNumber"));
    }

    private String resolveGodownName(UUID tenantId, UUID godownId) {
        if (godownId == null) return null;
        return godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, godownId)
                .map(g -> g.getName()).orElse(null);
    }

    private String generateNumber(UUID tenantId) {
        long seq = repo.countByTenantId(tenantId) + 1;
        return String.format("RCP-%05d", seq);
    }

    private Map<String, Object> toMap(InventoryReceipt e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("receiptNumber", e.getReceiptNumber());
        m.put("location", e.getLocation());
        m.put("godownId", e.getGodownId());
        m.put("godownName", e.getGodownName());
        m.put("netId", e.getNetId());
        m.put("supplierId", e.getSupplierId());
        m.put("supplierName", e.getSupplierName());
        m.put("purchaseOrderId", e.getPurchaseOrderId());
        m.put("poNo", e.getPoNo());
        m.put("receivedBy", e.getReceivedBy());
        m.put("receiptDate", e.getReceiptDate() == null ? null : e.getReceiptDate().toString());
        m.put("inGatePass", e.getInGatePass());
        m.put("packDetails", e.getPackDetails());
        m.put("truckNumber", e.getTruckNumber());
        m.put("dcNo", e.getDcNo());
        m.put("dcDate", e.getDcDate() == null ? null : e.getDcDate().toString());
        m.put("carrier", e.getCarrier());
        m.put("lrNo", e.getLrNo());
        m.put("quantity", e.getQuantity());
        m.put("unit", e.getUnit());
        m.put("lotNumber", e.getLotNumber());
        m.put("status", e.getStatus());
        m.put("createdAt", e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
        return m;
    }
}
