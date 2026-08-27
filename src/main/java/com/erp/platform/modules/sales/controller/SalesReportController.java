package com.erp.platform.modules.sales.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.ReceiptRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap; import java.util.Map;

@RestController @RequestMapping("/api/v1/sales")
@RequiredArgsConstructor @Tag(name="Sales - Reports",description="Sales reporting endpoints")
public class SalesReportController {
    private final InvoiceRepository invoiceRepo;
    private final ReceiptRepository receiptRepo;
    private final TenantContext tenantContext;

    @GetMapping("/aging-report") @PreAuthorize("isAuthenticated()") @Operation(summary="Sales aging report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> agingReport(
            @RequestParam(required=false,defaultValue="") String customerId,
            @RequestParam(required=false,defaultValue="") String repId,
            @RequestParam(required=false,defaultValue="") String asOfDate,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var today = LocalDate.now();
        var pageable = PageRequest.of(page, size, Sort.by("dueDate"));
        var result = invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(i -> {
            Map<String,Object> m = new HashMap<>();
            m.put("invoiceId",i.getId()); m.put("invoiceNumber",i.getInvoiceNumber());
            m.put("customerId",i.getCustomerId()); m.put("customerName",i.getCustomerName()==null?"":i.getCustomerName());
            m.put("invoiceDate",i.getInvoiceDate()==null?"":i.getInvoiceDate().toString());
            m.put("dueDate",i.getDueDate()==null?"":i.getDueDate().toString());
            m.put("totalAmount",i.getTotalAmount()); m.put("paidAmount",i.getPaidAmount());
            m.put("balanceDue",i.getTotalAmount().subtract(i.getPaidAmount()));
            long days = i.getDueDate() != null ? ChronoUnit.DAYS.between(i.getDueDate(), today) : 0;
            m.put("daysPastDue", Math.max(0, days));
            String bucket = days <= 0 ? "CURRENT" : days <= 30 ? "0-30" : days <= 60 ? "31-60" : days <= 90 ? "61-90" : "90+";
            m.put("bucket",bucket); m.put("status",i.getStatus());
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/customer-payments") @PreAuthorize("isAuthenticated()") @Operation(summary="Customer payments")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> customerPayments(
            @RequestParam(required=false,defaultValue="") String customerId,
            @RequestParam(required=false,defaultValue="") String method,
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        var result = receiptRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(r -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id",r.getId()); m.put("receiptNumber",r.getReceiptNumber());
            m.put("customerId",r.getCustomerId()); m.put("customerName","");
            m.put("paymentDate",r.getPaymentDate()==null?"":r.getPaymentDate().toString());
            m.put("amount",r.getAmount()); m.put("method",r.getPaymentMethod()==null?"":r.getPaymentMethod());
            m.put("referenceNumber",r.getReferenceNumber()==null?"":r.getReferenceNumber());
            m.put("status",r.getStatus());
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }
}
