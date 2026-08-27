package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.inventory.entity.SeedConversion;
import com.erp.platform.modules.inventory.repository.SeedConversionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/seed-conversions")
@RequiredArgsConstructor
@Tag(name = "Inventory - Seed Conversions", description = "Seed/material conversion documents")
public class SeedConversionController {

    private final SeedConversionRepository repo;
    private final TenantContext tenantContext;

    /**
     * Conversions entered on this screen.
     *
     * <p>A process job moving a lot between states also writes a conversion, as an audit of what
     * became what. Those are worth keeping — a lot that changed state with no trace would be
     * worse — but they are not this screen's records, and mixed in they buried the handful of
     * entries an operator actually made under every stage transition the plant ran.
     *
     * <p>Pass {@code includeAutomatic=true} to see them, so the audit remains reachable rather
     * than merely hidden.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List seed conversions entered on this screen")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeAutomatic) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID tenantId = tenantContext.current();
        var result = (includeAutomatic
                ? repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : repo.findManual(tenantId, pageable)).map(this::toMap);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create seed conversion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        SeedConversion e = new SeedConversion();
        // Entered on the Seed Conversion screen, which is what that screen lists.
        e.setSource("MANUAL");
        e.setTenantId(tenantId);
        apply(e, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toMap(repo.save(e)), "Seed conversion saved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Delete seed conversion")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        SeedConversion e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Seed conversion not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(SeedConversion e, Map<String, Object> req) {
        UUID cropGroupId = PayloadUtils.uuid(req, "cropGroupId");
        UUID cropId = PayloadUtils.uuid(req, "cropId");
        UUID varietyId = PayloadUtils.uuid(req, "varietyId");
        if (cropGroupId == null) throw AppException.badRequest("Crop group is required");
        if (cropId == null) throw AppException.badRequest("Crop is required");
        if (varietyId == null) throw AppException.badRequest("Variety is required");
        if (PayloadUtils.uuid(req, "godownId") == null) throw AppException.badRequest("Godown is required");

        e.setCropGroupId(cropGroupId);
        e.setCropGroupName(PayloadUtils.str(req, "cropGroupName"));
        e.setCropId(cropId);
        e.setCropName(PayloadUtils.str(req, "cropName"));
        e.setVarietyId(varietyId);
        e.setVarietyName(PayloadUtils.str(req, "varietyName"));
        e.setMaterialTypeId(PayloadUtils.uuid(req, "materialTypeId"));
        e.setMaterialStateId(PayloadUtils.uuid(req, "materialStateId"));
        e.setLocation(PayloadUtils.str(req, "location"));
        e.setGodownId(PayloadUtils.uuid(req, "godownId"));
        LocalDate d = PayloadUtils.date(req, "conversionDate");
        e.setConversionDate(d != null ? d : LocalDate.now());
        e.setFromLotNo(PayloadUtils.str(req, "fromLotNo"));
        e.setFromNoOfBags(PayloadUtils.str(req, "fromNoOfBags"));
        e.setFromQuantity(PayloadUtils.str(req, "fromQuantity"));
        e.setFromUomId(PayloadUtils.uuid(req, "fromUomId"));
        e.setToNoOfBags(PayloadUtils.str(req, "toNoOfBags"));
        e.setToQuantity(PayloadUtils.str(req, "toQuantity"));
        e.setToUomId(PayloadUtils.uuid(req, "toUomId"));
        e.setNotes(PayloadUtils.str(req, "notes"));
    }

    private Map<String, Object> toMap(SeedConversion e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("cropGroupId", e.getCropGroupId());
        m.put("cropGroupName", e.getCropGroupName());
        m.put("cropId", e.getCropId());
        m.put("cropName", e.getCropName());
        m.put("varietyId", e.getVarietyId());
        m.put("varietyName", e.getVarietyName());
        m.put("materialTypeId", e.getMaterialTypeId());
        m.put("materialStateId", e.getMaterialStateId());
        m.put("location", e.getLocation());
        m.put("godownId", e.getGodownId());
        m.put("conversionDate", e.getConversionDate() == null ? null : e.getConversionDate().toString());
        m.put("fromLotNo", e.getFromLotNo());
        m.put("fromNoOfBags", e.getFromNoOfBags());
        m.put("fromQuantity", e.getFromQuantity());
        m.put("fromUomId", e.getFromUomId());
        m.put("toNoOfBags", e.getToNoOfBags());
        m.put("toQuantity", e.getToQuantity());
        m.put("toUomId", e.getToUomId());
        m.put("notes", e.getNotes());
        // Exposed so the screen can label an automatic row when one is shown deliberately.
        m.put("source", e.getSource() == null ? "MANUAL" : e.getSource());
        m.put("sourceReference", e.getSourceReference());
        m.put("createdAt", e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
        return m;
    }
}
