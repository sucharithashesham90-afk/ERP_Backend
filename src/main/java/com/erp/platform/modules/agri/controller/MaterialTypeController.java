package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.MaterialType;
import com.erp.platform.modules.agri.repository.MaterialTypeRepository;
import com.erp.platform.modules.agri.repository.SeedStateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Material types, and material states under the name the screens ask for.
 *
 * <p>Seed conversion, opening inventory, physical inventory and dispatch all load
 * /agri/material-types and /agri/material-states. Neither existed. Every one of those calls ends in
 * a swallowed catch, so the dropdowns were permanently empty and nothing anywhere said a request had
 * failed.
 *
 * <p>Material state is the seed-state master seen from the inventory side rather than a second list,
 * so it is served from that master here instead of being duplicated - one master, two names, no way
 * for the two to drift apart.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Agri - Material Types & States")
public class MaterialTypeController {

    private final MaterialTypeRepository repo;
    private final SeedStateRepository seedStateRepo;
    private final TenantContext tenantContext;

    // ── Material types ───────────────────────────────────────────────────────

    @GetMapping("/api/v1/agri/material-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List material types")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID t = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
                repo.findByTenantIdAndDeletedAtIsNull(t, PageRequest.of(page, size, Sort.by("name")))
                        .map(MaterialTypeController::toMap))));
    }

    @PostMapping("/api/v1/agri/material-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create material type")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        MaterialType e = new MaterialType();
        e.setTenantId(tenantContext.current());
        apply(e, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e)), "Created"));
    }

    @PutMapping("/api/v1/agri/material-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update material type")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id,
                                                                   @RequestBody Map<String, Object> req) {
        MaterialType e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Material type not found: " + id));
        apply(e, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/api/v1/agri/material-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete material type")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        MaterialType e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Material type not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Material states (the seed-state master, under the name the screens use) ──

    @GetMapping("/api/v1/agri/material-states")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List material states (served from the seed-state master)")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> listStates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID t = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
                seedStateRepo.findByTenantIdAndDeletedAtIsNull(t, PageRequest.of(page, size, Sort.by("name")))
                        .map(s -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", s.getId());
                            m.put("code", s.getCode() == null ? "" : s.getCode());
                            m.put("name", s.getName());
                            m.put("description", s.getDescription() == null ? "" : s.getDescription());
                            m.put("active", s.isActive());
                            return m;
                        }))));
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private static void apply(MaterialType e, Map<String, Object> req) {
        Object name = req.get("name");
        if (name == null || String.valueOf(name).isBlank()) throw AppException.badRequest("Name is required");
        e.setName(String.valueOf(name).trim());
        if (req.containsKey("code")) e.setCode(str(req.get("code")));
        if (req.containsKey("description")) e.setDescription(str(req.get("description")));
        if (req.containsKey("sortOrder")) {
            try { e.setSortOrder(Integer.valueOf(String.valueOf(req.get("sortOrder")).trim())); }
            catch (NumberFormatException ignored) { }
        }
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(String.valueOf(req.get("active"))));
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Map<String, Object> toMap(MaterialType e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("code", e.getCode() == null ? "" : e.getCode());
        m.put("name", e.getName());
        m.put("description", e.getDescription() == null ? "" : e.getDescription());
        m.put("sortOrder", e.getSortOrder());
        m.put("active", e.isActive());
        return m;
    }
}
