package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.repository.LotIssueDetailRepository;
import com.erp.platform.modules.agri.repository.LotProcessingDetailRepository;
import com.erp.platform.modules.dispatch.repository.DispatchChallanRepository;
import com.erp.platform.modules.intake.repository.IntakeSlipRepository;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.LocationTransferRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.manufacturing.repository.ProductionIntakeRepository;
import com.erp.platform.modules.purchase.repository.LotPurchaseReturnRepository;
import com.erp.platform.modules.quality.repository.SampleRepository;
import com.erp.platform.modules.sales.repository.SalesReturnRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive lot traceability — aggregates one lot's full history across modules:
 * header + Inventory / Quality / Processing / Purchase-Returns / Issues (and placeholder sections
 * for Production / Dispatches / Receipts / Sales-Returns which are surfaced as empty until wired).
 */
@RestController
@RequestMapping("/api/v1/inventory/lot-traceability")
@RequiredArgsConstructor
@Tag(name = "Inventory - Lot Traceability", description = "Full cross-module trace for a lot")
public class LotTraceabilityController {

    private final StockLotRepository stockLotRepository;
    private final SampleRepository sampleRepository;
    private final LotProcessingDetailRepository processingRepository;
    private final LotIssueDetailRepository issueRepository;
    private final LotPurchaseReturnRepository purchaseReturnRepository;
    private final ProductionIntakeRepository productionIntakeRepository;
    private final DispatchChallanRepository dispatchChallanRepository;
    private final LocationTransferRepository locationTransferRepository;
    private final IntakeSlipRepository intakeSlipRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Full trace for a lot number")
    public ResponseEntity<ApiResponse<Map<String, Object>>> trace(@RequestParam String lotNumber) {
        UUID t = tenantContext.current();
        Map<String, Object> out = new LinkedHashMap<>();

        // ── Inventory (StockLot) ──
        List<StockLot> lots = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(t, lotNumber);
        List<Map<String, Object>> inventory = new ArrayList<>();
        for (StockLot l : lots) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("location", l.getLocation());
            m.put("godown", l.getGodownName());
            m.put("net", l.getNetName());
            m.put("packingType", null);
            m.put("noOfBags", l.getNoOfBags());
            m.put("quantity", l.getQuantity());
            m.put("state", l.getMaterialState());
            m.put("type", l.getMaterialType());
            m.put("code", null);
            m.put("blockedReason", null);
            m.put("got", null);
            inventory.add(m);
        }
        out.put("inventory", inventory);

