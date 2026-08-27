package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.Sku;
import com.erp.platform.modules.sales.repository.SkuRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales/skus")
@RequiredArgsConstructor
@Tag(name = "Sales - SKUs", description = "Product SKU (packing) configuration")
public class SkuController {

    private final SkuRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List SKUs")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("name"))).map(this::toMap))));
    }

    /**
     * Active SKUs, optionally narrowed to one variety, crop or crop group.
     *
     * <p>The transfer and order screens pick what is moving from the SKU master rather than the raw
     * product list: a SKU is the pack a variety is actually sold in, so offering every product let
     * someone transfer a material that was never packed for that variety.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active SKUs, optionally filtered by variety / crop / crop group")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive(
            @RequestParam(required = false) UUID varietyId,
            @RequestParam(required = false) UUID cropId,
            @RequestParam(required = false) UUID cropGroupId) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId).stream()
                        .filter(sk -> varietyId == null || varietyId.equals(sk.getVarietyId()))
                        .filter(sk -> cropId == null || cropId.equals(sk.getCropId()))
                        .filter(sk -> cropGroupId == null || cropGroupId.equals(sk.getCropGroupId()))
                        .map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create SKU")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        if (str(req, "name") == null || str(req, "name").isBlank())
            throw AppException.badRequest("Name is required");
        Sku s = new Sku();
        s.setTenantId(tenantId);
        apply(s, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(s))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update SKU")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        Sku s = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("SKU not found: " + id));
        apply(s, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(s))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete SKU")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        Sku s = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("SKU not found: " + id));
        s.setDeletedAt(LocalDateTime.now());
        repo.save(s);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(Sku s, Map<String, Object> req) {
        if (req.containsKey("brandId") && str(req, "brandId") != null && !str(req, "brandId").isBlank())
            s.setBrandId(UUID.fromString(str(req, "brandId")));
        if (req.containsKey("brandName")) s.setBrandName(str(req, "brandName"));
        if (req.containsKey("packingMaterial")) s.setPackingMaterial(str(req, "packingMaterial"));
        if (req.containsKey("name")) s.setName(str(req, "name"));
        if (req.containsKey("cropGroupId")) s.setCropGroupId(uuid(req, "cropGroupId"));
        if (req.containsKey("cropGroupName")) s.setCropGroupName(str(req, "cropGroupName"));
        if (req.containsKey("cropId")) s.setCropId(uuid(req, "cropId"));
        if (req.containsKey("cropName")) s.setCropName(str(req, "cropName"));
        if (req.containsKey("varietyId")) s.setVarietyId(uuid(req, "varietyId"));
        if (req.containsKey("varietyName")) s.setVarietyName(str(req, "varietyName"));
        if (req.containsKey("packInfo")) s.setPackInfo(str(req, "packInfo"));
        if (req.containsKey("actualPackQtyKgs")) s.setActualPackQtyKgs(decimal(req, "actualPackQtyKgs"));
        if (req.containsKey("packMrp")) s.setPackMrp(decimal(req, "packMrp"));
        if (req.containsKey("description")) s.setDescription(str(req, "description"));
        if (req.containsKey("productCode")) s.setProductCode(str(req, "productCode"));
        if (req.containsKey("active")) s.setActive(Boolean.parseBoolean(req.get("active").toString()));
        if (req.containsKey("useSticker")) s.setUseSticker(Boolean.parseBoolean(req.get("useSticker").toString()));
        if (req.containsKey("stickerMaterial")) s.setStickerMaterial(str(req, "stickerMaterial"));
        if (req.containsKey("imageUrl")) s.setImageUrl(str(req, "imageUrl"));
        if (req.containsKey("packingMaterialsJson")) s.setPackingMaterialsJson(str(req, "packingMaterialsJson"));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private static UUID uuid(Map<String, Object> req, String key) {
        String v = str(req, key);
        if (v == null || v.isBlank()) return null;
        try { return UUID.fromString(v.trim()); } catch (IllegalArgumentException e) { return null; }
    }

    private static BigDecimal decimal(Map<String, Object> req, String key) {
        String s = str(req, key);
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Map<String, Object> toMap(Sku s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("brandId", s.getBrandId() == null ? "" : s.getBrandId().toString());
        m.put("brandName", s.getBrandName() == null ? "" : s.getBrandName());
        m.put("packingMaterial", s.getPackingMaterial() == null ? "" : s.getPackingMaterial());
        m.put("name", s.getName());
        m.put("cropGroupId", s.getCropGroupId() == null ? "" : s.getCropGroupId().toString());
        m.put("cropGroupName", s.getCropGroupName() == null ? "" : s.getCropGroupName());
        m.put("cropId", s.getCropId() == null ? "" : s.getCropId().toString());
        m.put("cropName", s.getCropName() == null ? "" : s.getCropName());
        m.put("varietyId", s.getVarietyId() == null ? "" : s.getVarietyId().toString());
        m.put("varietyName", s.getVarietyName() == null ? "" : s.getVarietyName());
        m.put("packInfo", s.getPackInfo() == null ? "" : s.getPackInfo());
        m.put("actualPackQtyKgs", s.getActualPackQtyKgs());
        m.put("packMrp", s.getPackMrp());
        m.put("description", s.getDescription() == null ? "" : s.getDescription());
        m.put("productCode", s.getProductCode() == null ? "" : s.getProductCode());
        m.put("active", s.isActive());
        m.put("useSticker", s.isUseSticker());
        m.put("stickerMaterial", s.getStickerMaterial() == null ? "" : s.getStickerMaterial());
        m.put("imageUrl", s.getImageUrl() == null ? "" : s.getImageUrl());
        m.put("packingMaterialsJson", s.getPackingMaterialsJson() == null ? "" : s.getPackingMaterialsJson());
        return m;
    }
}
