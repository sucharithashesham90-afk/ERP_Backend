package com.erp.platform.modules.intake.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.intake.service.IntakeDocumentService;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intake/lot-wise-intake")
@RequiredArgsConstructor
@Tag(name = "Intake - Lot Wise", description = "Lot-wise material intake")
public class LotWiseIntakeController {

    private static final String TYPE = "LOT_WISE";
    private final IntakeDocumentService service;
    private final StockLotRepository stockLotRepository;
    private final com.erp.platform.modules.intake.repository.IntakeDocumentRepository intakeDocumentRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List lot-wise intakes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(service.list(TYPE, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create lot-wise intake (auto-generates a lot number and posts it to stock)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        // Auto-generate the intake slip and lot number when not supplied.
        String intakeSlip = PayloadUtils.str(body, "intakeSlip");
        if (intakeSlip == null || intakeSlip.isBlank()) {
            intakeSlip = generateIntakeSlip(tenantId);
            body.put("intakeSlip", intakeSlip);
        }
        String lotNo = PayloadUtils.str(body, "lotNo");
        if (lotNo == null || lotNo.isBlank()) {
            lotNo = generateLotNo(tenantId);
            body.put("lotNo", lotNo);
        }
        Map<String, Object> result = service.create(TYPE, body);
        result.putIfAbsent("intakeSlip", intakeSlip);
        upsertStockLot(tenantId, body, lotNo);
        result.putIfAbsent("lotNo", lotNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Intake saved (lot " + lotNo + ")"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Update lot-wise intake")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        String lotNo = PayloadUtils.str(body, "lotNo");
        if (lotNo == null || lotNo.isBlank()) {
            lotNo = generateLotNo(tenantId);
            body.put("lotNo", lotNo);
        }
        Map<String, Object> result = service.update(TYPE, id, body);
        upsertStockLot(tenantId, body, lotNo);
        result.putIfAbsent("lotNo", lotNo);
        return ResponseEntity.ok(ApiResponse.success(result, "Intake updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete lot-wise intake")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(TYPE, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** Sequential lot number, e.g. LOT-2026-0007. */
    private String generateLotNo(UUID tenantId) {
        long existing = stockLotRepository
                .findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("LOT-%d-%04d", LocalDate.now().getYear(), existing + 1);
    }

    /** Sequential intake slip, e.g. IS-2026-0007. */
    private String generateIntakeSlip(UUID tenantId) {
        long existing = intakeDocumentRepository.countByTenantIdAndTypeAndDeletedAtIsNull(tenantId, TYPE);
        return String.format("IS-%d-%04d", LocalDate.now().getYear(), existing + 1);
    }

    /**
     * Create (or, on edit, update) the on-hand stock lot for this intake so it shows in Physical Inventory.
     */
    private void upsertStockLot(UUID tenantId, Map<String, Object> body, String lotNo) {
        StockLot lot = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNo)
                .stream().findFirst().orElseGet(StockLot::new);
        lot.setTenantId(tenantId);
        lot.setLotNo(lotNo);
        lot.setProductName(firstNonBlank(
                PayloadUtils.str(body, "cropName"),
                PayloadUtils.str(body, "varietyName"),
                PayloadUtils.str(body, "intakeSlip")));
        lot.setLocation(firstNonBlank(PayloadUtils.str(body, "locationName"), PayloadUtils.str(body, "location")));
        lot.setGodownId(PayloadUtils.uuid(body, "godownId"));
        lot.setGodownName(PayloadUtils.str(body, "godownName"));
        lot.setNetId(PayloadUtils.uuid(body, "netId"));
        lot.setNetName(PayloadUtils.str(body, "netName"));
        lot.setCropGroupName(PayloadUtils.str(body, "cropGroupName"));
        lot.setCropName(PayloadUtils.str(body, "cropName"));
        lot.setVarietyName(PayloadUtils.str(body, "varietyName"));
        lot.setMaterialState(PayloadUtils.str(body, "inputStateName"));
        lot.setMaterialGroupName(PayloadUtils.str(body, "bagTypeName"));
        Integer bags = parseIntOrNull(PayloadUtils.str(body, "numberOfBags"));
        lot.setNoOfBags(bags);
        BigDecimal qty = PayloadUtils.decimal(body, "totalQuantity");
        lot.setQuantity(qty != null ? qty : BigDecimal.ZERO);
        String unit = PayloadUtils.str(body, "unit");
        lot.setUnit(unit != null && !unit.isBlank() ? unit : "KG");
        lot.setSource("INTAKE");
        // Fixed "received" audit amount — captured on the intake and never reduced afterwards.
        lot.setOriginalNoOfBags(bags);
        lot.setOriginalQuantity(qty != null ? qty : BigDecimal.ZERO);
        stockLotRepository.save(lot);
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
