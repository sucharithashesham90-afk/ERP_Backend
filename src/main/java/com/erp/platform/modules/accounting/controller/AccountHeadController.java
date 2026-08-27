package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AccountHead;
import com.erp.platform.modules.accounting.repository.AccountHeadRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/account-heads")
@RequiredArgsConstructor
@Tag(name = "Accounting - Account Heads", description = "Account head definition (staff / customer / supplier)")
public class AccountHeadController {

    private final AccountHeadRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List account heads")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("accountHead"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active account heads, optionally filtered by type (STAFF/CUSTOMER/SUPPLIER)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive(
            @RequestParam(required = false) String type) {
        UUID tenantId = tenantContext.current();
        List<AccountHead> heads = (type == null || type.isBlank())
                ? repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                : repo.findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNull(tenantId, type);
        return ResponseEntity.ok(ApiResponse.success(
                heads.stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create account head")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String accountHead = str(req, "accountHead");
        if (accountHead == null || accountHead.isBlank())
            throw AppException.badRequest("Account head is required");
        AccountHead h = new AccountHead();
        h.setTenantId(tenantId);
        apply(h, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(h))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update account head")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        AccountHead h = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Account head not found: " + id));
        apply(h, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(h))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete account head")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        AccountHead h = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Account head not found: " + id));
        h.setDeletedAt(LocalDateTime.now());
        repo.save(h);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(AccountHead h, Map<String, Object> req) {
        if (req.containsKey("groupCode"))   h.setGroupCode(str(req, "groupCode"));
        if (req.containsKey("groupName"))   h.setGroupName(str(req, "groupName"));
        if (req.containsKey("accountHead")) h.setAccountHead(str(req, "accountHead"));
        if (req.containsKey("code"))        h.setCode(str(req, "code"));
        if (req.containsKey("type"))        h.setType(str(req, "type"));
        if (req.containsKey("active"))      h.setActive(Boolean.parseBoolean(req.get("active").toString()));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(AccountHead h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("groupCode", h.getGroupCode() == null ? "" : h.getGroupCode());
        m.put("groupName", h.getGroupName() == null ? "" : h.getGroupName());
        m.put("accountHead", h.getAccountHead());
        m.put("code", h.getCode() == null ? "" : h.getCode());
        m.put("type", h.getType() == null ? "" : h.getType());
        m.put("active", h.isActive());
        m.put("createdAt", h.getCreatedAt() == null ? "" : h.getCreatedAt().toString());
        return m;
    }
}
