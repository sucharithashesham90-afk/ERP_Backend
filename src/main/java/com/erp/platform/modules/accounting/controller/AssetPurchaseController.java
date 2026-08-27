package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AssetPurchase;
import com.erp.platform.modules.accounting.entity.FixedAsset;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AssetPurchaseRepository;
import com.erp.platform.modules.accounting.service.FixedAssetService;
import com.erp.platform.modules.accounting.service.JournalEntryService;
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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Asset Purchase transaction. Saving registers a fixed asset and posts a balanced journal entry:
 * the selected fixed-asset ledger is CREDITED and the company ledger is DEBITED for the total.
 */
@RestController
@RequestMapping("/api/v1/accounting/asset-purchases")
@RequiredArgsConstructor
@Tag(name = "Accounting - Asset Purchase", description = "Purchase assets; posts to the asset register and ledgers")
public class AssetPurchaseController {

    private final AssetPurchaseRepository repo;
    private final FixedAssetService fixedAssetService;
    private final JournalEntryService journalEntryService;
    private final StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List asset purchases")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create an asset purchase (registers the asset and posts the ledgers)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();

        String name = str(req, "name");
        if (name == null || name.isBlank()) throw AppException.badRequest("Name is required");

        UUID assetLedgerId = uuid(req, "fixedAssetLedgerId");
        UUID companyLedgerId = uuid(req, "companyLedgerId");
        if (assetLedgerId == null) throw AppException.badRequest("Fixed Asset ledger is required (it is credited)");
        if (companyLedgerId == null) throw AppException.badRequest("Company ledger is required (it is debited)");

        int itemCount = intVal(req, "itemCount", 1);
        BigDecimal amountPerItem = decimal(req, "amountPerItem");
        BigDecimal total = amountPerItem.multiply(BigDecimal.valueOf(itemCount));
        if (total.compareTo(BigDecimal.ZERO) <= 0)
            throw AppException.badRequest("Purchase amount must be greater than zero");

        LocalDate entryDate = date(req, "entryDate");
        LocalDate purchaseDate = date(req, "purchaseDate");
        LocalDate postDate = entryDate != null ? entryDate : (purchaseDate != null ? purchaseDate : LocalDate.now());

        long seq = repo.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        String serial = String.format("AP-%05d", seq);

        // 1) Register the fixed asset (reflected in the asset register).
        FixedAsset fa = new FixedAsset();
        fa.setName(name);
        fa.setDescription(str(req, "narration"));
        fa.setPurchaseDate(purchaseDate != null ? purchaseDate : postDate);
        fa.setPurchaseCost(total);
        fa.setUsefulLifeYears(intVal(req, "lifeTimeYears", 5));
        fa.setDepreciationRate(decimal(req, "depreciationRate"));
        fa.setLocation(str(req, "location"));
        fa.setSerialNumber(serial);
        FixedAsset savedAsset = fixedAssetService.create(fa);

        // 2) Post the ledgers: credit the asset ledger, debit the company ledger.
        JournalEntry je = new JournalEntry();
        je.setEntryDate(postDate);
        je.setReferenceType("ASSET_PURCHASE");
        je.setReferenceNumber(serial);
        je.setDescription("Asset purchase: " + name);

        JournalEntryLine credit = new JournalEntryLine();
        credit.setAccountId(assetLedgerId);
        credit.setAccountCode(str(req, "fixedAssetLedgerCode"));
        credit.setAccountName(str(req, "fixedAssetLedgerName"));
        credit.setCreditAmount(total);
        credit.setDebitAmount(BigDecimal.ZERO);
        credit.setDescription("Asset purchase - credit");
        je.getLines().add(credit);

        JournalEntryLine debit = new JournalEntryLine();
        debit.setAccountId(companyLedgerId);
        debit.setAccountCode(str(req, "companyLedgerCode"));
        debit.setAccountName(str(req, "companyLedgerName"));
        debit.setDebitAmount(total);
        debit.setCreditAmount(BigDecimal.ZERO);
        debit.setDescription("Asset purchase - debit");
        je.getLines().add(debit);

        JournalEntry savedJe = journalEntryService.create(je);
        journalEntryService.post(savedJe.getId());

        // 3) Reflect in inventory: create an on-hand stock lot when a godown is chosen.
        StockLot lot = null;
        UUID godownId = uuid(req, "godownId");
        if (godownId != null) {
            lot = new StockLot();
            lot.setTenantId(tenantId);
            lot.setLotNo(serial);
            lot.setProductName(name);
            lot.setGodownId(godownId);
            lot.setGodownName(str(req, "godown"));
            String netId = str(req, "netId");
            if (netId != null && !netId.isBlank()) lot.setNetId(UUID.fromString(netId));
            lot.setNetName(str(req, "net"));
            lot.setMaterialGroupName(str(req, "materialGroup"));
            lot.setMaterialItemName(str(req, "materialItem"));
            lot.setNoOfBags(itemCount);
            lot.setQuantity(BigDecimal.valueOf(itemCount));
            lot.setUnit("NOS");
            lot.setSource("ASSET_PURCHASE");
            lot = stockLotRepository.save(lot);
        }

