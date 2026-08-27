package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.ProcessBOM;
import com.erp.platform.modules.manufacturing.entity.ProcessBOMItem;
import com.erp.platform.modules.manufacturing.repository.ProcessBOMRepository;
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
import java.util.*;

@RestController("mfgProcessBomController")
@RequestMapping("/api/v1/manufacturing/process-boms")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Process BOMs", description = "Process-step BOM assignments")
public class ProcessBomController {

    private final ProcessBOMRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process BOMs")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(
                        tenantId, PageRequest.of(page, size, Sort.by("name"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active process BOMs for dropdown")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())
                        .stream().map(this::toMap).toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process BOM by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable UUID id) {
        ProcessBOM e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process BOM not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(e)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process BOM")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProcessBOM bom = new ProcessBOM();
        bom.setTenantId(tenantId);
        applyFields(bom, req, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(bom)), "Process BOM created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process BOM")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProcessBOM bom = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Process BOM not found: " + id));
        applyFields(bom, req, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(bom)), "Process BOM updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete process BOM")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ProcessBOM bom = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process BOM not found: " + id));
        bom.setDeletedAt(LocalDateTime.now());
        repo.save(bom);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @SuppressWarnings("unchecked")
    private void applyFields(ProcessBOM bom, Map<String, Object> req, UUID tenantId) {
        if (req.containsKey("name")) bom.setName((String) req.get("name"));
        if (req.containsKey("description")) bom.setDescription((String) req.get("description"));
        if (req.containsKey("active")) bom.setActive(Boolean.parseBoolean(req.get("active").toString()));

        Object itemsRaw = req.get("items");
        if (itemsRaw instanceof List) {
            bom.getItems().clear();
            int seq = 0;
            for (Object obj : (List<?>) itemsRaw) {
                if (obj instanceof Map) {
                    Map<String, Object> i = (Map<String, Object>) obj;
                    ProcessBOMItem item = new ProcessBOMItem();
                    item.setTenantId(tenantId);
                    item.setProcessBom(bom);
                    if (i.get("processStepId") != null) item.setProcessStepId(UUID.fromString(i.get("processStepId").toString()));
                    item.setProcessStepName((String) i.get("processStepName"));
                    item.setProductCategoryName((String) i.get("productCategoryName"));
                    item.setProductName((String) i.get("productName"));
                    if (i.get("bomId") != null) item.setBomId(UUID.fromString(i.get("bomId").toString()));
                    item.setBomName((String) i.get("bomName"));
                    item.setSortOrder(seq++);
                    bom.getItems().add(item);
                }
            }
        }
    }

    private Map<String, Object> toMap(ProcessBOM bom) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", bom.getId());
        m.put("name", bom.getName() == null ? "" : bom.getName());
        m.put("description", bom.getDescription() == null ? "" : bom.getDescription());
        m.put("active", bom.isActive());
        m.put("createdAt", bom.getCreatedAt() == null ? "" : bom.getCreatedAt().toString());
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProcessBOMItem item : bom.getItems()) {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("processStepId", item.getProcessStepId());
            im.put("processStepName", item.getProcessStepName() == null ? "" : item.getProcessStepName());
            im.put("productCategoryName", item.getProductCategoryName() == null ? "" : item.getProductCategoryName());
            im.put("productName", item.getProductName() == null ? "" : item.getProductName());
            im.put("bomId", item.getBomId());
            im.put("bomName", item.getBomName() == null ? "" : item.getBomName());
            im.put("sortOrder", item.getSortOrder());
            items.add(im);
        }
        m.put("items", items);
        return m;
    }
}
