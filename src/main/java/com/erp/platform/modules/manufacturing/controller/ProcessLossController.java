package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.ByproductType;
import com.erp.platform.modules.manufacturing.entity.ProcessLoss;
import com.erp.platform.modules.manufacturing.entity.ProcessLossItem;
import com.erp.platform.modules.agri.entity.WasteType;
import com.erp.platform.modules.agri.repository.ByproductTypeRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessLossRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessStepRepository;
import com.erp.platform.modules.agri.repository.WasteTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/manufacturing/process-losses")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Process Losses", description = "Process loss configuration per step")
public class ProcessLossController {

    private final ProcessLossRepository repo;
    private final ProcessStepRepository stepRepo;
    private final ByproductTypeRepository byproductTypeRepo;
    private final WasteTypeRepository wasteTypeRepo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all process losses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        UUID tenantId = tenantContext.current();
        List<Map<String, Object>> result = new ArrayList<>();
        repo.findByTenantIdAndDeletedAtIsNull(tenantId, org.springframework.data.domain.Pageable.unpaged())
                .forEach(pl -> result.add(toMap(pl)));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/by-step/{processStepId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process loss config for a process step, auto-loading all byproduct/waste types")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByStep(@PathVariable UUID processStepId) {
        UUID tenantId = tenantContext.current();
        Optional<ProcessLoss> existing = repo.findByTenantIdAndProcessStepIdAndDeletedAtIsNull(tenantId, processStepId);

        List<ByproductType> allByproducts = byproductTypeRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<WasteType> allWastes = wasteTypeRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("processStepId", processStepId);

        if (existing.isPresent()) {
            ProcessLoss pl = existing.get();
            response.put("id", pl.getId());
            response.put("processStepName", pl.getProcessStepName());

            Set<UUID> selectedByproducts = new HashSet<>();
            Set<UUID> selectedWastes = new HashSet<>();
            for (ProcessLossItem item : pl.getItems()) {
                if ("BYPRODUCT".equals(item.getItemType()) && item.isSelected()) selectedByproducts.add(item.getReferenceId());
                if ("WASTE".equals(item.getItemType()) && item.isSelected()) selectedWastes.add(item.getReferenceId());
            }

            List<Map<String, Object>> byproducts = new ArrayList<>();
            for (ByproductType b : allByproducts) {
                byproducts.add(Map.of("id", b.getId(), "name", b.getName(), "selected", selectedByproducts.contains(b.getId())));
            }
            List<Map<String, Object>> wastes = new ArrayList<>();
            for (WasteType w : allWastes) {
                wastes.add(Map.of("id", w.getId(), "name", w.getName(), "selected", selectedWastes.contains(w.getId())));
            }
            response.put("byproducts", byproducts);
            response.put("wastes", wastes);
        } else {
            List<Map<String, Object>> byproducts = new ArrayList<>();
            for (ByproductType b : allByproducts) {
                byproducts.add(Map.of("id", b.getId(), "name", b.getName(), "selected", false));
            }
            List<Map<String, Object>> wastes = new ArrayList<>();
            for (WasteType w : allWastes) {
                wastes.add(Map.of("id", w.getId(), "name", w.getName(), "selected", false));
            }
            response.put("byproducts", byproducts);
            response.put("wastes", wastes);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create or update process loss for a step")
    public ResponseEntity<ApiResponse<Map<String, Object>>> save(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        UUID processStepId = UUID.fromString(req.get("processStepId").toString());

        ProcessLoss pl = repo.findByTenantIdAndProcessStepIdAndDeletedAtIsNull(tenantId, processStepId)
                .orElseGet(() -> {
                    ProcessLoss newPl = new ProcessLoss();
                    newPl.setTenantId(tenantId);
                    newPl.setProcessStepId(processStepId);
                    return newPl;
                });

        pl.setProcessStepName((String) req.getOrDefault("processStepName", ""));
        pl.getItems().clear();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byproducts = (List<Map<String, Object>>) req.getOrDefault("byproducts", List.of());
        for (Map<String, Object> b : byproducts) {
            ProcessLossItem item = new ProcessLossItem();
            item.setTenantId(tenantId);
            item.setProcessLoss(pl);
            item.setItemType("BYPRODUCT");
            item.setReferenceId(UUID.fromString(b.get("id").toString()));
            item.setReferenceName((String) b.getOrDefault("name", ""));
            item.setSelected(Boolean.parseBoolean(b.getOrDefault("selected", "false").toString()));
            pl.getItems().add(item);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> wastes = (List<Map<String, Object>>) req.getOrDefault("wastes", List.of());
        for (Map<String, Object> w : wastes) {
            ProcessLossItem item = new ProcessLossItem();
            item.setTenantId(tenantId);
            item.setProcessLoss(pl);
            item.setItemType("WASTE");
            item.setReferenceId(UUID.fromString(w.get("id").toString()));
            item.setReferenceName((String) w.getOrDefault("name", ""));
            item.setSelected(Boolean.parseBoolean(w.getOrDefault("selected", "false").toString()));
            pl.getItems().add(item);
        }

        ProcessLoss saved = repo.save(pl);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(saved), "Process loss saved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete process loss")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessLoss pl = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Process loss not found: " + id));
        pl.setDeletedAt(LocalDateTime.now());
        repo.save(pl);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(ProcessLoss pl) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pl.getId());
        m.put("processStepId", pl.getProcessStepId());
        m.put("processStepName", pl.getProcessStepName() == null ? "" : pl.getProcessStepName());
        List<Map<String, Object>> byproducts = new ArrayList<>();
        List<Map<String, Object>> wastes = new ArrayList<>();
        for (ProcessLossItem item : pl.getItems()) {
            Map<String, Object> im = Map.of("id", item.getReferenceId(), "name", item.getReferenceName(), "selected", item.isSelected());
            if ("BYPRODUCT".equals(item.getItemType())) byproducts.add(im);
            else wastes.add(im);
        }
        m.put("byproducts", byproducts);
        m.put("wastes", wastes);
        return m;
    }
}

