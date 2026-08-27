package com.erp.platform.modules.reports.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.AgriJobAllocation;
import com.erp.platform.modules.agri.repository.AgriJobAllocationRepository;
import com.erp.platform.modules.manufacturing.entity.ProductionJob;
import com.erp.platform.modules.manufacturing.repository.ProductionJobRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What was planned to be grown, and who is growing it.
 *
 * <p>A production job carries the crop cascade and the location; the allocations underneath it
 * carry the growers, their acreage and what each is expected to deliver. Neither half answers the
 * planning question on its own — "which growers are on this variety and for how much land" needs
 * both — so this report joins them.
 *
 * <p>One row per grower allocation. A job with no allocations still appears, with the grower columns
 * empty: a plan with nobody assigned to it is exactly the thing worth seeing.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports - Production Plan")
public class ProductionPlanReportController {

    private final ProductionJobRepository productionJobRepository;
    private final AgriJobAllocationRepository allocationRepository;
    private final TenantContext tenantContext;

    @GetMapping("/production-plan")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Production plan: crop, variety, location, grower, acreage and quantities")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> productionPlan(
            @RequestParam(required = false, defaultValue = "") String cropGroup,
            @RequestParam(required = false, defaultValue = "") String crop,
            @RequestParam(required = false, defaultValue = "") String variety,
            @RequestParam(required = false, defaultValue = "") String location,
            @RequestParam(required = false, defaultValue = "") String grower) {

        UUID tenantId = tenantContext.current();
        List<Map<String, Object>> rows = new ArrayList<>();

        for (ProductionJob job : productionJobRepository
                .findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000)).getContent()) {

            if (!matches(cropGroup, job.getCropGroupName())) continue;
            if (!matches(crop, job.getCropName())) continue;
            if (!matches(variety, job.getVarietyName())) continue;
            if (!matches(location, job.getLocationName())) continue;

            List<AgriJobAllocation> allocations =
                    allocationRepository.findByTenantIdAndJobIdAndDeletedAtIsNull(tenantId, job.getId());

            boolean any = false;
            for (AgriJobAllocation a : allocations) {
                if (!matches(grower, a.getAllocateeName())) continue;
                rows.add(row(job, a));
                any = true;
            }
            // Only show the unallocated job when the user is not searching for a particular grower;
            // otherwise a filtered run fills up with jobs that have nothing to do with them.
            if (!any && (grower == null || grower.isBlank())) rows.add(row(job, null));
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(new PageImpl<>(rows))));
    }

    private static Map<String, Object> row(ProductionJob job, AgriJobAllocation a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("jobNumber", job.getJobNumber());
        m.put("jobStatus", job.getStatus() != null ? job.getStatus().name() : null);
        m.put("plannedStartDate", job.getPlannedStartDate() != null ? job.getPlannedStartDate().toString() : null);
        m.put("cropGroupName", job.getCropGroupName());
        m.put("cropName", job.getCropName());
        m.put("varietyName", job.getVarietyName());
        m.put("location", job.getLocationName());

        // The grower on the job header is the one it was raised for; an allocation names the grower
        // actually carrying that share of it, so the allocation wins where there is one.
        m.put("growerName", a != null && a.getAllocateeName() != null ? a.getAllocateeName() : job.getGrowerName());
        m.put("allocationType", a != null ? a.getAllocationType() : null);
        m.put("acreage", a != null ? nz(a.getAcreageAcres()) : BigDecimal.ZERO);
        m.put("quantity", a != null ? nz(a.getQuantityKgs()) : nz(job.getInputQuantity()));
        m.put("estimatedQuantity", a != null ? nz(a.getExpectedYieldKgs()) : nz(job.getPlannedOutputQuantity()));
        m.put("lotNumber", a != null ? a.getTempLotNumber() : job.getLotNumber());
        m.put("allocated", a != null);
        return m;
    }

    private static BigDecimal nz(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    /** A blank filter matches everything; otherwise a case-insensitive contains. */
    private static boolean matches(String filter, String value) {
        if (filter == null || filter.isBlank()) return true;
        return value != null && value.toLowerCase().contains(filter.toLowerCase());
    }
}
