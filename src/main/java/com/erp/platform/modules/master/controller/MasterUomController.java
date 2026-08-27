package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.UnitOfMeasure;
import com.erp.platform.modules.master.repository.UnitOfMeasureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/master/units-of-measure")
@RequiredArgsConstructor
@Tag(name = "Master - Units of Measure", description = "UOM management")
public class MasterUomController {

    private final UnitOfMeasureRepository uomRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List units of measure")
    public ResponseEntity<ApiResponse<PageResponse<UnitOfMeasure>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String categoryCode) {
        var tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(uomRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable))));
    }
}