        // 4) Persist the purchase record linking asset + journal entry + stock lot.
        AssetPurchase ap = new AssetPurchase();
        ap.setTenantId(tenantId);
        ap.setSerialNumber(serial);
        ap.setName(name);
        ap.setAssetGroupCode(str(req, "assetGroupCode"));
        ap.setAssetGroupName(str(req, "assetGroupName"));
        ap.setFixedAssetLedgerId(assetLedgerId);
        ap.setFixedAssetLedgerCode(str(req, "fixedAssetLedgerCode"));
        ap.setFixedAssetLedgerName(str(req, "fixedAssetLedgerName"));
        ap.setCompanyLedgerId(companyLedgerId);
        ap.setCompanyLedgerCode(str(req, "companyLedgerCode"));
        ap.setCompanyLedgerName(str(req, "companyLedgerName"));
        ap.setLocation(str(req, "location"));
        ap.setGodown(str(req, "godown"));
        ap.setNet(str(req, "net"));
        ap.setMaterialGroup(str(req, "materialGroup"));
        ap.setMaterialItem(str(req, "materialItem"));
        ap.setItemCount(itemCount);
        ap.setAmountPerItem(amountPerItem);
        ap.setTotalAmount(total);
        ap.setNarration(str(req, "narration"));
        ap.setEntryDate(entryDate);
        ap.setPurchaseDate(purchaseDate);
        ap.setPutToUseDate(date(req, "putToUseDate"));
        ap.setModeOfPurchase(str(req, "modeOfPurchase"));
        ap.setDepreciationRate(decimal(req, "depreciationRate"));
        ap.setLifeTimeYears(intVal(req, "lifeTimeYears", 5));
        ap.setFixedAssetId(savedAsset.getId());
        ap.setJournalEntryId(savedJe.getId());
        ap.setJournalEntryNumber(savedJe.getEntryNumber());
        if (lot != null) {
            ap.setStockLotId(lot.getId());
            ap.setStockLotNo(lot.getLotNo());
        }

        String msg = lot != null
                ? "Asset purchased, registered, posted to ledgers and added to inventory"
                : "Asset purchased, registered and posted to ledgers";
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(ap)), msg));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete an asset purchase record (does not reverse the posted entry)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        AssetPurchase ap = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Asset purchase not found: " + id));
        ap.setDeletedAt(LocalDateTime.now());
        repo.save(ap);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ---- helpers ----
    private static String str(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? null : v.toString();
    }

    private static UUID uuid(Map<String, Object> r, String k) {
        String s = str(r, k);
        return (s == null || s.isBlank()) ? null : UUID.fromString(s);
    }

    private static int intVal(Map<String, Object> r, String k, int def) {
        String s = str(r, k);
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return def; }
    }

    private static BigDecimal decimal(Map<String, Object> r, String k) {
        String s = str(r, k);
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private static LocalDate date(Map<String, Object> r, String k) {
        String s = str(r, k);
        return (s == null || s.isBlank()) ? null : LocalDate.parse(s.substring(0, 10));
    }

    private Map<String, Object> toMap(AssetPurchase a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("serialNumber", a.getSerialNumber());
        m.put("name", a.getName());
        m.put("assetGroupName", a.getAssetGroupName() == null ? "" : a.getAssetGroupName());
        m.put("fixedAssetLedgerName", a.getFixedAssetLedgerName() == null ? "" : a.getFixedAssetLedgerName());
        m.put("companyLedgerName", a.getCompanyLedgerName() == null ? "" : a.getCompanyLedgerName());
        m.put("location", a.getLocation() == null ? "" : a.getLocation());
        m.put("itemCount", a.getItemCount());
        m.put("amountPerItem", a.getAmountPerItem());
        m.put("totalAmount", a.getTotalAmount());
        m.put("modeOfPurchase", a.getModeOfPurchase() == null ? "" : a.getModeOfPurchase());
        m.put("purchaseDate", a.getPurchaseDate() == null ? "" : a.getPurchaseDate().toString());
        m.put("journalEntryNumber", a.getJournalEntryNumber() == null ? "" : a.getJournalEntryNumber());
        m.put("stockLotNo", a.getStockLotNo() == null ? "" : a.getStockLotNo());
        m.put("createdAt", a.getCreatedAt() == null ? "" : a.getCreatedAt().toString());
        return m;
    }
}
