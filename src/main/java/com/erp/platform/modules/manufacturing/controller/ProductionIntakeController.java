package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.IntakeLine;
import com.erp.platform.modules.manufacturing.entity.IntakeWeightDeduction;
import com.erp.platform.modules.manufacturing.entity.ProductionIntake;
import com.erp.platform.modules.manufacturing.repository.ProductionIntakeRepository;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manufacturing/production-intakes")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Production Intake", description = "Production intake management (lot-wise, truck-wise, third-party)")
public class ProductionIntakeController {

    private final ProductionIntakeRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List / search production intakes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(required = false) String intakeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "intakeDate"));
        boolean hasFilter = intakeType != null || status != null || lotNumber != null || from != null || to != null;
        var result = hasFilter
                ? repo.findAll(ProductionIntakeRepository.search(tenantId, intakeType, status, lotNumber, from, to), pageable)
                : repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result.map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production intake")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProductionIntake pi = new ProductionIntake();
        pi.setTenantId(tenantId);
        pi.setIntakeNumber(generateNumber((String) req.getOrDefault("intakeType", "LOT")));
        applyFields(pi, req, tenantId, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(pi))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production intake")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ProductionIntake pi = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production intake not found: " + id));
        if ("VERIFIED".equals(pi.getStatus())) throw AppException.badRequest("Cannot edit a verified intake");
        applyFields(pi, req, tenantId, false);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(pi))));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify production intake")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionIntake pi = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production intake not found: " + id));
        pi.setStatus("VERIFIED");
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(pi))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete production intake")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionIntake pi = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production intake not found: " + id));
        if ("VERIFIED".equals(pi.getStatus())) throw AppException.badRequest("Cannot delete a verified intake");
        pi.setDeletedAt(LocalDateTime.now());
        repo.save(pi);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void applyFields(ProductionIntake pi, Map<String, Object> req, UUID tenantId, boolean isCreate) {
        if (isCreate || req.containsKey("intakeType"))
            pi.setIntakeType(req.getOrDefault("intakeType", "LOT_WISE").toString());
        String dateStr = (String) req.get("intakeDate");
        if (dateStr != null) pi.setIntakeDate(LocalDate.parse(dateStr));
        else if (isCreate)   pi.setIntakeDate(LocalDate.now());

        pi.setLocationName((String) req.get("locationName"));
        pi.setStorageLocationName((String) req.get("storageLocationName"));
        pi.setLotNumber((String) req.get("lotNumber"));
        pi.setVehicleNumber((String) req.get("vehicleNumber"));
        pi.setPartyName((String) req.get("partyName"));
        pi.setOrganizer((String) req.get("organizer"));
        pi.setSupervisor((String) req.get("supervisor"));
        pi.setInputType((String) req.get("inputType"));
        if (req.get("moisturePercent") != null)
            pi.setMoisturePercent(new BigDecimal(req.get("moisturePercent").toString()));
        pi.setInwardGatePassNumber((String) req.get("inwardGatePassNumber"));
        pi.setUnloadingSlipNumber((String) req.get("unloadingSlipNumber"));
        pi.setStackNumber((String) req.get("stackNumber"));
        if (req.get("weighbridgeQuantity") != null)
            pi.setWeighbridgeQuantity(new BigDecimal(req.get("weighbridgeQuantity").toString()));
        pi.setBagWeightType(req.getOrDefault("bagWeightType", "STANDARD").toString());
        pi.setProductCode((String) req.get("productCode"));
        pi.setProductName((String) req.get("productName"));
        if (req.get("quantity") != null)
            pi.setQuantity(new BigDecimal(req.get("quantity").toString()));
        else if (isCreate) pi.setQuantity(BigDecimal.ZERO);
        pi.setUnit(req.getOrDefault("unit", "KG").toString());
        pi.setPackType((String) req.get("packType"));
        if (req.get("packSize") != null)    pi.setPackSize(new BigDecimal(req.get("packSize").toString()));
        if (req.get("netBags") != null)     pi.setNetBags(Integer.parseInt(req.get("netBags").toString()));
        if (req.get("qtyPerBag") != null)   pi.setQtyPerBag(new BigDecimal(req.get("qtyPerBag").toString()));
        if (req.get("emptyBagsWeight") != null)
            pi.setEmptyBagsWeight(new BigDecimal(req.get("emptyBagsWeight").toString()));
        if (req.get("totalQuantityKg") != null)
            pi.setTotalQuantityKg(new BigDecimal(req.get("totalQuantityKg").toString()));
        if (req.get("netQuantityKg") != null)
            pi.setNetQuantityKg(new BigDecimal(req.get("netQuantityKg").toString()));
        pi.setStatus(req.getOrDefault("status", isCreate ? "DRAFT" : pi.getStatus()).toString());
        pi.setNotes((String) req.get("notes"));

        // New fields
        pi.setHamalContractor((String) req.get("hamalContractor"));
        if (req.get("rejectedBags") != null) pi.setRejectedBags(Integer.parseInt(req.get("rejectedBags").toString()));
        pi.setReturnType((String) req.get("returnType"));
        pi.setPurchaseOrderRef((String) req.get("purchaseOrderRef"));
        if (req.get("freightTotal") != null) pi.setFreightTotal(new BigDecimal(req.get("freightTotal").toString()));
        if (req.get("freightPaidAdv") != null) pi.setFreightPaidAdv(new BigDecimal(req.get("freightPaidAdv").toString()));
        if (req.get("freightToPay") != null) pi.setFreightToPay(new BigDecimal(req.get("freightToPay").toString()));
        String inGatePassDateStr = (String) req.get("inGatePassDate");
        if (inGatePassDateStr != null && !inGatePassDateStr.isEmpty()) pi.setInGatePassDate(LocalDate.parse(inGatePassDateStr));
        pi.setCustomerDcNumber((String) req.get("customerDcNumber"));
        String customerDcDateStr = (String) req.get("customerDcDate");
        if (customerDcDateStr != null && !customerDcDateStr.isEmpty()) pi.setCustomerDcDate(LocalDate.parse(customerDcDateStr));
        if (req.get("directReturn") != null) pi.setDirectReturn(Boolean.parseBoolean(req.get("directReturn").toString()));
        if (req.get("freeSampleReturn") != null) pi.setFreeSampleReturn(Boolean.parseBoolean(req.get("freeSampleReturn").toString()));
        pi.setTestsSelected((String) req.get("testsSelected"));
        pi.setTransport((String) req.get("transport"));

        // Weight deductions
        Object deductionsRaw = req.get("weightDeductions");
        if (deductionsRaw instanceof List) {
            pi.getWeightDeductions().clear();
            for (Object obj : (List<?>) deductionsRaw) {
                if (obj instanceof Map) {
                    Map<String, Object> d = (Map<String, Object>) obj;
                    IntakeWeightDeduction wd = new IntakeWeightDeduction();
                    wd.setTenantId(tenantId);
                    wd.setIntake(pi);
                    wd.setDeductionItem(d.getOrDefault("deductionItem", "").toString());
                    wd.setQuantityKg(d.get("quantityKg") != null
                            ? new BigDecimal(d.get("quantityKg").toString()) : BigDecimal.ZERO);
                    pi.getWeightDeductions().add(wd);
                }
            }
        }

        // Intake lines (multi-material per intake)
        Object linesRaw = req.get("intakeLines");
        if (linesRaw instanceof List) {
            pi.getIntakeLines().clear();
            int lineNum = 1;
            for (Object obj : (List<?>) linesRaw) {
                if (obj instanceof Map) {
                    Map<String, Object> l = (Map<String, Object>) obj;
                    IntakeLine line = new IntakeLine();
                    line.setTenantId(tenantId);
                    line.setIntake(pi);
                    line.setLineNumber(lineNum++);
                    line.setProductCategoryName((String) l.get("productCategoryName"));
                    line.setProductName((String) l.get("productName"));
                    line.setMaterialState((String) l.get("materialState"));
                    line.setStorageLocationName((String) l.get("storageLocationName"));
                    line.setNetBinName((String) l.get("netBinName"));
                    line.setPackType((String) l.get("packType"));
                    if (l.get("packSize") != null) line.setPackSize(new BigDecimal(l.get("packSize").toString()));
                    if (l.get("qtyPerBag") != null) line.setQtyPerBag(new BigDecimal(l.get("qtyPerBag").toString()));
                    if (l.get("numberOfBags") != null) line.setNumberOfBags(Integer.parseInt(l.get("numberOfBags").toString()));
                    if (l.get("totalQuantity") != null) line.setTotalQuantity(new BigDecimal(l.get("totalQuantity").toString()));
                    if (l.get("moisturePercent") != null) line.setMoisturePercent(new BigDecimal(l.get("moisturePercent").toString()));
                    if (l.get("emptyBagsWeight") != null) line.setEmptyBagsWeight(new BigDecimal(l.get("emptyBagsWeight").toString()));
                    line.setRemarks((String) l.get("remarks"));
                    line.setTestsSelected((String) l.get("testsSelected"));
                    line.setAutoSlipNumber((String) l.get("autoSlipNumber"));
                    line.setLotNumber((String) l.get("lotNumber"));
                    pi.getIntakeLines().add(line);
                }
            }
        }
    }

    private String generateNumber(String type) {
        String prefix;
        if (type.startsWith("TRUCK")) prefix = "TRK";
        else if (type.startsWith("THIRD")) prefix = "TPI";
        else if (type.startsWith("SALES")) prefix = "SRI";
        else prefix = "LWI";
        return prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Map<String, Object> toMap(ProductionIntake p) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("intakeNumber", p.getIntakeNumber());
        m.put("intakeType", p.getIntakeType() == null ? "" : p.getIntakeType());
        m.put("intakeDate", p.getIntakeDate() == null ? "" : p.getIntakeDate().toString());
        m.put("locationName", p.getLocationName() == null ? "" : p.getLocationName());
        m.put("storageLocationName", p.getStorageLocationName() == null ? "" : p.getStorageLocationName());
        m.put("lotNumber", p.getLotNumber() == null ? "" : p.getLotNumber());
        m.put("vehicleNumber", p.getVehicleNumber() == null ? "" : p.getVehicleNumber());
        m.put("partyName", p.getPartyName() == null ? "" : p.getPartyName());
        m.put("organizer", p.getOrganizer() == null ? "" : p.getOrganizer());
        m.put("supervisor", p.getSupervisor() == null ? "" : p.getSupervisor());
        m.put("inputType", p.getInputType() == null ? "" : p.getInputType());
        m.put("moisturePercent", p.getMoisturePercent());
        m.put("inwardGatePassNumber", p.getInwardGatePassNumber() == null ? "" : p.getInwardGatePassNumber());
        m.put("unloadingSlipNumber", p.getUnloadingSlipNumber() == null ? "" : p.getUnloadingSlipNumber());
        m.put("stackNumber", p.getStackNumber() == null ? "" : p.getStackNumber());
        m.put("weighbridgeQuantity", p.getWeighbridgeQuantity());
        m.put("bagWeightType", p.getBagWeightType() == null ? "STANDARD" : p.getBagWeightType());
        m.put("productCode", p.getProductCode() == null ? "" : p.getProductCode());
        m.put("productName", p.getProductName() == null ? "" : p.getProductName());
        m.put("quantity", p.getQuantity());
        m.put("unit", p.getUnit() == null ? "KG" : p.getUnit());
        m.put("packType", p.getPackType() == null ? "" : p.getPackType());
        m.put("packSize", p.getPackSize());
        m.put("netBags", p.getNetBags());
        m.put("qtyPerBag", p.getQtyPerBag());
        m.put("emptyBagsWeight", p.getEmptyBagsWeight());
        m.put("totalQuantityKg", p.getTotalQuantityKg());
        m.put("netQuantityKg", p.getNetQuantityKg());
        m.put("status", p.getStatus() == null ? "DRAFT" : p.getStatus());
        m.put("notes", p.getNotes() == null ? "" : p.getNotes());
        m.put("hamalContractor", p.getHamalContractor() == null ? "" : p.getHamalContractor());
        m.put("rejectedBags", p.getRejectedBags());
        m.put("returnType", p.getReturnType() == null ? "" : p.getReturnType());
        m.put("purchaseOrderRef", p.getPurchaseOrderRef() == null ? "" : p.getPurchaseOrderRef());
        m.put("freightTotal", p.getFreightTotal());
        m.put("freightPaidAdv", p.getFreightPaidAdv());
        m.put("freightToPay", p.getFreightToPay());
        m.put("inGatePassDate", p.getInGatePassDate() == null ? "" : p.getInGatePassDate().toString());
        m.put("customerDcNumber", p.getCustomerDcNumber() == null ? "" : p.getCustomerDcNumber());
        m.put("customerDcDate", p.getCustomerDcDate() == null ? "" : p.getCustomerDcDate().toString());
        m.put("directReturn", p.isDirectReturn());
        m.put("freeSampleReturn", p.isFreeSampleReturn());
        m.put("testsSelected", p.getTestsSelected() == null ? "" : p.getTestsSelected());
        m.put("transport", p.getTransport() == null ? "" : p.getTransport());
        m.put("createdAt", p.getCreatedAt() == null ? "" : p.getCreatedAt().toString());
        // Intake lines
        List<Map<String, Object>> lines = new ArrayList<>();
        if (p.getIntakeLines() != null) {
            for (IntakeLine il : p.getIntakeLines()) {
                LinkedHashMap<String, Object> lm = new LinkedHashMap<>();
                lm.put("id", il.getId());
                lm.put("lineNumber", il.getLineNumber());
                lm.put("productCategoryName", il.getProductCategoryName() == null ? "" : il.getProductCategoryName());
                lm.put("productName", il.getProductName() == null ? "" : il.getProductName());
                lm.put("materialState", il.getMaterialState() == null ? "" : il.getMaterialState());
                lm.put("storageLocationName", il.getStorageLocationName() == null ? "" : il.getStorageLocationName());
                lm.put("netBinName", il.getNetBinName() == null ? "" : il.getNetBinName());
                lm.put("packType", il.getPackType() == null ? "" : il.getPackType());
                lm.put("packSize", il.getPackSize());
                lm.put("qtyPerBag", il.getQtyPerBag());
                lm.put("numberOfBags", il.getNumberOfBags());
                lm.put("totalQuantity", il.getTotalQuantity());
                lm.put("moisturePercent", il.getMoisturePercent());
                lm.put("emptyBagsWeight", il.getEmptyBagsWeight());
                lm.put("remarks", il.getRemarks() == null ? "" : il.getRemarks());
                lm.put("testsSelected", il.getTestsSelected() == null ? "" : il.getTestsSelected());
                lm.put("autoSlipNumber", il.getAutoSlipNumber() == null ? "" : il.getAutoSlipNumber());
                lm.put("lotNumber", il.getLotNumber() == null ? "" : il.getLotNumber());
                lines.add(lm);
            }
        }
        m.put("intakeLines", lines);
        // Weight deductions
        List<Map<String, Object>> deductions = new ArrayList<>();
        if (p.getWeightDeductions() != null) {
            for (IntakeWeightDeduction wd : p.getWeightDeductions()) {
                LinkedHashMap<String, Object> dm = new LinkedHashMap<>();
                dm.put("id", wd.getId());
                dm.put("deductionItem", wd.getDeductionItem());
                dm.put("quantityKg", wd.getQuantityKg());
                deductions.add(dm);
            }
        }
        m.put("weightDeductions", deductions);
        BigDecimal totalDed = p.getWeightDeductions() == null ? BigDecimal.ZERO
                : p.getWeightDeductions().stream()
                        .map(IntakeWeightDeduction::getQuantityKg)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("totalWeightDeductions", totalDed);
        return m;
    }
}
