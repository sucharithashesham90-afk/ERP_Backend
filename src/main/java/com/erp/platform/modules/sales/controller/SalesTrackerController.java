package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.crm.entity.Lead;
import com.erp.platform.modules.crm.repository.LeadRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales/tracker")
@RequiredArgsConstructor
@Tag(name = "Sales - Tracker", description = "Sales pipeline tracker aggregating CRM leads")
public class SalesTrackerController {

    private final LeadRepository leadRepo;
    /** Leads carry the assignee's id; the grid wants their name. */
    private final com.erp.platform.modules.auth.repository.UserRepository userRepository;
    private final TenantContext tenantContext;

    // Maps backend Lead status to frontend stage names
    private static String toStage(Lead.LeadStatus status) {
        return switch (status) {
            case NEW, CONTACTED -> "LEAD";
            case QUALIFIED -> "QUALIFIED";
            case PROPOSAL -> "PROPOSAL";
            case NEGOTIATION -> "NEGOTIATION";
            case WON -> "WON";
            case LOST -> "LOST";
        };
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sales pipeline tracker data from CRM leads")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> tracker(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String stage) {
        UUID tenantId = tenantContext.current();

        List<Lead> leads = leadRepo.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1000)).getContent();

        List<Map<String, Object>> result = leads.stream()
                .filter(l -> {
                    // Filter by date range on expectedCloseDate if provided
                    if (from != null && l.getExpectedCloseDate() != null && l.getExpectedCloseDate().isBefore(from)) return false;
                    if (to != null && l.getExpectedCloseDate() != null && l.getExpectedCloseDate().isAfter(to)) return false;
                    return true;
                })
                .filter(l -> {
                    if (stage == null || stage.isBlank()) return true;
                    return stage.equals(toStage(l.getStatus()));
                })
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getId());
                    m.put("name", l.getName() == null ? "" : l.getName());
                    m.put("company", l.getCompany() == null ? "" : l.getCompany());
                    m.put("stage", toStage(l.getStatus()));
                    m.put("value", l.getEstimatedValue());
                    m.put("expectedCloseDate", l.getExpectedCloseDate() == null ? "" : l.getExpectedCloseDate().toString());
                    m.put("priority", l.getPriority() == null ? "" : l.getPriority().name());
                    // The tracker grid reads these four names and none of them were being sent, so
                    // Customer, Description, Sales Rep and Expected Close were blank on every row.
                    // A lead's customer is the company it belongs to, falling back to the contact.
                    m.put("customerName", firstNonBlank(l.getCompany(), l.getName()));
                    m.put("description", l.getNotes() == null ? "" : l.getNotes());
                    m.put("salesRepName", resolveOwner(l.getAssignedTo()));
                    m.put("expectedClose", l.getExpectedCloseDate() == null ? "" : l.getExpectedCloseDate().toString());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return "";
    }

    /** The name of whoever the lead is assigned to; blank when it is unassigned. */
    private String resolveOwner(java.util.UUID assignedTo) {
        if (assignedTo == null) return "";
        return userRepository.findById(assignedTo).map(u -> u.getFullName() == null ? "" : u.getFullName()).orElse("");
    }
}
