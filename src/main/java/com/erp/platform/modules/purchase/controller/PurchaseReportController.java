package com.erp.platform.modules.purchase.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.time.temporal.ChronoUnit;
import java.util.HashMap; import java.util.Map;

@RestController @RequestMapping("/api/v1/purchase")
@RequiredArgsConstructor @Tag(name="Purchase - Reports",description="Purchase reporting endpoints")
public class PurchaseReportController {
    private final PurchaseOrderRepository purchaseOrderRepo;
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;
    private final TenantContext tenantContext;

    @GetMapping("/aging-report") @PreAuthorize("isAuthenticated()") @Operation(summary="Purchase aging report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> agingReport(
            @RequestParam(required=false,defaultValue="") String vendorId,
            @RequestParam(required=false,defaultValue="") String asOfDate,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var today = LocalDate.now();
        var pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        var result = purchaseOrderRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(po -> {
            Map<String,Object> m = new HashMap<>();
            m.put("poNumber",po.getPoNumber()); m.put("vendorId",po.getVendorId());
            m.put("vendorName",po.getVendorName()==null?"":po.getVendorName());
            m.put("orderDate",po.getOrderDate()==null?"":po.getOrderDate().toString());
            m.put("totalAmount",po.getTotalAmount()); m.put("paidAmount",0);
            m.put("balanceDue",po.getTotalAmount());
            long days = po.getOrderDate() != null ? ChronoUnit.DAYS.between(po.getOrderDate(), today) : 0;
            m.put("daysPastDue",Math.max(0,days));
            String bucket = days <= 30 ? "0-30" : days <= 60 ? "31-60" : days <= 90 ? "61-90" : "90+";
            m.put("bucket",bucket); m.put("status",po.getStatus());
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }
}
