package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.GroupRange;
import com.erp.platform.modules.accounting.repository.GroupRangeRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/group-ranges")
@RequiredArgsConstructor
@Tag(name = "Accounting - Group Ranges", description = "Account group code range definition")
public class GroupRangeController {

    private final GroupRangeRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List group ranges")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("groupCode"))).map(this::toMap))));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all group ranges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAll() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create group range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        GroupRange gr = new GroupRange();
        gr.setTenantId(tenantId);
        gr.setGroupCode((String) req.get("groupCode"));
        gr.setGroupName((String) req.get("groupName"));
        gr.setFromCode((String) req.get("fromCode"));
        gr.setToCode((String) req.get("toCode"));
        gr.setDescription((String) req.get("description"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(gr))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update group range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        GroupRange gr = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Group range not found: " + id));
        if (req.containsKey("groupCode")) gr.setGroupCode((String) req.get("groupCode"));
        if (req.containsKey("groupName")) gr.setGroupName((String) req.get("groupName"));
        if (req.containsKey("fromCode")) gr.setFromCode((String) req.get("fromCode"));
        if (req.containsKey("toCode")) gr.setToCode((String) req.get("toCode"));
        if (req.containsKey("description")) gr.setDescription((String) req.get("description"));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(gr))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete group range")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        GroupRange gr = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Group range not found: " + id));
        gr.setDeletedAt(LocalDateTime.now());
        repo.save(gr);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(GroupRange g) {
        return Map.of(
                "id", g.getId(),
                "groupCode", g.getGroupCode(),
                "groupName", g.getGroupName() == null ? "" : g.getGroupName(),
                "fromCode", g.getFromCode(),
                "toCode", g.getToCode(),
                "description", g.getDescription() == null ? "" : g.getDescription(),
                "createdAt", g.getCreatedAt() == null ? "" : g.getCreatedAt().toString()
        );
    }
}
