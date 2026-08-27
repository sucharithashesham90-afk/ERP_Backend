package com.erp.platform.modules.quality.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List; import java.util.Map;

@RestController @RequestMapping("/api/v1/quality")
@RequiredArgsConstructor @Tag(name="Quality - Lot History",description="Lot quality history lookup")
public class QualityLotHistoryController {
    private final TenantContext tenantContext;

    @GetMapping("/lot-history") @PreAuthorize("isAuthenticated()") @Operation(summary="Get lot quality history")
    public ResponseEntity<ApiResponse<List<Map<String,Object>>>> lotHistory(
            @RequestParam(required=false,defaultValue="") String lotNumber) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
