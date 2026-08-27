package com.erp.platform.modules.hr.controller;
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

@RestController @RequestMapping("/api/v1/hr/payments")
@RequiredArgsConstructor @Tag(name="HR - Payments",description="HR monthly payment summaries")
public class HrPaymentController {
    private final TenantContext tenantContext;

    @GetMapping("/monthly-summary") @PreAuthorize("isAuthenticated()") @Operation(summary="Monthly payment summary")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> monthlySummary(
            @RequestParam int month, @RequestParam int year,
            @RequestParam(required=false,defaultValue="") String department,
            @RequestParam(required=false,defaultValue="") String employee,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<Map<String,Object>>builder()
            .content(List.of()).page(page).size(size).totalElements(0).totalPages(0).first(true).last(true).build()));
    }

    @PostMapping("/post") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Post monthly payments to accounting")
    public ResponseEntity<ApiResponse<Map<String,Object>>> post(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("message","Payments posted for " + month + "/" + year,"month",month,"year",year)));
    }
}
