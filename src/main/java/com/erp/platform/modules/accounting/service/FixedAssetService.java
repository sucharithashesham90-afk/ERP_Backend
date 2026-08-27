package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AssetGroup;
import com.erp.platform.modules.accounting.entity.AssetMaintenance;
import com.erp.platform.modules.accounting.entity.DepreciationEntry;
import com.erp.platform.modules.accounting.entity.FixedAsset;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.AssetGroupRepository;
import com.erp.platform.modules.accounting.repository.AssetMaintenanceRepository;
import com.erp.platform.modules.accounting.repository.DepreciationEntryRepository;
import com.erp.platform.modules.accounting.repository.FixedAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FixedAssetService {

    private final AssetGroupRepository assetGroupRepo;
    private final FixedAssetRepository fixedAssetRepo;
    private final DepreciationEntryRepository depreciationEntryRepo;
    private final AssetMaintenanceRepository maintenanceRepo;
    private final AccountRepository accountRepo;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    // ---- Asset Groups ----

    public PageResponse<AssetGroup> listGroups(Pageable pageable) {
        return PageResponse.of(assetGroupRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    @Transactional
    public AssetGroup createGroup(AssetGroup req) {
        req.setTenantId(tenantContext.current());
        return assetGroupRepo.save(req);
    }

    @Transactional
    public AssetGroup updateGroup(UUID id, AssetGroup req) {
        AssetGroup e = assetGroupRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Asset group not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setDepreciationMethod(req.getDepreciationMethod());
        e.setUsefulLifeYears(req.getUsefulLifeYears());
        e.setDepreciationRate(req.getDepreciationRate());
        e.setAssetLedgerId(req.getAssetLedgerId());
        e.setDepreciationLedgerId(req.getDepreciationLedgerId());
        e.setActive(req.isActive());
        return assetGroupRepo.save(e);
    }

    @Transactional
    public void deleteGroup(UUID id) {
        AssetGroup e = assetGroupRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Asset group not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        assetGroupRepo.save(e);
    }

    public List<AssetGroup> getAllActiveGroups() {
        return assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(tenantContext.current(), true);
    }

    // ---- Fixed Assets ----

    public PageResponse<FixedAsset> list(String status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (status != null && !status.isBlank()) {
            FixedAsset.AssetStatus assetStatus = FixedAsset.AssetStatus.valueOf(status.toUpperCase());
            return PageResponse.of(fixedAssetRepo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, assetStatus, pageable));
        }
        return PageResponse.of(fixedAssetRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public FixedAsset getById(UUID id) {
        return fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Fixed asset not found: " + id));
    }

    @Transactional
    public FixedAsset create(FixedAsset req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);
        req.setAssetCode("ASSET-" + System.currentTimeMillis());
        if (req.getPurchaseCost() == null) req.setPurchaseCost(BigDecimal.ZERO);
        if (req.getSalvageValue() == null) req.setSalvageValue(BigDecimal.ZERO);
        req.setCurrentBookValue(req.getPurchaseCost());
        req.setAccumulatedDepreciation(BigDecimal.ZERO);
        req.setStatus(FixedAsset.AssetStatus.ACTIVE);
        return fixedAssetRepo.save(req);
    }

    @Transactional
    public FixedAsset update(UUID id, FixedAsset req) {
        FixedAsset e = fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Fixed asset not found: " + id));
        e.setName(req.getName());
        e.setAssetGroup(req.getAssetGroup());
        e.setDescription(req.getDescription());
        e.setPurchaseDate(req.getPurchaseDate());
        e.setPurchaseCost(req.getPurchaseCost());
        e.setSalvageValue(req.getSalvageValue());
        e.setUsefulLifeYears(req.getUsefulLifeYears());
        e.setDepreciationMethod(req.getDepreciationMethod());
        e.setDepreciationRate(req.getDepreciationRate());
        e.setLocation(req.getLocation());
        e.setAssignedTo(req.getAssignedTo());
        e.setSerialNumber(req.getSerialNumber());
        e.setActive(req.isActive());
        return fixedAssetRepo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        FixedAsset e = fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Fixed asset not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        fixedAssetRepo.save(e);
    }

    @Transactional
    public FixedAsset dispose(UUID assetId, LocalDate disposalDate, BigDecimal disposalAmount) {
        FixedAsset e = fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), assetId)
                .orElseThrow(() -> AppException.notFound("Fixed asset not found: " + assetId));
        e.setStatus(FixedAsset.AssetStatus.DISPOSED);
        e.setDisposalDate(disposalDate);
        e.setDisposalAmount(disposalAmount);
        e.setActive(false);
        return fixedAssetRepo.save(e);
    }

    @Transactional
    public List<DepreciationEntry> generateDepreciationSchedule(UUID assetId) {
        FixedAsset asset = fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), assetId)
                .orElseThrow(() -> AppException.notFound("Fixed asset not found: " + assetId));

        // Delete existing unposted entries
        List<DepreciationEntry> existing = depreciationEntryRepo.findByAssetIdOrderByDepreciationDateAsc(assetId);
        existing.stream().filter(e -> !e.isPosted()).forEach(e -> depreciationEntryRepo.delete(e));

        List<DepreciationEntry> schedule = new ArrayList<>();
        LocalDate startDate = asset.getPurchaseDate() != null ? asset.getPurchaseDate() : LocalDate.now();
        BigDecimal cost = asset.getPurchaseCost() != null ? asset.getPurchaseCost() : BigDecimal.ZERO;
        BigDecimal salvage = asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO;
        int totalMonths = asset.getUsefulLifeYears() * 12;
        BigDecimal currentValue = cost;

        for (int i = 0; i < totalMonths; i++) {
            LocalDate depDate = startDate.plusMonths(i + 1).withDayOfMonth(1).minusDays(1);
            BigDecimal depAmount;
            if (asset.getDepreciationMethod() == AssetGroup.DepreciationMethod.STRAIGHT_LINE) {
                depAmount = cost.subtract(salvage)
                        .divide(BigDecimal.valueOf(totalMonths), 2, RoundingMode.HALF_UP);
            } else {
                // WDV
                BigDecimal rate = asset.getDepreciationRate() != null ? asset.getDepreciationRate() : BigDecimal.ZERO;
                depAmount = currentValue.multiply(rate)
                        .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
            }
            BigDecimal closing = currentValue.subtract(depAmount);
            if (closing.compareTo(salvage) < 0) {
                depAmount = currentValue.subtract(salvage);
                closing = salvage;
            }
            if (depAmount.compareTo(BigDecimal.ZERO) <= 0) break;

            DepreciationEntry entry = new DepreciationEntry();
            entry.setTenantId(tenantContext.current());
            entry.setAsset(asset);
            entry.setPeriodYear(depDate.getYear());
            entry.setPeriodMonth(depDate.getMonthValue());
            entry.setDepreciationDate(depDate);
            entry.setOpeningValue(currentValue);
            entry.setDepreciationAmount(depAmount);
            entry.setClosingValue(closing);
            entry.setPosted(false);
            schedule.add(depreciationEntryRepo.save(entry));
            currentValue = closing;
        }
        return schedule;
    }

    public List<DepreciationEntry> getDepreciationSchedule(UUID assetId) {
        getById(assetId); // verify access
        return depreciationEntryRepo.findByAssetIdOrderByDepreciationDateAsc(assetId);
    }

    public List<DepreciationEntry> getPendingDepreciations() {
        return depreciationEntryRepo.findByTenantIdAndPostedAndDepreciationDateBefore(
                tenantContext.current(), false, LocalDate.now().plusDays(1));
    }

    @Transactional
    public DepreciationEntry postDepreciation(UUID entryId) {
        UUID tenantId = tenantContext.current();
        DepreciationEntry entry = depreciationEntryRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, entryId)
                .orElseThrow(() -> AppException.notFound("Depreciation entry not found: " + entryId));
        if (entry.isPosted()) {
            throw AppException.badRequest("Depreciation entry is already posted");
        }

        FixedAsset asset = entry.getAsset();
        BigDecimal depAmount = entry.getDepreciationAmount();

        // Determine depreciation expense and accumulated depreciation accounts
        // Prefer ledger IDs from the asset group; fall back to subtype lookup
        Account expenseAcct  = resolveAccount(tenantId, asset.getAssetGroup() != null ? asset.getAssetGroup().getDepreciationLedgerId() : null, "DEPRECIATION_EXPENSE");
        Account accumAcct    = resolveAccount(tenantId, null, "ACCUMULATED_DEPRECIATION");

        if (expenseAcct != null && accumAcct != null) {
            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("DEPRECIATION");
            je.setReferenceId(asset.getId());
            je.setReferenceNumber(asset.getAssetCode());
            je.setEntryDate(entry.getDepreciationDate());
            je.setDescription("Depreciation: " + asset.getName() + " " + entry.getPeriodYear() + "/" + entry.getPeriodMonth());

            JournalEntryLine drLine = new JournalEntryLine();
            drLine.setAccountId(expenseAcct.getId());
            drLine.setAccountCode(expenseAcct.getCode());
            drLine.setAccountName(expenseAcct.getName());
            drLine.setDebitAmount(depAmount);
            drLine.setCreditAmount(BigDecimal.ZERO);
            drLine.setDescription("Depreciation expense — " + asset.getName());

            JournalEntryLine crLine = new JournalEntryLine();
            crLine.setAccountId(accumAcct.getId());
            crLine.setAccountCode(accumAcct.getCode());
            crLine.setAccountName(accumAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO);
            crLine.setCreditAmount(depAmount);
            crLine.setDescription("Accumulated depreciation — " + asset.getName());

            je.getLines().add(drLine);
            je.getLines().add(crLine);
            journalEntryService.create(je);
        } else {
            log.warn("Depreciation journal skipped for asset {}: DEPRECIATION_EXPENSE or ACCUMULATED_DEPRECIATION account not configured", asset.getAssetCode());
        }

        // Update asset book value and accumulated depreciation
        BigDecimal newAccumulated = (asset.getAccumulatedDepreciation() != null ? asset.getAccumulatedDepreciation() : BigDecimal.ZERO).add(depAmount);
        BigDecimal newBookValue = (asset.getCurrentBookValue() != null ? asset.getCurrentBookValue() : asset.getPurchaseCost()).subtract(depAmount).max(BigDecimal.ZERO);
        asset.setAccumulatedDepreciation(newAccumulated);
        asset.setCurrentBookValue(newBookValue);
        fixedAssetRepo.save(asset);

        entry.setPosted(true);
        DepreciationEntry saved = depreciationEntryRepo.save(entry);
        log.info("Depreciation posted: asset={}, amount={}, newBookValue={}", asset.getAssetCode(), depAmount, newBookValue);
        return saved;
    }

    private Account resolveAccount(UUID tenantId, UUID ledgerId, String subType) {
        if (ledgerId != null) {
            return accountRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ledgerId).orElse(null);
        }
        List<Account> accounts = accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, subType);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    // ─── Asset Register Report ─────────────────────────────────────────────────

    public Map<String, Object> getAssetRegister(String status, String location) {
        UUID tenantId = tenantContext.current();
        List<FixedAsset> assets;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            assets = fixedAssetRepo.findAllByTenantIdAndStatusAndDeletedAtIsNull(
                    tenantId, FixedAsset.AssetStatus.valueOf(status.toUpperCase()));
        } else {
            assets = fixedAssetRepo.findAllByTenantIdAndDeletedAtIsNull(tenantId);
        }
        if (location != null && !location.isBlank()) {
            assets = assets.stream()
                    .filter(a -> location.equalsIgnoreCase(a.getLocation()))
                    .collect(Collectors.toList());
        }

        // Group by asset group name (preserve insertion order)
        Map<String, List<FixedAsset>> byGroup = new LinkedHashMap<>();
        for (FixedAsset a : assets) {
            String grp = (a.getAssetGroup() != null && a.getAssetGroup().getName() != null)
                    ? a.getAssetGroup().getName() : "Ungrouped";
            byGroup.computeIfAbsent(grp, k -> new ArrayList<>()).add(a);
        }

        BigDecimal totalCost      = BigDecimal.ZERO;
        BigDecimal totalAccumDep  = BigDecimal.ZERO;
        BigDecimal totalBookValue = BigDecimal.ZERO;

        List<Map<String, Object>> groups = new ArrayList<>();
        for (Map.Entry<String, List<FixedAsset>> entry : byGroup.entrySet()) {
            BigDecimal gCost = BigDecimal.ZERO, gAccum = BigDecimal.ZERO, gBook = BigDecimal.ZERO;
            List<Map<String, Object>> rows = new ArrayList<>();
            for (FixedAsset a : entry.getValue()) {
                BigDecimal cost  = nvl(a.getPurchaseCost());
                BigDecimal accum = nvl(a.getAccumulatedDepreciation());
                BigDecimal book  = nvl(a.getCurrentBookValue());
                gCost  = gCost.add(cost);
                gAccum = gAccum.add(accum);
                gBook  = gBook.add(book);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id",                    a.getId());
                row.put("assetCode",             a.getAssetCode());
                row.put("name",                  a.getName());
                row.put("serialNumber",          a.getSerialNumber());
                row.put("location",              a.getLocation());
                row.put("assignedTo",            a.getAssignedTo());
                row.put("purchaseDate",          a.getPurchaseDate() != null ? a.getPurchaseDate().toString() : "");
                row.put("purchaseCost",          cost);
                row.put("accumulatedDepreciation", accum);
                row.put("currentBookValue",      book);
                row.put("salvageValue",          nvl(a.getSalvageValue()));
                row.put("status",                a.getStatus() != null ? a.getStatus().name() : "");
                row.put("usefulLifeYears",       a.getUsefulLifeYears());
                row.put("depreciationMethod",    a.getDepreciationMethod() != null ? a.getDepreciationMethod().name() : "");
                rows.add(row);
            }
            totalCost      = totalCost.add(gCost);
            totalAccumDep  = totalAccumDep.add(gAccum);
            totalBookValue = totalBookValue.add(gBook);

            FixedAsset sample = entry.getValue().get(0);
            Map<String, Object> gm = new LinkedHashMap<>();
            gm.put("groupName",              entry.getKey());
            gm.put("groupCode",              sample.getAssetGroup() != null ? sample.getAssetGroup().getCode() : "");
            gm.put("count",                  entry.getValue().size());
            gm.put("totalCost",              gCost);
            gm.put("totalAccumDepreciation", gAccum);
            gm.put("totalBookValue",         gBook);
            gm.put("assets",                 rows);
            groups.add(gm);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAssets",            assets.size());
        summary.put("totalCost",              totalCost);
        summary.put("totalAccumDepreciation", totalAccumDep);
        summary.put("totalBookValue",         totalBookValue);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("asOf",    LocalDate.now().toString());
        result.put("status",  status != null ? status : "ALL");
        result.put("location", location);
        result.put("summary", summary);
        result.put("groups",  groups);
        return result;
    }

    // ─── Depreciation Period Report ────────────────────────────────────────────

    public Map<String, Object> getDepreciationReport(int year, Integer month) {
        UUID tenantId = tenantContext.current();
        List<DepreciationEntry> entries = month != null
                ? depreciationEntryRepo.findByTenantIdAndPeriodYearAndPeriodMonthOrderByDepreciationDateAsc(tenantId, year, month)
                : depreciationEntryRepo.findByTenantIdAndPeriodYearOrderByDepreciationDateAsc(tenantId, year);

        Map<String, List<DepreciationEntry>> byGroup = new LinkedHashMap<>();
        for (DepreciationEntry de : entries) {
            FixedAsset a = de.getAsset();
            String grp = (a != null && a.getAssetGroup() != null) ? a.getAssetGroup().getName() : "Ungrouped";
            byGroup.computeIfAbsent(grp, k -> new ArrayList<>()).add(de);
        }

        BigDecimal totalDep   = BigDecimal.ZERO;
        BigDecimal postedAmt  = BigDecimal.ZERO;
        BigDecimal pendingAmt = BigDecimal.ZERO;
        int postedCt = 0, pendingCt = 0;

        List<Map<String, Object>> groups = new ArrayList<>();
        for (Map.Entry<String, List<DepreciationEntry>> entry : byGroup.entrySet()) {
            BigDecimal gTotal = BigDecimal.ZERO;
            int gPosted = 0, gPending = 0;
            List<Map<String, Object>> rows = new ArrayList<>();
            for (DepreciationEntry de : entry.getValue()) {
                BigDecimal dep = nvl(de.getDepreciationAmount());
                gTotal = gTotal.add(dep);
                if (de.isPosted()) { gPosted++; postedAmt  = postedAmt.add(dep);  }
                else               { gPending++; pendingAmt = pendingAmt.add(dep); }
                FixedAsset a = de.getAsset();
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("entryId",            de.getId());
                row.put("assetCode",          a != null ? a.getAssetCode() : "");
                row.put("assetName",          a != null ? a.getName()      : "");
                row.put("location",           a != null ? a.getLocation()  : "");
                row.put("openingValue",       nvl(de.getOpeningValue()));
                row.put("depreciationAmount", dep);
                row.put("closingValue",       nvl(de.getClosingValue()));
                row.put("posted",             de.isPosted());
                row.put("depreciationDate",   de.getDepreciationDate() != null ? de.getDepreciationDate().toString() : "");
                rows.add(row);
            }
            totalDep  = totalDep.add(gTotal);
            postedCt  += gPosted;
            pendingCt += gPending;

            Map<String, Object> gm = new LinkedHashMap<>();
            gm.put("groupName",         entry.getKey());
            gm.put("count",             entry.getValue().size());
            gm.put("posted",            gPosted);
            gm.put("pending",           gPending);
            gm.put("totalDepreciation", gTotal);
            gm.put("entries",           rows);
            groups.add(gm);
        }

        String[] MONTHS = {"","January","February","March","April","May","June",
                            "July","August","September","October","November","December"};
        String periodLabel = month != null ? MONTHS[month] + " " + year : "Year " + year;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalEntries",    entries.size());
        summary.put("posted",          postedCt);
        summary.put("pending",         pendingCt);
        summary.put("totalDepreciation", totalDep);
        summary.put("postedAmount",    postedAmt);
        summary.put("pendingAmount",   pendingAmt);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year",        year);
        result.put("month",       month);
        result.put("periodLabel", periodLabel);
        result.put("summary",     summary);
        result.put("groups",      groups);
        return result;
    }

    // ─── Asset Maintenance ─────────────────────────────────────────────────────

    public PageResponse<AssetMaintenance> listMaintenance(UUID assetId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (assetId != null)
            return PageResponse.of(maintenanceRepo.findByTenantIdAndAssetIdAndDeletedAtIsNull(tenantId, assetId, pageable));
        return PageResponse.of(maintenanceRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public AssetMaintenance createMaintenance(AssetMaintenance req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);
        // Denormalise asset code/name for easy retrieval
        if (req.getAssetId() != null) {
            fixedAssetRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getAssetId()).ifPresent(a -> {
                req.setAssetCode(a.getAssetCode());
                req.setAssetName(a.getName());
            });
        }
        return maintenanceRepo.save(req);
    }

    @Transactional
    public AssetMaintenance updateMaintenance(UUID id, AssetMaintenance req) {
        UUID tenantId = tenantContext.current();
        AssetMaintenance e = maintenanceRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Maintenance record not found: " + id));
        e.setMaintenanceDate(req.getMaintenanceDate());
        e.setMaintenanceType(req.getMaintenanceType());
        e.setDescription(req.getDescription());
        e.setCost(req.getCost());
        e.setVendor(req.getVendor());
        e.setPerformedBy(req.getPerformedBy());
        e.setCompletionDate(req.getCompletionDate());
        e.setNextMaintenanceDate(req.getNextMaintenanceDate());
        e.setStatus(req.getStatus());
        e.setRemarks(req.getRemarks());
        return maintenanceRepo.save(e);
    }

    @Transactional
    public void deleteMaintenance(UUID id) {
        UUID tenantId = tenantContext.current();
        AssetMaintenance e = maintenanceRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Maintenance record not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        maintenanceRepo.save(e);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional
    public Map<String, Object> postAllPendingDepreciations() {
        UUID tenantId = tenantContext.current();
        List<DepreciationEntry> pending = depreciationEntryRepo.findByTenantIdAndPostedAndDepreciationDateBefore(
                tenantId, false, LocalDate.now().plusDays(1));
        int posted = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();
        for (DepreciationEntry entry : pending) {
            try {
                postDepreciation(entry.getId());
                posted++;
            } catch (Exception ex) {
                skipped++;
                errors.add("Entry " + entry.getId() + ": " + ex.getMessage());
                log.warn("Batch depreciation skipped entry {}: {}", entry.getId(), ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalPending", pending.size());
        result.put("posted", posted);
        result.put("skipped", skipped);
        if (!errors.isEmpty()) result.put("errors", errors);
        return result;
    }

    public Map<String, Object> getAssetSummary() {
        UUID tenantId = tenantContext.current();
        long totalAssets = fixedAssetRepo.countByTenantIdAndDeletedAtIsNull(tenantId);
        long pendingDep = depreciationEntryRepo.findByTenantIdAndPostedAndDepreciationDateBefore(
                tenantId, false, LocalDate.now().plusDays(1)).size();
        List<FixedAsset> allAssets = fixedAssetRepo.findByTenantIdAndActiveAndDeletedAtIsNull(tenantId, true);
        BigDecimal totalCost = allAssets.stream()
                .map(a -> a.getPurchaseCost() != null ? a.getPurchaseCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBookValue = allAssets.stream()
                .map(a -> a.getCurrentBookValue() != null ? a.getCurrentBookValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAssets", totalAssets);
        summary.put("totalCost", totalCost);
        summary.put("totalBookValue", totalBookValue);
        summary.put("pendingDepreciations", pendingDep);
        return summary;
    }
}
