package com.erp.platform.modules.intake.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.intake.repository.IntakeDocumentRepository;
import com.erp.platform.modules.intake.service.IntakeDocumentService;
import com.erp.platform.modules.inventory.entity.InventoryReceipt;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.InventoryReceiptRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intake/third-party-intake")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Intake - Third Party", description = "Third-party material intake")
public class ThirdPartyIntakeController {

    private static final String TYPE = "THIRD_PARTY";
    private final IntakeDocumentService service;
    private final StockLotRepository stockLotRepository;
    private final InventoryReceiptRepository inventoryReceiptRepository;
    private final IntakeDocumentRepository intakeDocumentRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List third-party intakes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(service.list(TYPE, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create third-party intake (auto-generates a lot number, posts stock + receipt)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
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
        upsertStockLot(tenantId, body, lotNo);
        createReceipt(tenantId, body, lotNo);
        try {
            createGoodsReceiptForIntake(tenantId, body, lotNo, PayloadUtils.uuid(result, "id"));
        } catch (Exception e) {
            // Never block the intake — the seed is physically in the godown either way.
            log.warn("Goods receipt generation skipped for third-party intake {}: {}", intakeSlip, e.getMessage());
        }
        result.putIfAbsent("intakeSlip", intakeSlip);
        result.putIfAbsent("lotNo", lotNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Intake saved (lot " + lotNo + ")"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Update third-party intake")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        String lotNo = PayloadUtils.str(body, "lotNo");
        if (lotNo == null || lotNo.isBlank()) {
            lotNo = generateLotNo(tenantId);
            body.put("lotNo", lotNo);
        }
        Map<String, Object> result = service.update(TYPE, id, body);
        upsertStockLot(tenantId, body, lotNo);   // keep on-hand stock in sync; receipt stays as the original audit record
        result.putIfAbsent("lotNo", lotNo);
        return ResponseEntity.ok(ApiResponse.success(result, "Intake updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete third-party intake")
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

    /** Sequential intake slip, e.g. TPI-2026-0007. */
    private String generateIntakeSlip(UUID tenantId) {
        long existing = intakeDocumentRepository.countByTenantIdAndTypeAndDeletedAtIsNull(tenantId, TYPE);
        return String.format("TPI-%d-%04d", LocalDate.now().getYear(), existing + 1);
    }

    /** Create (or, on edit, update) the on-hand stock lot so it shows in Physical Inventory. */
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
        lot.setNetName(firstNonBlank(PayloadUtils.str(body, "netName"), PayloadUtils.str(body, "netOrBin")));
        // The intake carries the crop group in cropName (from the PO's cropGroupName), so
        // fall back to it when an explicit cropGroupName isn't sent.
        lot.setCropGroupName(firstNonBlank(PayloadUtils.str(body, "cropGroupName"), PayloadUtils.str(body, "cropName")));
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
        lot.setSource("THIRD_PARTY_INTAKE");
        // Fixed "received" audit amount — captured on the intake and never reduced afterwards.
        lot.setOriginalNoOfBags(bags);
        lot.setOriginalQuantity(qty != null ? qty : BigDecimal.ZERO);
        stockLotRepository.save(lot);
    }

    /** Immutable RECEIPT audit record for the received lot (created once, at intake). */
    private void createReceipt(UUID tenantId, Map<String, Object> body, String lotNo) {
        BigDecimal qty = PayloadUtils.decimal(body, "totalQuantity");
        Integer bags = parseIntOrNull(PayloadUtils.str(body, "numberOfBags"));
        String unit = PayloadUtils.str(body, "unit");
        InventoryReceipt rcp = new InventoryReceipt();
        rcp.setTenantId(tenantId);
        rcp.setReceiptNumber(String.format("RCP-%05d", inventoryReceiptRepository.countByTenantId(tenantId) + 1));
        rcp.setLocation(firstNonBlank(PayloadUtils.str(body, "locationName"), PayloadUtils.str(body, "location")));
        rcp.setGodownId(PayloadUtils.uuid(body, "godownId"));
        rcp.setGodownName(PayloadUtils.str(body, "godownName"));
        // The intake form collects these; they used to be dropped, leaving every receipt
        // showing a blank supplier, PO and truck.
        rcp.setSupplierId(PayloadUtils.uuid(body, "supplierId"));
        rcp.setSupplierName(PayloadUtils.str(body, "supplierName"));
        rcp.setTruckNumber(PayloadUtils.str(body, "truckNo"));
        rcp.setInGatePass(PayloadUtils.str(body, "inwardGatePass"));
        PurchaseOrder po = resolvePurchaseOrder(tenantId, body);
        if (po != null) {
            rcp.setPurchaseOrderId(po.getId());
            rcp.setPoNo(po.getPoNumber());
        } else {
            rcp.setPoNo(PayloadUtils.str(body, "purchaseNo"));
        }
        rcp.setReceiptDate(LocalDate.now());
        rcp.setQuantity(qty != null ? qty : BigDecimal.ZERO);
        rcp.setUnit(unit != null && !unit.isBlank() ? unit : "KG");
        rcp.setLotNumber(lotNo);
        rcp.setPackDetails("Third Party Intake — lot " + lotNo
                + ", qty " + (qty != null ? qty.toPlainString() : "0") + ", bags " + (bags != null ? bags : 0)
                + firstNonBlankSuffix(" — ", PayloadUtils.str(body, "supplierName")));
        inventoryReceiptRepository.save(rcp);
    }

    /** The purchase order this intake is against. The form sends its number, not its id. */
    private PurchaseOrder resolvePurchaseOrder(UUID tenantId, Map<String, Object> body) {
        String poNo = PayloadUtils.str(body, "purchaseNo");
        if (poNo == null || poNo.isBlank()) return null;
        var byNumber = purchaseOrderRepository
                .findByTenantIdAndPoNumberIgnoreCaseAndDeletedAtIsNull(tenantId, poNo.trim());
        if (byNumber.isPresent()) return byNumber.get();
        // Older rows stored the order's id in the same field.
        try {
            return purchaseOrderRepository
                    .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, UUID.fromString(poNo.trim()))
                    .orElse(null);
        } catch (IllegalArgumentException notAUuid) {
            return null;
        }
    }

    /**
     * Raise a goods receipt for seed taken in against a purchase order, so the intake reaches the
     * three-way match alongside the PO and the vendor invoice. A walk-in intake with no purchase
     * order raises nothing — there is no order to match against.
     *
     * Stock is deliberately NOT posted here: upsertStockLot already put the lot on hand, and
     * posting again would double-count the intake.
     */
    private void createGoodsReceiptForIntake(UUID tenantId, Map<String, Object> body, String lotNo, UUID intakeId) {
        PurchaseOrder po = resolvePurchaseOrder(tenantId, body);
        if (po == null) return;
        if (intakeId != null
                && goodsReceiptRepository.existsByTenantIdAndSourceIntakeSlipIdAndDeletedAtIsNull(tenantId, intakeId)) {
            return;   // already raised for this intake
        }

        BigDecimal qty = PayloadUtils.decimal(body, "totalQuantity");
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) return;

        PurchaseOrderItem poLine = matchPoLine(po, body);
        if (poLine == null) return;   // nothing on the order corresponds to what arrived

        GoodsReceipt grn = new GoodsReceipt();
        grn.setTenantId(tenantId);
        grn.setGrnNumber("GRN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        grn.setPurchaseOrderId(po.getId());
        grn.setSourceIntakeSlipId(intakeId);
        grn.setVendorId(po.getVendorId() != null ? po.getVendorId() : PayloadUtils.uuid(body, "supplierId"));
        grn.setVendorName(firstNonBlank(po.getVendorName(), PayloadUtils.str(body, "supplierName")));
        grn.setWarehouseId(PayloadUtils.uuid(body, "godownId"));
        grn.setReceiptDate(LocalDate.now());
        grn.setVehicleNumber(PayloadUtils.str(body, "truckNo"));
        grn.setInGatePass(PayloadUtils.str(body, "inwardGatePass"));
        grn.setStatus("RECEIVED");
        grn.setNotes("Auto-generated from third-party intake — lot " + lotNo);

        GoodsReceiptItem gi = new GoodsReceiptItem();
        gi.setGoodsReceipt(grn);
        gi.setProductId(poLine.getProductId());
        gi.setProductName(firstNonBlank(poLine.getProductName(), poLine.getVarietyName()));
        gi.setOrderedQty(poLine.getQuantity() != null ? poLine.getQuantity() : BigDecimal.ZERO);
        gi.setReceivedQty(qty);
        gi.setAcceptedQty(qty);          // third-party intake records no rejection
        gi.setRejectedQty(BigDecimal.ZERO);
        gi.setBatchNumber(lotNo);
        grn.setItems(new ArrayList<>(List.of(gi)));
        goodsReceiptRepository.save(grn);

        // Roll the accepted quantity onto the PO line and move the order's status on.
        BigDecimal already = poLine.getReceivedQty() != null ? poLine.getReceivedQty() : BigDecimal.ZERO;
        poLine.setReceivedQty(already.add(qty));
        if (po.getStatus() != PurchaseOrder.POStatus.CANCELLED) {
            boolean allReceived = po.getItems().stream().allMatch(pi -> {
                BigDecimal received = pi.getReceivedQty() != null ? pi.getReceivedQty() : BigDecimal.ZERO;
                BigDecimal ordered = pi.getQuantity() != null ? pi.getQuantity() : BigDecimal.ZERO;
                return received.compareTo(ordered) >= 0;
            });
            po.setStatus(allReceived ? PurchaseOrder.POStatus.RECEIVED
                    : PurchaseOrder.POStatus.PARTIALLY_RECEIVED);
        }
        purchaseOrderRepository.save(po);
        log.info("GRN {} auto-raised from third-party intake lot {} against PO {}",
                grn.getGrnNumber(), lotNo, po.getPoNumber());
    }

    /**
     * A third-party intake carries a crop and variety rather than a product, so match the order
     * line on those. A single-line order needs no matching.
     */
    private PurchaseOrderItem matchPoLine(PurchaseOrder po, Map<String, Object> body) {
        List<PurchaseOrderItem> lines = po.getItems();
        if (lines == null || lines.isEmpty()) return null;

        String variety = PayloadUtils.str(body, "varietyName");
        if (variety != null && !variety.isBlank()) {
            for (PurchaseOrderItem line : lines) {
                if (variety.equalsIgnoreCase(line.getVarietyName())) return line;
            }
        }
        String crop = firstNonBlank(PayloadUtils.str(body, "cropGroupName"), PayloadUtils.str(body, "cropName"));
        if (crop != null && !crop.isBlank()) {
            for (PurchaseOrderItem line : lines) {
                if (crop.equalsIgnoreCase(line.getCropGroupName())) return line;
            }
        }
        return lines.size() == 1 ? lines.get(0) : null;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static String firstNonBlankSuffix(String prefix, String value) {
        return (value != null && !value.isBlank()) ? prefix + value : "";
    }
}