        // ── Header (from first StockLot / Sample) ──
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("lotNumber", lotNumber);
        StockLot first = lots.isEmpty() ? null : lots.get(0);
        var samples = sampleRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber);
        var firstSample = samples.isEmpty() ? null : samples.get(0);
        header.put("cropGroup", first != null ? first.getCropGroupName() : (firstSample != null ? firstSample.getCropGroupName() : null));
        header.put("crop", first != null ? first.getCropName() : (firstSample != null ? firstSample.getCropName() : null));
        header.put("variety", first != null ? first.getVarietyName() : (firstSample != null ? firstSample.getVarietyName() : null));
        header.put("productCode", first != null ? first.getProductName() : null);
        header.put("lotQualityStatus", firstSample != null ? firstSample.getResultStatus() : null);
        out.put("header", header);

        // ── Quality (Sample) ──
        List<Map<String, Object>> quality = new ArrayList<>();
        samples.forEach(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sampleCreatedDate", s.getSampleDate());
            m.put("batchNumber", s.getBatchNumber());
            m.put("sampleCode", s.getSampleNumber());
            m.put("sampleStatus", s.getStatus() != null ? s.getStatus().name() : null);
            m.put("materialState", s.getSeedStateName());
            m.put("location", s.getLocation());
            m.put("result", s.getResultStatus());
            quality.add(m);
        });
        out.put("quality", quality);

        // ── Processing (LotProcessingDetail) ──
        List<Map<String, Object>> processing = new ArrayList<>();
        processingRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("location", p.getLocation());
            m.put("process", p.getProcessType());
            m.put("jobNumber", p.getJobNumber());
            m.put("inputLots", p.getInputLotNumber());
            m.put("inputQuantity", p.getInputQuantityKgs());
            m.put("outputLots", p.getOutputLotNumber());
            m.put("outputQuantity", p.getOutputQuantityKgs());
            m.put("startDate", p.getProcessingDate());
            processing.add(m);
        });
        out.put("processing", processing);

        // ── Purchase Returns ──
        List<Map<String, Object>> purchaseReturns = new ArrayList<>();
        purchaseReturnRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("purchaseReturnNumber", r.getReturnNumber());
            m.put("purchaseReturnDate", r.getReturnDate());
            m.put("orderInfo", r.getCropName());
            m.put("quantity", r.getReturnQuantityKgs());
            purchaseReturns.add(m);
        });
        out.put("purchaseReturns", purchaseReturns);

        // ── Issues ──
        List<Map<String, Object>> issues = new ArrayList<>();
        java.math.BigDecimal totalIssued = java.math.BigDecimal.ZERO;
        for (var i : issueRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("issueNumber", i.getIssueNumber());
            m.put("issueDate", i.getIssueDate());
            m.put("location", i.getLocation());
            m.put("godown", i.getGodown());
            m.put("quantity", i.getQuantity());
            m.put("remarks", i.getIssueType());
            issues.add(m);
            if (i.getQuantity() != null) totalIssued = totalIssued.add(i.getQuantity());
        }
        out.put("issues", issues);

        // ── Production (intake from ProductionIntake; inspection has no lot-linked source) ──
        List<Map<String, Object>> prodIntake = new ArrayList<>();
        productionIntakeRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("intakeDate", p.getIntakeDate());
            m.put("intakeSlipNumber", p.getIntakeNumber());
            m.put("intakeType", p.getIntakeType());
            m.put("intakeQuantityKgs", p.getTotalQuantityKg() != null ? p.getTotalQuantityKg() : p.getQuantity());
            m.put("intakeMoisture", p.getMoisturePercent());
            prodIntake.add(m);
        });
        out.put("production", Map.of("intake", prodIntake, "inspection", List.of()));

        // ── Dispatches (sales ← DispatchChallan, stock transfer ← LocationTransfer) ──
        List<Map<String, Object>> salesDisp = new ArrayList<>();
        dispatchChallanRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.getChallanDate());
            m.put("dcNumber", d.getChallanNumber());
            m.put("customer", d.getCustomerName());
            m.put("quantity", d.getQuantityKgs());
            salesDisp.add(m);
        });
        List<Map<String, Object>> transferDisp = new ArrayList<>();
        locationTransferRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(x -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", x.getTransferDate());
            m.put("dcNumber", x.getTransferNumber());
            m.put("fromLocation", x.getFromLocationCode());
            m.put("toLocation", x.getToLocationCode());
            m.put("quantity", x.getQuantity());
            transferDisp.add(m);
        });
        out.put("dispatches", Map.of("sales", salesDisp, "stockTransfer", transferDisp, "freeSample", List.of()));

        // ── Receipts (from intake slips + lot-wise / opening intake stock lots) ──
        List<Map<String, Object>> receipts = new ArrayList<>();
        intakeSlipRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(t, lotNumber).forEach(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("receiptNumber", s.getSlipNumber());
            m.put("receivedDate", s.getReceiptDate());
            m.put("location", s.getLocation());
            m.put("godown", s.getGodownName());
            m.put("quantity", s.getTotalQuantity());
            receipts.add(m);
        });
        // Lot-wise / opening intake / process output writes a StockLot — surface it as a receipt.
        // Receipt is a FIXED audit of the ORIGINAL received amount (never the current stock): use the
        // immutable originalQuantity, falling back to current + total issued for pre-existing lots so
        // that Inventory = Receipts - Issues always holds.
        for (StockLot l : lots) {
            String src = l.getSource();
            if (src == null || !(src.contains("INTAKE") || src.contains("OPENING") || src.contains("PROCESS"))) continue;
            java.math.BigDecimal received = l.getOriginalQuantity() != null
                    ? l.getOriginalQuantity()
                    : (l.getQuantity() == null ? java.math.BigDecimal.ZERO : l.getQuantity()).add(totalIssued);
            Integer receivedBags = l.getOriginalNoOfBags() != null ? l.getOriginalNoOfBags() : l.getNoOfBags();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("receiptNumber", l.getLotNo());
            m.put("receivedDate", l.getCreatedAt() != null ? l.getCreatedAt().toLocalDate() : null);
            m.put("location", l.getLocation());
            m.put("godown", l.getGodownName());
            m.put("noOfBags", receivedBags);
            m.put("quantity", received);
            receipts.add(m);
        }
        out.put("receipts", receipts);

        // ── Sales Returns (returns whose items reference this lot) ──
        List<Map<String, Object>> salesReturns = new ArrayList<>();
        salesReturnRepository.findByLotNumber(t, lotNumber).forEach(r -> {
            var item = r.getItems().stream().filter(i -> lotNumber.equals(i.getLotNumber())).findFirst().orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("salesReturnDate", r.getReturnDate());
            m.put("salesReturnNumber", r.getReturnNumber());
            m.put("customer", r.getCustomerName());
            m.put("quantity", item != null ? item.getQuantity() : null);
            m.put("reason", r.getReason());
            salesReturns.add(m);
        });
        out.put("salesReturns", salesReturns);

        return ResponseEntity.ok(ApiResponse.success(out));
    }
}
