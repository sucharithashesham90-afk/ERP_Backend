package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.ProductionDelivery;
import com.erp.platform.modules.manufacturing.repository.ProductionDeliveryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manufacturing/production-deliveries")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Production Delivery", description = "Finished goods production delivery management")
public class ProductionDeliveryController {

    private final ProductionDeliveryRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List / search production deliveries")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) String jobNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deliveryDate"));
        boolean hasFilter = status != null || lotNumber != null || jobNumber != null || from != null || to != null;
        var result = hasFilter
                ? repo.findAll(ProductionDeliveryRepository.search(tenantId, status, lotNumber, jobNumber, from, to), pageable)
                : repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result.map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production delivery")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProductionDelivery pd = new ProductionDelivery();
        pd.setTenantId(tenantId);
        pd.setDeliveryNumber(generateNumber());
        String dateStr = (String) req.get("deliveryDate");
        pd.setDeliveryDate(dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now());
        pd.setLocationName((String) req.get("locationName"));
        pd.setDestinationName((String) req.get("destinationName"));
        pd.setLotNumber((String) req.get("lotNumber"));
        pd.setJobNumber((String) req.get("jobNumber"));
        if (req.get("productId") != null) pd.setProductId(UUID.fromString(req.get("productId").toString()));
        pd.setProductCode((String) req.get("productCode"));
        pd.setProductName((String) req.get("productName"));
        pd.setQuantity(new BigDecimal(req.getOrDefault("quantity", "0").toString()));
        pd.setUnit(req.getOrDefault("unit", "KG").toString());
        pd.setPackType((String) req.get("packType"));
        if (req.get("packSize") != null) pd.setPackSize(new BigDecimal(req.get("packSize").toString()));
        pd.setVehicleNumber((String) req.get("vehicleNumber"));
        pd.setDriverName((String) req.get("driverName"));
        pd.setStatus(req.getOrDefault("status", "DRAFT").toString());
        pd.setNotes((String) req.get("notes"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(pd))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production delivery")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProductionDelivery pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production delivery not found: " + id));
        if ("DELIVERED".equals(pd.getStatus())) throw AppException.badRequest("Cannot edit a delivered record");
        if (req.containsKey("deliveryDate")) pd.setDeliveryDate(LocalDate.parse(req.get("deliveryDate").toString()));
        if (req.containsKey("locationName")) pd.setLocationName((String) req.get("locationName"));
        if (req.containsKey("destinationName")) pd.setDestinationName((String) req.get("destinationName"));
        if (req.containsKey("lotNumber")) pd.setLotNumber((String) req.get("lotNumber"));
        if (req.containsKey("jobNumber")) pd.setJobNumber((String) req.get("jobNumber"));
        if (req.containsKey("productId") && req.get("productId") != null) pd.setProductId(UUID.fromString(req.get("productId").toString()));
        if (req.containsKey("productCode")) pd.setProductCode((String) req.get("productCode"));
        if (req.containsKey("productName")) pd.setProductName((String) req.get("productName"));
        if (req.containsKey("quantity")) pd.setQuantity(new BigDecimal(req.get("quantity").toString()));
        if (req.containsKey("unit")) pd.setUnit(req.get("unit").toString());
        if (req.containsKey("packType")) pd.setPackType((String) req.get("packType"));
        if (req.containsKey("packSize")) pd.setPackSize(new BigDecimal(req.get("packSize").toString()));
        if (req.containsKey("vehicleNumber")) pd.setVehicleNumber((String) req.get("vehicleNumber"));
        if (req.containsKey("driverName")) pd.setDriverName((String) req.get("driverName"));
        if (req.containsKey("status")) pd.setStatus(req.get("status").toString());
        if (req.containsKey("notes")) pd.setNotes((String) req.get("notes"));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(pd))));
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark production delivery as dispatched")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dispatch(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionDelivery pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production delivery not found: " + id));
        pd.setStatus("DISPATCHED");
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(pd))));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark production delivery as delivered")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deliver(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionDelivery pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production delivery not found: " + id));
        pd.setStatus("DELIVERED");
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(pd))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete production delivery")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionDelivery pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production delivery not found: " + id));
        if ("DELIVERED".equals(pd.getStatus())) throw AppException.badRequest("Cannot delete a delivered record");
        pd.setDeletedAt(LocalDateTime.now());
        repo.save(pd);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String generateNumber() {
        return "PD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Map<String, Object> toMap(ProductionDelivery d) {
        java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("deliveryNumber", d.getDeliveryNumber());
        m.put("deliveryDate", d.getDeliveryDate() == null ? "" : d.getDeliveryDate().toString());
        m.put("locationName", d.getLocationName() == null ? "" : d.getLocationName());
        m.put("destinationName", d.getDestinationName() == null ? "" : d.getDestinationName());
        m.put("lotNumber", d.getLotNumber() == null ? "" : d.getLotNumber());
        m.put("jobNumber", d.getJobNumber() == null ? "" : d.getJobNumber());
        m.put("productCode", d.getProductCode() == null ? "" : d.getProductCode());
        m.put("productName", d.getProductName() == null ? "" : d.getProductName());
        m.put("quantity", d.getQuantity());
        m.put("unit", d.getUnit() == null ? "KG" : d.getUnit());
        m.put("packType", d.getPackType() == null ? "" : d.getPackType());
        m.put("packSize", d.getPackSize());
        m.put("vehicleNumber", d.getVehicleNumber() == null ? "" : d.getVehicleNumber());
        m.put("driverName", d.getDriverName() == null ? "" : d.getDriverName());
        m.put("status", d.getStatus() == null ? "DRAFT" : d.getStatus());
        m.put("notes", d.getNotes() == null ? "" : d.getNotes());
        m.put("createdAt", d.getCreatedAt() == null ? "" : d.getCreatedAt().toString());
        return m;
    }
}
