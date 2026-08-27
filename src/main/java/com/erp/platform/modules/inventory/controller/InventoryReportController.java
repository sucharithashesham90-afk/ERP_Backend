package com.erp.platform.modules.inventory.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor @Tag(name="Inventory - Reports",description="Inventory reporting endpoints")
public class InventoryReportController {
    private final TenantContext tenantContext;

    @GetMapping("/stock-valuation") @PreAuthorize("isAuthenticated()") @Operation(summary="Stock valuation report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> stockValuation(
            @RequestParam(required=false,defaultValue="") String asOfDate,
            @RequestParam(required=false,defaultValue="") String warehouse,
            @RequestParam(required=false,defaultValue="") String product,
            @RequestParam(required=false,defaultValue="WA") String method,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<Map<String,Object>>builder()
            .content(List.of()).page(page).size(size).totalElements(0).totalPages(0).first(true).last(true).build()));
    }

    @GetMapping("/stock-movements") @PreAuthorize("isAuthenticated()") @Operation(summary="Stock movement report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> stockMovements(
            @RequestParam(required=false,defaultValue="") String productCode,
            @RequestParam(required=false,defaultValue="") String warehouse,
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<Map<String,Object>>builder()
            .content(List.of()).page(page).size(size).totalElements(0).totalPages(0).first(true).last(true).build()));
    }
}
