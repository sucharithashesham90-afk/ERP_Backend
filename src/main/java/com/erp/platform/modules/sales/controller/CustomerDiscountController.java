package com.erp.platform.modules.sales.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.CustomerDiscount;
import com.erp.platform.modules.sales.repository.CustomerDiscountRepository;
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
import java.util.Map; import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/customer-discounts")
@RequiredArgsConstructor
@Tag(name = "Sales - Customer Discounts", description = "Customer-specific discount management")
public class CustomerDiscountController {
    private final CustomerDiscountRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "List customer discounts")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("customerName"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "Create customer discount")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        CustomerDiscount e = new CustomerDiscount();
        e.setTenantId(tid);
        if (req.get("customerId") != null) e.setCustomerId(UUID.fromString(req.get("customerId").toString()));
        e.setCustomerName((String) req.get("customerName"));
        e.setProductCategory((String) req.get("productCategory"));
        if (req.get("discountPct") != null) e.setDiscountPct(new BigDecimal(req.get("discountPct").toString()));
        if (req.get("validFrom") != null) e.setValidFrom(LocalDate.parse(req.get("validFrom").toString()));
        if (req.get("validTo") != null) e.setValidTo(LocalDate.parse(req.get("validTo").toString()));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary = "Update customer discount")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        CustomerDiscount e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid, id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("customerName")) e.setCustomerName((String) req.get("customerName"));
        if (req.containsKey("discountPct") && req.get("discountPct") != null) e.setDiscountPct(new BigDecimal(req.get("discountPct").toString()));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary = "Delete customer discount")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        CustomerDiscount e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid, id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(CustomerDiscount e) {
        return Map.of("id",e.getId(),
            "customerId",e.getCustomerId()==null?"":e.getCustomerId().toString(),
            "customerName",e.getCustomerName()==null?"":e.getCustomerName(),
            "productCategory",e.getProductCategory()==null?"":e.getProductCategory(),
            "discountPct",e.getDiscountPct(),
            "validFrom",e.getValidFrom()==null?"":e.getValidFrom().toString(),
            "validTo",e.getValidTo()==null?"":e.getValidTo().toString(),
            "active",e.isActive());
    }
}
