package com.erp.platform.modules.reports.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.ReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryLineRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api/v1/reports")
@RequiredArgsConstructor @Tag(name="Reports - Financial",description="Financial reporting endpoints")
public class FinancialReportController {
    private final InvoiceRepository invoiceRepo;
    private final ReceiptRepository receiptRepo;
    private final PurchaseOrderRepository purchaseOrderRepo;
    private final JournalEntryRepository journalEntryRepo;
    private final JournalEntryLineRepository journalEntryLineRepo;
    private final AccountRepository accountRepo;
    private final com.erp.platform.modules.sales.repository.SalesReturnRepository salesReturnRepo;
    private final com.erp.platform.modules.inventory.repository.StockLotRepository stockLotRepo;
    private final com.erp.platform.modules.dispatch.repository.DispatchChallanRepository dispatchChallanRepo;
    /** Spend analysis needs what was billed, not only what was ordered. */
    private final com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository purchaseInvoiceRepo;
    /** Collections arrive as receipts, as advances taken before invoicing, and as bank vouchers. */
    private final com.erp.platform.modules.sales.repository.CustomerAdvanceRepository customerAdvanceRepo;
    private final com.erp.platform.modules.accounting.repository.BankVoucherRepository bankVoucherRepo;
    /** Cash flow is both directions: receipts in, supplier payments out. */
    private final com.erp.platform.modules.purchase.repository.SupplierPaymentRepository supplierPaymentRepo;
    /** A debtor statement needs the credits too, not only the invoices. */
    private final com.erp.platform.modules.accounting.repository.CreditNoteRepository creditNoteRepo;
    /** Liabilities are recorded in three places; reading one understates what is owed. */
    private final com.erp.platform.modules.purchase.repository.PaymentLiabilityRepository paymentLiabilityRepo;
    private final com.erp.platform.modules.hr.repository.HrExpenseRepository expenseRepo;
    /** Stock valuation reads movement costs and falls back to the product's purchase price. */
    private final com.erp.platform.modules.inventory.repository.StockTransactionRepository stockTransactionRepo;
    private final com.erp.platform.modules.master.repository.ProductRepository productRepo;
    /** Purchase Summary's PO/GRN counts per group. */
    private final com.erp.platform.modules.purchase.repository.GoodsReceiptRepository goodsReceiptRepo;
    private final TenantContext tenantContext;

    @GetMapping("/ledger-statement") @PreAuthorize("isAuthenticated()")
    @Operation(summary="Ledger statement for one account: opening balance, dated movements and closing balance")
    public ResponseEntity<ApiResponse<Map<String,Object>>> ledgerStatement(
            @RequestParam String accountCode,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required=false) String groupCode,
            @RequestParam(required=false) String amountFrom,
            @RequestParam(required=false) String amountTo) {
        var tid = tenantContext.current();
        LocalDate fromD = LocalDate.parse(from);
        LocalDate toD   = LocalDate.parse(to);

        Account account = accountRepo.findByTenantIdAndCodeAndDeletedAtIsNull(tid, accountCode)
                .or(() -> {
                    try { return accountRepo.findByTenantIdAndIdAndDeletedAtIsNull(tid, UUID.fromString(accountCode)); }
                    catch (Exception e) { return Optional.empty(); }
                })
                .or(() -> accountRepo.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tid, accountCode))
                .orElse(null);

        String searchCode = account != null ? account.getCode() : accountCode;
        UUID searchId = account != null ? account.getId() : null;
        String searchName = account != null ? account.getName() : accountCode;

        String accountName = account != null ? account.getName() : accountCode;
        String accountType = account != null && account.getType() != null ? account.getType() : "";

        List<JournalEntryLine> openingLines;
        List<JournalEntryLine> statementLines;

        if (searchId != null) {
            openingLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountId(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, searchId);
            statementLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountIdOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, toD, searchId);
        } else if (account != null) {
            openingLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountCode(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, searchCode);
            statementLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountCodeOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, toD, searchCode);
        } else {
            openingLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountNameIgnoreCase(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, searchName);
            statementLines = journalEntryLineRepo.findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountNameIgnoreCaseOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                    tid, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, fromD, toD, searchName);
        }

        BigDecimal opening = openingLines.stream()
                .map(l -> (l.getDebitAmount() != null ? l.getDebitAmount() : BigDecimal.ZERO)
                        .subtract(l.getCreditAmount() != null ? l.getCreditAmount() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String,Object>> lines = new ArrayList<>();
        BigDecimal running = opening, totalDebit = BigDecimal.ZERO, totalCredit = BigDecimal.ZERO;
        for (JournalEntryLine line : statementLines) {
            BigDecimal debit  = line.getDebitAmount() != null ? line.getDebitAmount() : BigDecimal.ZERO;
            BigDecimal credit = line.getCreditAmount() != null ? line.getCreditAmount() : BigDecimal.ZERO;
            running     = running.add(debit).subtract(credit);
            totalDebit  = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);

            var je = line.getJournalEntry();
            Map<String,Object> m = new HashMap<>();
            m.put("date",            je != null && je.getEntryDate() != null ? je.getEntryDate().toString() : "");
            m.put("description",     line.getDescription() != null && !line.getDescription().isBlank() ? line.getDescription() : (je != null ? je.getDescription() : ""));
            m.put("referenceNumber", je != null && je.getReferenceNumber() != null ? je.getReferenceNumber() : "");
            m.put("referenceType",   je != null && je.getReferenceType() != null ? je.getReferenceType() : "");
            m.put("voucherType",     je != null && je.getReferenceType() != null ? je.getReferenceType() : "");
            m.put("entryNumber",     je != null && je.getEntryNumber() != null ? je.getEntryNumber() : "");
            m.put("chequeNo",        "");
            m.put("debit",           debit);
            m.put("credit",          credit);
            m.put("balance",         running);
            lines.add(m);
        }
        BigDecimal closing = opening.add(totalDebit).subtract(totalCredit);

        Map<String,Object> result = new HashMap<>();
        result.put("accountCode", accountCode); result.put("accountName", accountName);
        result.put("accountType", accountType); result.put("from", from); result.put("to", to);
        result.put("openingBalance", opening); result.put("lines", lines);
        result.put("totalDebit", totalDebit); result.put("totalCredit", totalCredit);
        result.put("closingBalance", closing);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Net sales: what actually went out against what came back.
     *
     * <p>Previously built from invoices, which measures what was billed rather than what moved, and
     * carried no quantities at all - the netQty column existed but was always zero. Dispatch
     * challans are the record of goods leaving and sales returns the record of goods coming back,
     * so those two are what "net sales" means here, in both value and quantity.
     *
     * <p>The from/to parameters were accepted and then never used, so every run reported all time
     * regardless of the dates on the screen. They filter now.
     */
    @GetMapping("/net-sales") @PreAuthorize("isAuthenticated()") @Operation(summary="Net sales report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> netSales(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(required=false,defaultValue="") String product,
            @RequestParam(required=false,defaultValue="") String salesArea,
            @RequestParam(required=false,defaultValue="") String salesRep,
            @RequestParam(required=false,defaultValue="MONTH") String groupBy,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        LocalDate fromD = parseReportDate(from), toD = parseReportDate(to);

        var challans = dispatchChallanRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 5000)).getContent();
        var returns  = salesReturnRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 5000)).getContent();

        Map<String, Map<String, Object>> groups = new LinkedHashMap<>();

        for (var dc : challans) {
            if (outsideReportWindow(dc.getChallanDate(), fromD, toD)) continue;
            if (!filterMatches(product, dc.getProductName())) continue;
            if (!filterMatches(salesArea, dc.getSalesArea())) continue;

            String key = "CUSTOMER".equalsIgnoreCase(groupBy)
                    ? blankTo(dc.getCustomerName(), "General Customer")
                    : "PRODUCT".equalsIgnoreCase(groupBy)
                    ? blankTo(dc.getProductName(), "Unspecified Product")
                    : monthKey(dc.getChallanDate());

            var g = netSalesRow(groups, key);
            g.put("dispatchQty", addAmt(g.get("dispatchQty"), dc.getQuantityKgs()));
            g.put("sales",       addAmt(g.get("sales"),       dc.getValue()));
        }

        for (var ret : returns) {
            if (outsideReportWindow(ret.getReturnDate(), fromD, toD)) continue;

            BigDecimal retQty = BigDecimal.ZERO;
            String firstProduct = null;
            if (ret.getItems() != null) {
                for (var it : ret.getItems()) {
                    if (it.getQuantity() != null) retQty = retQty.add(it.getQuantity());
                    if (firstProduct == null) firstProduct = it.getProductName();
                }
            }
            if (!filterMatches(product, firstProduct)) continue;

            String key = "CUSTOMER".equalsIgnoreCase(groupBy)
                    ? blankTo(ret.getCustomerName(), "General Customer")
                    : "PRODUCT".equalsIgnoreCase(groupBy)
                    ? blankTo(firstProduct, "Unspecified Product")
                    : monthKey(ret.getReturnDate());

            // A return whose period has no dispatch still belongs in the report; the old version
            // looked the group up and silently dropped the return when it was missing.
            var g = netSalesRow(groups, key);
            g.put("salesReturnQty", addAmt(g.get("salesReturnQty"), retQty));
            g.put("salesReturn",    addAmt(g.get("salesReturn"),    ret.getTotalAmount()));
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (var g : groups.values()) {
            g.put("netSalesQty", ((BigDecimal) g.get("dispatchQty")).subtract((BigDecimal) g.get("salesReturnQty")));
            g.put("netSales",    ((BigDecimal) g.get("sales")).subtract((BigDecimal) g.get("salesReturn")));
            list.add(g);
        }
        list.sort((a, b) -> String.valueOf(a.get("groupKey")).compareTo(String.valueOf(b.get("groupKey"))));

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(new org.springframework.data.domain.PageImpl<>(list))));
    }

    /**
     * Net sales broken down by what was sold: crop group, crop, variety and product.
     *
     * <p>The period report answers "how much did we sell in June"; this answers "which varieties did
     * it come from". Dispatch challans carry a lot number rather than the crop cascade, so crop,
     * variety and group are resolved from the lot; sales return items carry theirs directly.
     */
    @GetMapping("/net-sales-by-crop") @PreAuthorize("isAuthenticated()")
    @Operation(summary="Net sales by crop group / crop / variety / product")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> netSalesByCrop(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(required=false,defaultValue="") String cropGroup,
            @RequestParam(required=false,defaultValue="") String crop,
            @RequestParam(required=false,defaultValue="") String variety) {
        var tid = tenantContext.current();
        LocalDate fromD = parseReportDate(from), toD = parseReportDate(to);

        // One pass over the lots, so resolving a challan's crop is a map lookup rather than a query
        // per challan.
        Map<String, com.erp.platform.modules.inventory.entity.StockLot> lotByNo = new HashMap<>();
        for (var lot : stockLotRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 20000)).getContent()) {
            if (lot.getLotNo() != null) lotByNo.putIfAbsent(lot.getLotNo(), lot);
        }

        Map<String, Map<String,Object>> groups = new LinkedHashMap<>();

        for (var dc : dispatchChallanRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 5000)).getContent()) {
            if (outsideReportWindow(dc.getChallanDate(), fromD, toD)) continue;
            var lot = dc.getLotNumber() != null ? lotByNo.get(dc.getLotNumber()) : null;
            String cg = lot != null ? lot.getCropGroupName() : null;
            String cr = lot != null ? lot.getCropName() : null;
            String vr = lot != null ? lot.getVarietyName() : null;
            if (!filterMatches(cropGroup, cg) || !filterMatches(crop, cr) || !filterMatches(variety, vr)) continue;

            var g = cropRow(groups, cg, cr, vr, dc.getProductName());
            g.put("dispatchQty", addAmt(g.get("dispatchQty"), dc.getQuantityKgs()));
            g.put("sales",       addAmt(g.get("sales"),       dc.getValue()));
        }

        for (var ret : salesReturnRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 5000)).getContent()) {
            if (outsideReportWindow(ret.getReturnDate(), fromD, toD)) continue;
            if (ret.getItems() == null) continue;
            for (var it : ret.getItems()) {
                var lot = it.getLotNumber() != null ? lotByNo.get(it.getLotNumber()) : null;
                String cg = lot != null ? lot.getCropGroupName() : null;
                String cr = it.getCropName() != null ? it.getCropName() : (lot != null ? lot.getCropName() : null);
                String vr = it.getVarietyName() != null ? it.getVarietyName() : (lot != null ? lot.getVarietyName() : null);
                if (!filterMatches(cropGroup, cg) || !filterMatches(crop, cr) || !filterMatches(variety, vr)) continue;

                var g = cropRow(groups, cg, cr, vr, it.getProductName());
                BigDecimal qty = it.getQuantity() != null ? it.getQuantity() : BigDecimal.ZERO;
                BigDecimal val = qty.multiply(it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO);
                g.put("salesReturnQty", addAmt(g.get("salesReturnQty"), qty));
                g.put("salesReturn",    addAmt(g.get("salesReturn"),    val));
            }
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (var g : groups.values()) {
            g.put("netSalesQty", ((BigDecimal) g.get("dispatchQty")).subtract((BigDecimal) g.get("salesReturnQty")));
            g.put("netSales",    ((BigDecimal) g.get("sales")).subtract((BigDecimal) g.get("salesReturn")));
            list.add(g);
        }
        list.sort((a,b) -> String.valueOf(a.get("groupKey")).compareTo(String.valueOf(b.get("groupKey"))));

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(new org.springframework.data.domain.PageImpl<>(list))));
    }

    // -- Net-sales helpers ---------------------------------------------------

    private static Map<String,Object> netSalesRow(Map<String, Map<String,Object>> groups, String key) {
        return groups.computeIfAbsent(key, k -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("period", k);
            m.put("groupKey", k);
            m.put("groupLabel", k);
            m.put("dispatchQty", BigDecimal.ZERO);
            m.put("sales", BigDecimal.ZERO);
            m.put("salesReturnQty", BigDecimal.ZERO);
            m.put("salesReturn", BigDecimal.ZERO);
            m.put("netSalesQty", BigDecimal.ZERO);
            m.put("netSales", BigDecimal.ZERO);
            return m;
        });
    }

    private static Map<String,Object> cropRow(Map<String, Map<String,Object>> groups,
                                              String cropGroup, String crop, String variety, String product) {
        String cg = blankTo(cropGroup, "Unspecified");
        String cr = blankTo(crop, "Unspecified");
        String vr = blankTo(variety, "Unspecified");
        String pr = blankTo(product, "Unspecified");
        String key = cg + " | " + cr + " | " + vr + " | " + pr;
        return groups.computeIfAbsent(key, k -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("groupKey", k);
            m.put("cropGroupName", cg);
            m.put("cropName", cr);
            m.put("varietyName", vr);
            m.put("productName", pr);
            m.put("dispatchQty", BigDecimal.ZERO);
            m.put("sales", BigDecimal.ZERO);
            m.put("salesReturnQty", BigDecimal.ZERO);
            m.put("salesReturn", BigDecimal.ZERO);
            m.put("netSalesQty", BigDecimal.ZERO);
            m.put("netSales", BigDecimal.ZERO);
            return m;
        });
    }

    private static BigDecimal addAmt(Object running, BigDecimal v) {
        return ((BigDecimal) running).add(v != null ? v : BigDecimal.ZERO);
    }

    private static String blankTo(String v, String fallback) {
        return v != null && !v.isBlank() ? v : fallback;
    }

    private static String monthKey(LocalDate d) {
        return d != null ? d.toString().substring(0, 7) : "Undated";
    }

    private static LocalDate parseReportDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    /**
     * Whether a document falls outside the requested window.
     *
     * <p>Deliberately stricter than {@code outsideRange} further down this class: an undated
     * document is excluded once a window is given. Asking for June and being shown a challan that
     * might be from any month would overstate the period, and net sales is a figure people compare
     * month to month.
     */
    private static boolean outsideReportWindow(LocalDate d, LocalDate from, LocalDate to) {
        if (d == null) return from != null || to != null;
        if (from != null && d.isBefore(from)) return true;
        return to != null && d.isAfter(to);
    }

    /** A blank filter matches everything; otherwise a case-insensitive contains. */
    private static boolean filterMatches(String filter, String value) {
        if (filter == null || filter.isBlank()) return true;
        return value != null && value.toLowerCase().contains(filter.toLowerCase());
    }

    @GetMapping("/dispatch-status") @PreAuthorize("isAuthenticated()") @Operation(summary="Dispatch status report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> dispatchStatus(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("challanDate").descending());
        var result = dispatchChallanRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(dc -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", dc.getId());
            m.put("dispatchCode", dc.getChallanNumber());
            m.put("dispatchNumber", dc.getChallanNumber());
            m.put("dispatchDate", dc.getChallanDate() != null ? dc.getChallanDate().toString() : "");
            m.put("customerName", dc.getCustomerName() != null ? dc.getCustomerName() : "General Customer");
            m.put("location", dc.getDispatchLocation() != null ? dc.getDispatchLocation() : "Main Warehouse");
            m.put("status", dc.getStatus() != null ? dc.getStatus() : "DISPATCHED");
            m.put("totalItems", 1);
            m.put("totalAmount", dc.getValue() != null ? dc.getValue() : (dc.getQuantityKgs() != null ? dc.getQuantityKgs() : BigDecimal.ZERO));
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/inventory") @PreAuthorize("isAuthenticated()") @Operation(summary="Inventory stock report")
    public ResponseEntity<ApiResponse<Map<String,Object>>> inventoryReport(
            @RequestParam(required=false,defaultValue="") String warehouseId) {
        var tid = tenantContext.current();
        var lots = stockLotRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 1000)).getContent();
        Map<UUID, BigDecimal> costByProduct = weightedAverageCosts(tid);
        PriceIndex purchasePrices = purchasePriceIndex(tid);

        BigDecimal totalBags = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        List<Map<String, Object>> valuation = new ArrayList<>();
        List<Map<String, Object>> lowStock = new ArrayList<>();

        for (var lot : lots) {
            BigDecimal qty = lot.getQuantity() != null ? lot.getQuantity() : BigDecimal.ZERO;
            int bags = lot.getNoOfBags() != null ? lot.getNoOfBags() : 0;
            totalQty = totalQty.add(qty);
            totalBags = totalBags.add(new BigDecimal(bags));

            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", lot.getId());
            v.put("lotNo", lot.getLotNo());
            v.put("productName", lot.getProductName() != null ? lot.getProductName() : "Seed Product");
            v.put("cropGroupName", lot.getCropGroupName() != null ? lot.getCropGroupName() : "General Crop Group");
            v.put("cropName", lot.getCropName() != null ? lot.getCropName() : "General Crop");
            v.put("varietyName", lot.getVarietyName() != null ? lot.getVarietyName() : "General Hybrid");
            v.put("location", lot.getLocation() != null ? lot.getLocation() : (lot.getGodownName() != null ? lot.getGodownName() : "Main Warehouse"));
            v.put("godownName", lot.getGodownName() != null ? lot.getGodownName() : "Main Warehouse");
            v.put("materialState", lot.getMaterialState() != null ? lot.getMaterialState() : "RAW");
            v.put("quantity", qty);
            v.put("noOfBags", bags);
            v.put("unit", lot.getUnit() != null ? lot.getUnit() : "BAGS");
            // Valued at what the stock actually cost, not a placeholder rate.
            BigDecimal unitCost = resolveUnitCost(lot, costByProduct, purchasePrices);
            v.put("unitCost", unitCost);
            BigDecimal lineValue = qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
            v.put("totalValue", lineValue);
            // The chart plots category/value; without these keys every bar was undefined and the
            // section rendered blank. Crop group is the meaningful grouping for this stock.
            v.put("category", lot.getCropGroupName() != null ? lot.getCropGroupName()
                    : (lot.getProductName() != null ? lot.getProductName() : "Uncategorised"));
            v.put("value", lineValue);
            v.put("name", firstNonBlankOf(lot.getVarietyName(), lot.getProductName(), lot.getLotNo()));
            totalValue = totalValue.add(lineValue);
            valuation.add(v);

            // Below its own reorder level, not an arbitrary "fewer than ten". Products carry the
            // level; where none is set the lot cannot be judged low and is left out rather than
            // guessed at.
            BigDecimal reorderLevel = lot.getProductId() == null ? null
                    : productRepo.findByTenantIdAndIdAndDeletedAtIsNull(tid, lot.getProductId())
                        .map(p -> p.getReorderLevel()).orElse(null);
            if (reorderLevel != null && reorderLevel.signum() > 0 && qty.compareTo(reorderLevel) < 0) {
                Map<String, Object> low = new LinkedHashMap<>(v);
                // The panel reads name/current/reorderLevel; it was reading keys that never existed,
                // which is why every entry printed "undefined: undefined / undefined".
                low.put("current", qty);
                low.put("reorderLevel", reorderLevel);
                low.put("shortfall", reorderLevel.subtract(qty));
                lowStock.add(low);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalSkus", lots.size());
        summary.put("totalBags", totalBags);
        summary.put("totalQty", totalQty);
        summary.put("totalValue", totalValue);
        summary.put("lowStockCount", lowStock.size());

        // "Valuation by category" means one bar per category, not one per lot. Aggregated here so
        // the chart is a summary rather than a wall of individual lots.
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (Map<String, Object> v : valuation) {
            String cat = String.valueOf(v.getOrDefault("category", "Uncategorised"));
            categoryTotals.merge(cat, dec(v.get("totalValue")), BigDecimal::add);
        }
        List<Map<String, Object>> byCategory = new ArrayList<>();
        categoryTotals.forEach((cat, val) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", cat);
            m.put("value", val);
            byCategory.add(m);
        });
        byCategory.sort((a, b) -> dec(b.get("value")).compareTo(dec(a.get("value"))));

        // Movements were returned as an empty list, so the report could never show stock in or out.
        List<Map<String,Object>> movements = new ArrayList<>();
        stockTransactionRepo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(0, 500,
                Sort.by(Sort.Direction.DESC, "movementDate"))).forEach(mv -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", mv.getId());
            m.put("movementDate", mv.getMovementDate() == null ? "" : mv.getMovementDate().toString());
            m.put("type", mv.getType() == null ? "" : mv.getType().name());
            m.put("productName", mv.getProductName() == null ? "" : mv.getProductName());
            m.put("warehouseName", mv.getWarehouseName() == null ? "" : mv.getWarehouseName());
            m.put("quantity", mv.getQuantity());
            m.put("unitCost", mv.getUnitCost());
            m.put("totalCost", mv.getTotalCost());
            m.put("balanceAfter", mv.getBalanceAfter());
            m.put("referenceType", mv.getReferenceType() == null ? "" : mv.getReferenceType());
            m.put("referenceNumber", mv.getReferenceNumber() == null ? "" : mv.getReferenceNumber());
            movements.add(m);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("valuation", valuation);
        result.put("byCategory", byCategory);
        result.put("lowStock", lowStock);
        result.put("movements", movements);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Purchase and spend, grouped by Month/Supplier/Category (Category = PurchaseOrder.poType —
     * there's no separate category concept). Each group tallies how many POs and GRNs fell in it,
     * how much was invoiced, how much of that has been paid, and what's still outstanding.
     *
     * <p>Previously returned one raw row per PO/invoice document with fields the screen never
     * asked for (documentNumber/vendorName/totalAmount) instead of the grouped poCount/grnCount/
     * invoiceAmount/paid/outstanding shape it reads — every column came back as 0 regardless of
     * real data. This computes the shape the screen actually consumes.
     */
    @GetMapping("/purchase-summary") @PreAuthorize("isAuthenticated()")
    @Operation(summary="Purchase & spend — grouped by month, supplier, or category")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> purchaseSummary(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(required=false,defaultValue="") String supplier,
            @RequestParam(required=false,defaultValue="") String category,
            @RequestParam(required=false,defaultValue="MONTH") String groupBy,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="200") int size) {
        var tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);
        String supplierFilter = supplier == null ? "" : supplier.trim().toLowerCase();
        String categoryFilter = category == null ? "" : category.trim().toUpperCase();

        // groupKey(vendorName, poType, date) -> the bucket that row's PO/GRN/invoice belongs in.
        java.util.function.BiFunction<String, LocalDate, String> monthKey =
                (name, date) -> date == null ? "Unknown" : date.getYear() + "-" + String.format("%02d", date.getMonthValue());

        class Bucket { long poCount, grnCount; BigDecimal invoiceAmount = BigDecimal.ZERO, paid = BigDecimal.ZERO; }
        Map<String, Bucket> buckets = new LinkedHashMap<>();

        // A PO's own poType is the category; look its poType up once so GRNs/invoices (which don't
        // carry a category directly) can inherit it via their purchaseOrderId link.
        Map<UUID, String> poTypeById = new HashMap<>();
        List<com.erp.platform.modules.purchase.entity.PurchaseOrder> pos =
                purchaseOrderRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).getContent();
        for (var po : pos) {
            poTypeById.put(po.getId(), po.getPoType());
        }

        java.util.function.Function<Map.Entry<String, String>, String> groupKeyFor = e -> {
            String name = e.getKey(); String cat = e.getValue();
            return switch (groupBy) {
                case "SUPPLIER" -> name == null || name.isBlank() ? "Unknown" : name;
                case "CATEGORY" -> cat == null || cat.isBlank() ? "Uncategorized" : cat;
                default -> null; // MONTH is computed from the date directly at the call site
            };
        };

        for (var po : pos) {
            if (outsideRange(po.getOrderDate(), fromDate, toDate)) continue;
            String vendorName = po.getVendorName() == null ? "" : po.getVendorName();
            if (!supplierFilter.isEmpty() && !vendorName.toLowerCase().contains(supplierFilter)) continue;
            String cat = po.getPoType() == null ? "" : po.getPoType();
            if (!categoryFilter.isEmpty() && !cat.equalsIgnoreCase(categoryFilter)) continue;
            String key = "MONTH".equals(groupBy) ? monthKey.apply(vendorName, po.getOrderDate())
                    : groupKeyFor.apply(Map.entry(vendorName, cat));
            buckets.computeIfAbsent(key, k -> new Bucket()).poCount++;
        }

        for (var grn : goodsReceiptRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            if (outsideRange(grn.getReceiptDate(), fromDate, toDate)) continue;
            String vendorName = grn.getVendorName() == null ? "" : grn.getVendorName();
            if (!supplierFilter.isEmpty() && !vendorName.toLowerCase().contains(supplierFilter)) continue;
            String cat = poTypeById.getOrDefault(grn.getPurchaseOrderId(), "");
            if (!categoryFilter.isEmpty() && !categoryFilter.equalsIgnoreCase(cat == null ? "" : cat)) continue;
            String key = "MONTH".equals(groupBy) ? monthKey.apply(vendorName, grn.getReceiptDate())
                    : groupKeyFor.apply(Map.entry(vendorName, cat));
            buckets.computeIfAbsent(key, k -> new Bucket()).grnCount++;
        }

        for (var pi : purchaseInvoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            if (outsideRange(pi.getInvoiceDate(), fromDate, toDate)) continue;
            String vendorName = pi.getVendorName() == null ? "" : pi.getVendorName();
            if (!supplierFilter.isEmpty() && !vendorName.toLowerCase().contains(supplierFilter)) continue;
            String cat = poTypeById.getOrDefault(pi.getPurchaseOrderId(), "");
            if (!categoryFilter.isEmpty() && !categoryFilter.equalsIgnoreCase(cat == null ? "" : cat)) continue;
            String key = "MONTH".equals(groupBy) ? monthKey.apply(vendorName, pi.getInvoiceDate())
                    : groupKeyFor.apply(Map.entry(vendorName, cat));
            Bucket b = buckets.computeIfAbsent(key, k -> new Bucket());
            b.invoiceAmount = b.invoiceAmount.add(pi.getNetPayable() == null ? BigDecimal.ZERO : pi.getNetPayable());
            b.paid = b.paid.add(pi.getPaidAmount() == null ? BigDecimal.ZERO : pi.getPaidAmount());
        }

        List<Map<String,Object>> rows = new ArrayList<>();
        buckets.forEach((label, b) -> {
            Map<String,Object> m = new HashMap<>();
            m.put("groupLabel", label);
            m.put("poCount", b.poCount);
            m.put("grnCount", b.grnCount);
            m.put("invoiceAmount", b.invoiceAmount);
            m.put("paid", b.paid);
            m.put("outstanding", b.invoiceAmount.subtract(b.paid));
            rows.add(m);
        });
        rows.sort((a, b) -> String.valueOf(a.get("groupLabel")).compareTo(String.valueOf(b.get("groupLabel"))));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(rows, page, size))));
    }

    /**
     * Sales &amp; revenue analysis — headline figures, a monthly trend, and who and what earned it.
     *
     * <p>The screen asked for /reports/sales and no such endpoint existed, so every request 404'd and
     * the report rendered empty. Cancelled invoices are excluded: they were never revenue.
     */
    @GetMapping("/sales") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sales & revenue analysis — summary, trend, top customers and products")
    public ResponseEntity<ApiResponse<Map<String,Object>>> salesAnalysis(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to) {
        var tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        int invoiceCount = 0;
        Map<String, BigDecimal> byMonth = new TreeMap<>();
        Map<String, BigDecimal> byCustomer = new HashMap<>();
        Map<String, BigDecimal[]> byProduct = new HashMap<>();   // name -> [qty, amount]

        for (var inv : invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            LocalDate d = inv.getInvoiceDate();
            if (outsideRange(d, fromDate, toDate)) continue;
            if (inv.getStatus() == com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus.CANCELLED) continue;

            BigDecimal amount = dec(inv.getTotalAmount());
            totalRevenue = totalRevenue.add(amount);
            outstanding = outstanding.add(dec(inv.getBalanceDue()));
            invoiceCount++;

            if (d != null) {
                String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
                byMonth.merge(key, amount, BigDecimal::add);
            }
            String cust = firstNonBlankOf(inv.getCustomerName(), "Unattributed");
            byCustomer.merge(cust, amount, BigDecimal::add);

            for (var item : inv.getItems() == null ? List.<com.erp.platform.modules.sales.entity.InvoiceItem>of() : inv.getItems()) {
                String pname = firstNonBlankOf(item.getProductName(), item.getDescription(), "Unnamed item");
                BigDecimal[] agg = byProduct.computeIfAbsent(pname, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                agg[0] = agg[0].add(dec(item.getQuantity()));
                agg[1] = agg[1].add(dec(item.getTotalAmount()));
            }
        }

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalInvoices", invoiceCount);
        summary.put("avgInvoiceValue", invoiceCount == 0 ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(invoiceCount), 2, RoundingMode.HALF_UP));
        summary.put("outstanding", outstanding);

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("trend", monthlyTrend(byMonth));
        result.put("topCustomers", topBy(byCustomer, 10));
        result.put("topProducts", byProduct.entrySet().stream()
                .sorted((a, b) -> b.getValue()[1].compareTo(a.getValue()[1]))
                .limit(10)
                .map(e -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("quantity", e.getValue()[0].stripTrailingZeros().toPlainString());
                    m.put("amount", e.getValue()[1]);
                    return m;
                }).toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Purchase &amp; spend analysis — the counterpart to {@link #salesAnalysis}.
     *
     * <p>This used to be an alias for the paged document list, which is a different shape entirely:
     * the screen reads summary/trend/topVendors and got a page of rows, so it showed nothing. The
     * document list is still served at /purchase-summary, which is what asks for it.
     */
    @GetMapping("/purchases") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Purchase & spend analysis — summary, trend and top vendors")
    public ResponseEntity<ApiResponse<Map<String,Object>>> purchaseAnalysis(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to) {
        var tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);

        BigDecimal totalSpend = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        int orderCount = 0;
        Map<String, BigDecimal> byMonth = new TreeMap<>();
        Map<String, BigDecimal> byVendor = new HashMap<>();

        // Orders are the commitment, and what "POs raised" counts.
        for (var po : purchaseOrderRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            LocalDate d = po.getOrderDate();
            if (outsideRange(d, fromDate, toDate)) continue;
            BigDecimal amount = dec(po.getTotalAmount());
            totalSpend = totalSpend.add(amount);
            orderCount++;
            if (d != null) {
                byMonth.merge(d.getYear() + "-" + String.format("%02d", d.getMonthValue()), amount, BigDecimal::add);
            }
            byVendor.merge(firstNonBlankOf(po.getVendorName(), "Unattributed"), amount, BigDecimal::add);
        }

        // What is still owed comes from invoices, not orders — an order is not a debt.
        for (var pi : purchaseInvoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            if (outsideRange(pi.getInvoiceDate(), fromDate, toDate)) continue;
            outstanding = outstanding.add(dec(pi.getNetPayable()).subtract(dec(pi.getPaidAmount())).max(BigDecimal.ZERO));
        }

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("totalSpend", totalSpend);
        summary.put("totalPOs", orderCount);
        summary.put("avgPOValue", orderCount == 0 ? BigDecimal.ZERO
                : totalSpend.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP));
        summary.put("outstanding", outstanding);

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("trend", monthlyTrend(byMonth));
        result.put("topVendors", topBy(byVendor, 10));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Month buckets as the charts want them: a readable label and a value. */
    private static List<Map<String,Object>> monthlyTrend(Map<String, BigDecimal> byMonth) {
        return byMonth.entrySet().stream().map(e -> {
            Map<String,Object> m = new LinkedHashMap<>();
            String[] parts = e.getKey().split("-");
            m.put("label", java.time.Month.of(Integer.parseInt(parts[1]))
                    .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                    + " " + parts[0].substring(2));
            m.put("value", e.getValue());
            m.put("period", e.getKey());
            return m;
        }).toList();
    }

    /** Highest-value entries first, as {name, amount} — the shape both "top" panels read. */
    private static List<Map<String,Object>> topBy(Map<String, BigDecimal> totals, int limit) {
        return totals.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(e -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                }).toList();
    }

    /**
     * Collections by customer over a period: what they were invoiced, what actually came in, and
     * what is still owed.
     *
     * <p>Money reaches us by three routes and all three count as collected — a receipt against an
     * invoice, an advance/deposit taken before invoicing, and a bank receipt voucher entered
     * straight into the books. Counting only receipts understated collections and made every
     * customer look delinquent.
     *
     * <p>Customers are keyed by id where there is one and by name otherwise, because bank vouchers
     * record only a party name.
     */
    @GetMapping("/collections") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Collections by customer — invoiced, collected and outstanding")
    public ResponseEntity<ApiResponse<Map<String,Object>>> collections(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to) {
        UUID tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);

        // key -> row. LinkedHashMap so the order stays stable between refreshes.
        Map<String, Map<String,Object>> byCustomer = new LinkedHashMap<>();

        invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(inv -> {
            if (outsideRange(inv.getInvoiceDate(), fromDate, toDate)) return;
            Map<String,Object> row = collectionRow(byCustomer, inv.getCustomerId(), inv.getCustomerName());
            addTo(row, "invoiceAmount", inv.getTotalAmount());
            // What the invoice itself already records as settled, so a customer who paid before the
            // period still shows as cleared rather than fully outstanding.
            addTo(row, "collectedAmount", inv.getPaidAmount());
        });

        receiptRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(rec -> {
            if (outsideRange(rec.getPaymentDate(), fromDate, toDate)) return;
            if (rec.getStatus() != null && "CANCELLED".equalsIgnoreCase(rec.getStatus().name())) return;
            Map<String,Object> row = collectionRow(byCustomer, rec.getCustomerId(), null);
            addTo(row, "receiptAmount", rec.getAmount());
        });

        customerAdvanceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(adv -> {
            if (outsideRange(adv.getPaymentDate(), fromDate, toDate)) return;
            if (adv.getStatus() != null && "CANCELLED".equalsIgnoreCase(adv.getStatus().name())) return;
            Map<String,Object> row = collectionRow(byCustomer, adv.getCustomerId(), adv.getCustomerName());
            addTo(row, "depositAmount", adv.getAmount());
        });

        bankVoucherRepo.findByTenantIdAndDeletedAtIsNull(tid).forEach(bv -> {
            if (bv.getVoucherType() == null || !"RECEIPT".equalsIgnoreCase(bv.getVoucherType().name())) return;
            if (outsideRange(bv.getVoucherDate(), fromDate, toDate)) return;
            if (bv.getPartyName() == null || bv.getPartyName().isBlank()) return;
            Map<String,Object> row = collectionRow(byCustomer, null, bv.getPartyName());
            addTo(row, "bankReceiptAmount", bv.getTotalAmount());
        });

        List<Map<String,Object>> rows = new ArrayList<>();
        BigDecimal grandInvoiced = BigDecimal.ZERO, grandCollected = BigDecimal.ZERO;
        for (Map<String,Object> row : byCustomer.values()) {
            BigDecimal invoiced = dec(row.get("invoiceAmount"));
            // A receipt recorded against an invoice is already in the invoice's paid amount, so the
            // larger of the two is taken rather than the sum — adding them would double count.
            BigDecimal collected = dec(row.get("collectedAmount")).max(dec(row.get("receiptAmount")))
                    .add(dec(row.get("depositAmount")))
                    .add(dec(row.get("bankReceiptAmount")));
            row.put("invoiceAmount", invoiced);
            row.put("collectedAmount", collected);
            row.put("outstandingAmount", invoiced.subtract(collected).max(BigDecimal.ZERO));
            grandInvoiced = grandInvoiced.add(invoiced);
            grandCollected = grandCollected.add(collected);
            rows.add(row);
        }
        rows.sort((a, b) -> dec(b.get("outstandingAmount")).compareTo(dec(a.get("outstandingAmount"))));

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("totalInvoiced", grandInvoiced);
        summary.put("totalCollected", grandCollected);
        summary.put("totalOutstanding", grandInvoiced.subtract(grandCollected).max(BigDecimal.ZERO));
        summary.put("customerCount", rows.size());

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("rows", rows);
        payload.put("summary", summary);
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    /**
     * Debtor statement — every document that moves a customer's balance, newest first.
     *
     * <p>An invoice-only list is not a statement: a customer who paid, or who was credited for a
     * return, still appears to owe the full amount. Receipts, credit notes and debit notes are
     * included so the running balance is what the customer actually owes.
     *
     * <p>Amounts are signed by their effect on the debtor: invoices and debit notes increase what
     * is owed, receipts and credit notes reduce it.
     */
    @GetMapping("/debtor-statement") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Debtor statement — invoices, receipts, credit and debit notes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> debtorStatement(
            @RequestParam(required=false,defaultValue="") String customerId,
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="200") int size) {
        UUID tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);
        UUID custFilter = customerId == null || customerId.isBlank() ? null : UUID.fromString(customerId);
        List<Map<String,Object>> rows = new ArrayList<>();

        invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(inv -> {
            if (outsideRange(inv.getInvoiceDate(), fromDate, toDate)) return;
            if (custFilter != null && !custFilter.equals(inv.getCustomerId())) return;
            Map<String,Object> m = statementRow("INVOICE", inv.getInvoiceNumber(),
                    inv.getCustomerId(), inv.getCustomerName(), inv.getInvoiceDate(), inv.getTotalAmount(), +1);
            m.put("dueDate", inv.getDueDate()==null?"":inv.getDueDate().toString());
            m.put("paidAmount", inv.getPaidAmount());
            m.put("outstandingAmount", inv.getBalanceDue());
            rows.add(m);
        });

        receiptRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(r -> {
            if (outsideRange(r.getPaymentDate(), fromDate, toDate)) return;
            if (custFilter != null && !custFilter.equals(r.getCustomerId())) return;
            rows.add(statementRow("RECEIPT", r.getReceiptNumber(),
                    r.getCustomerId(), null, r.getPaymentDate(), r.getAmount(), -1));
        });

        // Credit notes carry only a party name — they have no customer id — so a customer filter
        // cannot be applied to them by id and they are matched on name instead.
        creditNoteRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(cn -> {
            if (outsideRange(cn.getNoteDate(), fromDate, toDate)) return;
            if (custFilter != null) return;
            rows.add(statementRow("CREDIT_NOTE", cn.getCreditNoteNumber(),
                    null, cn.getPartyName(), cn.getNoteDate(), cn.getTotalAmount(), -1));
        });

        // A sales return credits the customer, reducing what they owe.
        salesReturnRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(sr -> {
            if (outsideRange(sr.getReturnDate(), fromDate, toDate)) return;
            if (custFilter != null && !custFilter.equals(sr.getCustomerId())) return;
            rows.add(statementRow("SALES_RETURN", sr.getReturnNumber(),
                    sr.getCustomerId(), sr.getCustomerName(), sr.getReturnDate(), sr.getTotalAmount(), -1));
        });

        rows.sort((a, b) -> String.valueOf(b.get("date")).compareTo(String.valueOf(a.get("date"))));

        // Running balance across the whole statement, oldest first, then re-reversed for display.
        BigDecimal running = BigDecimal.ZERO;
        for (int i = rows.size() - 1; i >= 0; i--) {
            running = running.add(dec(rows.get(i).get("signedAmount")));
            rows.get(i).put("runningBalance", running);
        }

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(rows, page, size))));
    }

    private static Map<String,Object> statementRow(String docType, String number, UUID partyId,
                                                   String partyName, LocalDate date,
                                                   BigDecimal amount, int sign) {
        BigDecimal amt = amount == null ? BigDecimal.ZERO : amount;
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("documentType", docType);
        m.put("documentNumber", number);
        m.put("invoiceNumber", number);           // the screen's existing column name
        m.put("customerId", partyId);
        m.put("customerName", partyName == null ? "" : partyName);
        m.put("date", date == null ? "" : date.toString());
        m.put("invoiceDate", date == null ? "" : date.toString());
        m.put("dueDate", "");
        m.put("amount", amt);                     // what the screen reads
        m.put("totalAmount", amt);
        m.put("paidAmount", BigDecimal.ZERO);
        m.put("outstandingAmount", sign > 0 ? amt : BigDecimal.ZERO);
        m.put("signedAmount", sign > 0 ? amt : amt.negate());
        return m;
    }

    private static Map<String,Object> collectionRow(Map<String, Map<String,Object>> byCustomer,
                                                    UUID customerId, String customerName) {
        String key = customerId != null ? customerId.toString()
                : (customerName == null ? "—" : customerName.trim().toLowerCase());
        Map<String,Object> row = byCustomer.computeIfAbsent(key, k -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("customerId", customerId);
            m.put("customerName", customerName == null || customerName.isBlank() ? "Unnamed customer" : customerName);
            return m;
        });
        // A later source may know the name when the first did not (receipts carry only an id).
        if (customerName != null && !customerName.isBlank()
                && "Unnamed customer".equals(row.get("customerName"))) {
            row.put("customerName", customerName);
        }
        return row;
    }

    private static void addTo(Map<String,Object> row, String key, BigDecimal amount) {
        if (amount == null) return;
        row.put(key, dec(row.get(key)).add(amount));
    }

    private static BigDecimal dec(Object o) {
        return o instanceof BigDecimal b ? b : BigDecimal.ZERO;
    }

    private static LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private static boolean outsideRange(LocalDate d, LocalDate from, LocalDate to) {
        if (d == null) return false;              // undated documents are never filtered out
        return (from != null && d.isBefore(from)) || (to != null && d.isAfter(to));
    }

    private static org.springframework.data.domain.Page<Map<String,Object>> paginate(
            List<Map<String,Object>> rows, int page, int size) {
        int fromIdx = Math.min(page * size, rows.size());
        int toIdx = Math.min(fromIdx + size, rows.size());
        return new org.springframework.data.domain.PageImpl<>(
                rows.subList(fromIdx, toIdx), PageRequest.of(page, size), rows.size());
    }

    @GetMapping("/customer-ledger") @PreAuthorize("isAuthenticated()") @Operation(summary="Customer ledger report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> customerLedger(
            @RequestParam(required=false,defaultValue="") String customerId,
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("invoiceDate").descending());
        var result = invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(i -> {
            Map<String,Object> m = new HashMap<>();
            m.put("docType","INVOICE"); m.put("docNumber",i.getInvoiceNumber());
            m.put("date",i.getInvoiceDate()==null?"":i.getInvoiceDate().toString());
            m.put("customerId",i.getCustomerId()); m.put("customerName",i.getCustomerName()==null?"":i.getCustomerName());
            m.put("debit",i.getTotalAmount()); m.put("credit",i.getPaidAmount());
            m.put("balance",i.getTotalAmount().subtract(i.getPaidAmount())); m.put("status",i.getStatus());
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/outstanding-receivables") @PreAuthorize("isAuthenticated()") @Operation(summary="Outstanding receivables")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> outstandingReceivables(
            @RequestParam(required=false,defaultValue="") String customerId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        UUID tid = tenantContext.current();
        LocalDate today = LocalDate.now();
        UUID custFilter = customerId == null || customerId.isBlank() ? null : UUID.fromString(customerId);
        List<Map<String,Object>> rows = new ArrayList<>();

        // Whether an invoice is outstanding is a question about its balance, not its status label.
        // Filtering on status SENT alone dropped every partially paid and overdue invoice, which is
        // exactly the population this report exists to show.
        invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(i -> {
            if (i.getStatus() == Invoice.InvoiceStatus.CANCELLED) return;
            if (custFilter != null && !custFilter.equals(i.getCustomerId())) return;
            BigDecimal total = i.getTotalAmount() == null ? BigDecimal.ZERO : i.getTotalAmount();
            BigDecimal paid  = i.getPaidAmount()  == null ? BigDecimal.ZERO : i.getPaidAmount();
            BigDecimal balance = i.getBalanceDue() != null ? i.getBalanceDue() : total.subtract(paid);
            if (balance.signum() <= 0) return;

            Map<String,Object> m = new HashMap<>();
            m.put("invoiceNumber", i.getInvoiceNumber());
            m.put("customerId", i.getCustomerId());
            m.put("customerName", i.getCustomerName()==null?"":i.getCustomerName());
            m.put("partyName", i.getCustomerName()==null?"":i.getCustomerName());
            m.put("invoiceDate", i.getInvoiceDate()==null?"":i.getInvoiceDate().toString());
            m.put("dueDate", i.getDueDate()==null?"":i.getDueDate().toString());
            m.put("totalAmount", total);
            m.put("paidAmount", paid);
            m.put("balance", balance);
            m.put("outstandingAmount", balance);
            m.put("status", i.getStatus()==null?"":i.getStatus().name());
            long days = i.getDueDate() != null ? ChronoUnit.DAYS.between(i.getDueDate(), today) : 0;
            m.put("daysPastDue", Math.max(0, days));
            m.put("bucket", agingBucket(days));
            rows.add(m);
        });

        rows.sort(Comparator.comparing(m -> String.valueOf(m.get("dueDate"))));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(rows, page, size))));
    }

    /**
     * Everything the business currently owes, from all three places a liability is recorded:
     * purchase invoices not yet settled, recorded payment liabilities, and approved staff expenses
     * awaiting reimbursement. Reading any one of them alone understates what is owed.
     *
     * <p>Settled items are excluded — a liability with nothing left to pay is history, not a
     * liability.
     */
    @GetMapping("/liabilities") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Liability amount — payables, recorded liabilities and staff expenses")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> liabilities(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="200") int size) {
        UUID tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);
        List<Map<String,Object>> rows = new ArrayList<>();

        purchaseInvoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(pi -> {
            if (pi.getStatus() == com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus.CANCELLED) return;
            if (outsideRange(pi.getInvoiceDate(), fromDate, toDate)) return;
            BigDecimal total = pi.getNetPayable()==null?BigDecimal.ZERO:pi.getNetPayable();
            BigDecimal paid  = pi.getPaidAmount()==null?BigDecimal.ZERO:pi.getPaidAmount();
            BigDecimal bal = total.subtract(paid);
            if (bal.signum() <= 0) return;
            rows.add(liabilityRow("PURCHASE_INVOICE", pi.getPiNumber(), pi.getVendorName(),
                    pi.getInvoiceDate(), pi.getDueDate(), bal,
                    paid.signum() > 0 ? "PARTIALLY_PAID" : "PENDING"));
        });

        paymentLiabilityRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(pl -> {
            if (outsideRange(pl.getLiabilityFromDate(), fromDate, toDate)) return;
            BigDecimal bal = pl.getBalance() != null ? pl.getBalance()
                    : (pl.getTotalLiability()==null?BigDecimal.ZERO:pl.getTotalLiability());
            if (bal.signum() <= 0) return;
            rows.add(liabilityRow("PAYMENT_LIABILITY", pl.getLiabilityNumber(), pl.getPartyName(),
                    pl.getLiabilityFromDate(), pl.getLiabilityToDate(), bal,
                    pl.getStatus()==null?"PENDING":pl.getStatus()));
        });

        // Approved expenses the company still owes its staff.
        expenseRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(ex -> {
            if (outsideRange(ex.getExpenseDate(), fromDate, toDate)) return;
            String st = ex.getStatus()==null?"":ex.getStatus().name();
            if (!"APPROVED".equalsIgnoreCase(st)) return;   // draft or reimbursed: not owed now
            BigDecimal amt = ex.getAmount()==null?BigDecimal.ZERO:ex.getAmount();
            if (amt.signum() <= 0) return;
            rows.add(liabilityRow("EMPLOYEE_EXPENSE", ex.getExpenseNumber(), ex.getEmployeeName(),
                    ex.getExpenseDate(), null, amt, "PENDING"));
        });

        rows.sort(Comparator.comparing((Map<String,Object> m) -> String.valueOf(m.get("liabilityDate"))).reversed());
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(rows, page, size))));
    }

    private static Map<String,Object> liabilityRow(String source, String number, String party,
                                                   LocalDate date, LocalDate due,
                                                   BigDecimal amount, String status) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("source", source);
        m.put("documentNumber", number);
        m.put("liabilityNumber", number);
        // The screen labels this column "Vendor Name"; for an expense it is the employee owed.
        m.put("vendorName", party == null ? "" : party);
        m.put("partyName", party == null ? "" : party);
        m.put("liabilityDate", date == null ? "" : date.toString());
        m.put("dueDate", due == null ? "" : due.toString());
        m.put("amount", amount);
        m.put("balance", amount);
        m.put("status", status);
        return m;
    }

    // ── Stock valuation ──────────────────────────────────────────────────────
    //
    // Basis: cost, resolved per lot in this order —
    //   1. the lot's own unit cost, where the receipt or production run recorded one;
    //   2. the weighted average of that product's priced receipts, from stock movements;
    //   3. the product's purchase price, as a last resort.
    //
    // Lot cost first means stock is valued at what each specific lot actually cost, which gives the
    // same answer as FIFO without replaying every movement — and it matches how this system already
    // tracks goods, by lot. Weighted average is the fallback because it is the most defensible
    // figure derivable from history when a lot carries no cost of its own.

    /** Weighted average cost per product, from priced stock receipts. */
    private Map<UUID, BigDecimal> weightedAverageCosts(UUID tenantId) {
        Map<UUID, BigDecimal[]> acc = new HashMap<>();   // productId -> [value, qty]
        stockTransactionRepo.findByTenantIdAndDeletedAtIsNull(tenantId, Pageable.unpaged()).forEach(mv -> {
            if (mv.getProductId() == null) return;
            BigDecimal qty = mv.getQuantity() == null ? BigDecimal.ZERO : mv.getQuantity().abs();
            BigDecimal cost = mv.getUnitCost() == null ? BigDecimal.ZERO : mv.getUnitCost();
            if (qty.signum() <= 0 || cost.signum() <= 0) return;   // only priced movements inform cost
            BigDecimal[] a = acc.computeIfAbsent(mv.getProductId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            a[0] = a[0].add(qty.multiply(cost));
            a[1] = a[1].add(qty);
        });
        Map<UUID, BigDecimal> out = new HashMap<>();
        acc.forEach((pid, a) -> {
            if (a[1].signum() > 0) out.put(pid, a[0].divide(a[1], 4, RoundingMode.HALF_UP));
        });
        return out;
    }

    /**
     * What the business last paid for each thing it buys, keyed by product id and — because lots
     * often carry names rather than ids — by product and variety name too.
     *
     * Without this tier every line valued at nil. Stock lots have no cost of their own (nothing
     * writes one), and the movement costs they fall back on are copied from StockItem.averageCost,
     * which is initialised to zero and never rolled forward. Purchase orders are the one place a
     * real, entered rate survives, so the report reads them directly.
     */
    private PriceIndex purchasePriceIndex(UUID tenantId) {
        PriceIndex index = new PriceIndex();
        // Newest first, so the most recently agreed rate wins for any product bought more than once.
        purchaseOrderRepo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "orderDate")))
                .forEach(po -> {
                    if (po.getItems() == null) return;
                    for (var item : po.getItems()) {
                        index.put(item.getProductId(), item.getUnitPrice(),
                                item.getProductName(), item.getVarietyName());
                    }
                });
        return index;
    }

    /** Prices found by id first, then by any of the names a lot might be known under. */
    private static final class PriceIndex {
        private final Map<UUID, BigDecimal> byId = new HashMap<>();
        private final Map<String, BigDecimal> byName = new HashMap<>();

        void put(UUID productId, BigDecimal price, String... names) {
            if (price == null || price.signum() <= 0) return;
            if (productId != null) byId.putIfAbsent(productId, price);
            for (String n : names) {
                if (n != null && !n.isBlank()) byName.putIfAbsent(n.trim().toLowerCase(), price);
            }
        }

        BigDecimal find(UUID productId, String... names) {
            if (productId != null) {
                BigDecimal p = byId.get(productId);
                if (p != null) return p;
            }
            for (String n : names) {
                if (n == null || n.isBlank()) continue;
                BigDecimal p = byName.get(n.trim().toLowerCase());
                if (p != null) return p;
            }
            return null;
        }
    }

    private BigDecimal resolveUnitCost(com.erp.platform.modules.inventory.entity.StockLot lot,
                                       Map<UUID, BigDecimal> costByProduct,
                                       PriceIndex purchasePrices) {
        if (lot.getUnitCost() != null && lot.getUnitCost().signum() > 0) return lot.getUnitCost();
        if (lot.getProductId() != null) {
            BigDecimal avg = costByProduct.get(lot.getProductId());
            if (avg != null && avg.signum() > 0) return avg;
        }
        // What was actually paid for it, by id or by name. This is what rescues stock that came in
        // as opening balances or through paths that never recorded a rate.
        BigDecimal paid = purchasePrices.find(lot.getProductId(),
                lot.getProductName(), lot.getVarietyName(), lot.getMaterialItemName(), lot.getCropName());
        if (paid != null && paid.signum() > 0) return paid;

        if (lot.getProductId() != null) {
            BigDecimal listed = productRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), lot.getProductId())
                    .map(p -> p.getPurchasePrice()).orElse(null);
            if (listed != null && listed.signum() > 0) return listed;
        }
        return BigDecimal.ZERO;   // honestly zero rather than an invented rate
    }

    /**
     * Stock valuation — what the stock on hand is worth, lot by lot, with the basis stated so the
     * figure can be audited rather than taken on trust.
     */
    @GetMapping("/stock-valuation") @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Stock valuation at cost")
    public ResponseEntity<ApiResponse<Map<String,Object>>> stockValuation(
            @RequestParam(required=false,defaultValue="") String warehouseId) {
        UUID tid = tenantContext.current();
        Map<UUID, BigDecimal> costByProduct = weightedAverageCosts(tid);
        PriceIndex purchasePrices = purchasePriceIndex(tid);
        List<Map<String,Object>> rows = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO, totalValue = BigDecimal.ZERO;
        int unvalued = 0;

        for (var lot : stockLotRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged())) {
            BigDecimal qty = lot.getQuantity() == null ? BigDecimal.ZERO : lot.getQuantity();
            if (qty.signum() <= 0) continue;
            if (!warehouseId.isBlank() && lot.getGodownId() != null
                    && !warehouseId.equals(lot.getGodownId().toString())) continue;

            BigDecimal unitCost = resolveUnitCost(lot, costByProduct, purchasePrices);
            String basis = lot.getUnitCost() != null && lot.getUnitCost().signum() > 0 ? "LOT_COST"
                    : (lot.getProductId() != null && costByProduct.containsKey(lot.getProductId())
                        ? "WEIGHTED_AVERAGE" : (unitCost.signum() > 0 ? "PURCHASE_PRICE" : "UNVALUED"));
            if (unitCost.signum() <= 0) unvalued++;

            BigDecimal value = qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("lotNo", lot.getLotNo());
            m.put("productName", lot.getProductName() == null ? "" : lot.getProductName());
            m.put("cropName", lot.getCropName() == null ? "" : lot.getCropName());
            m.put("varietyName", lot.getVarietyName() == null ? "" : lot.getVarietyName());
            m.put("godownName", lot.getGodownName() == null ? "" : lot.getGodownName());
            m.put("materialState", lot.getMaterialState() == null ? "" : lot.getMaterialState());
            m.put("quantity", qty);
            m.put("unit", lot.getUnit() == null ? "" : lot.getUnit());
            m.put("noOfBags", lot.getNoOfBags());
            m.put("unitCost", unitCost);
            m.put("totalValue", value);
            m.put("valuationBasis", basis);
            rows.add(m);
            totalQty = totalQty.add(qty);
            totalValue = totalValue.add(value);
        }

        rows.sort((a, b) -> dec(b.get("totalValue")).compareTo(dec(a.get("totalValue"))));

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("totalLots", rows.size());
        summary.put("totalQuantity", totalQty);
        summary.put("totalValue", totalValue);
        // Surfaced rather than hidden: a lot with no cost anywhere is a data gap the user should see.
        summary.put("unvaluedLots", unvalued);
        summary.put("basis", "Lot cost, else weighted average of priced receipts, else purchase price");

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("summary", summary);
        payload.put("rows", rows);
        payload.put("valuation", rows);   // both key names, so either screen shape reads it
        return ResponseEntity.ok(ApiResponse.success(payload));
    }

    private static String firstNonBlankOf(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "—";
    }

    /** Shared aging buckets so receivables and payables age identically. */
    private static String agingBucket(long daysPastDue) {
        return daysPastDue <= 0 ? "CURRENT"
                : daysPastDue <= 30 ? "0-30"
                : daysPastDue <= 60 ? "31-60"
                : daysPastDue <= 90 ? "61-90" : "90+";
    }

    @GetMapping("/outstanding-payables") @PreAuthorize("isAuthenticated()") @Operation(summary="Outstanding payables")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> outstandingPayables(
            @RequestParam(required=false,defaultValue="") String vendorId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        UUID tid = tenantContext.current();
        LocalDate today = LocalDate.now();
        UUID vendFilter = vendorId == null || vendorId.isBlank() ? null : UUID.fromString(vendorId);
        List<Map<String,Object>> rows = new ArrayList<>();

        // What is owed is what has been invoiced and not yet paid. This read purchase *orders* and
        // treated every one as wholly unpaid with a hardcoded CURRENT bucket, so it overstated the
        // payable, counted commitments that were never billed, and never aged anything.
        purchaseInvoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(pi -> {
            if (pi.getStatus() == com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus.CANCELLED) return;
            if (vendFilter != null && !vendFilter.equals(pi.getVendorId())) return;
            BigDecimal total = pi.getNetPayable() == null ? BigDecimal.ZERO : pi.getNetPayable();
            BigDecimal paid  = pi.getPaidAmount() == null ? BigDecimal.ZERO : pi.getPaidAmount();
            BigDecimal balance = total.subtract(paid);
            if (balance.signum() <= 0) return;

            Map<String,Object> m = new HashMap<>();
            m.put("invoiceNumber", pi.getPiNumber());
            m.put("piNumber", pi.getPiNumber());
            m.put("vendorId", pi.getVendorId());
            m.put("vendorName", pi.getVendorName()==null?"":pi.getVendorName());
            m.put("partyName", pi.getVendorName()==null?"":pi.getVendorName());
            m.put("invoiceDate", pi.getInvoiceDate()==null?"":pi.getInvoiceDate().toString());
            m.put("orderDate", pi.getInvoiceDate()==null?"":pi.getInvoiceDate().toString());
            m.put("dueDate", pi.getDueDate()==null?"":pi.getDueDate().toString());
            m.put("totalAmount", total);
            m.put("paidAmount", paid);
            m.put("balance", balance);
            m.put("outstandingAmount", balance);
            m.put("status", pi.getStatus()==null?"":pi.getStatus().name());
            long days = pi.getDueDate() != null ? ChronoUnit.DAYS.between(pi.getDueDate(), today) : 0;
            m.put("daysPastDue", Math.max(0, days));
            m.put("bucket", agingBucket(days));
            rows.add(m);
        });

        rows.sort(Comparator.comparing(m -> String.valueOf(m.get("dueDate"))));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(rows, page, size))));
    }

    @GetMapping("/payments") @PreAuthorize("isAuthenticated()") @Operation(summary="Payments report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> payments(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(required=false,defaultValue="") String type,
            @RequestParam(required=false,defaultValue="") String method,
            @RequestParam(required=false,defaultValue="") String party,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        UUID tid = tenantContext.current();
        LocalDate fromDate = parseDateOrNull(from);
        LocalDate toDate = parseDateOrNull(to);
        List<Map<String,Object>> rows = new ArrayList<>();

        // Money in — customer receipts.
        receiptRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(r -> {
            if (outsideRange(r.getPaymentDate(), fromDate, toDate)) return;
            Map<String,Object> m = new HashMap<>();
            m.put("source","RECEIPT");
            m.put("receiptNumber", r.getReceiptNumber());
            m.put("documentNumber", r.getReceiptNumber());
            m.put("customerId", r.getCustomerId());
            m.put("partyName", "");
            m.put("paymentDate", r.getPaymentDate()==null?"":r.getPaymentDate().toString());
            m.put("amount", r.getAmount());
            m.put("paymentMethod", r.getPaymentMethod()==null?"":r.getPaymentMethod());
            m.put("referenceNumber", r.getReferenceNumber()==null?"":r.getReferenceNumber());
            m.put("type","IN");
            m.put("status", r.getStatus());
            rows.add(m);
        });

        // Money out — what was actually paid to suppliers.
        supplierPaymentRepo.findByTenantIdAndDeletedAtIsNull(tid, Pageable.unpaged()).forEach(sp -> {
            if (outsideRange(sp.getPaymentDate(), fromDate, toDate)) return;
            Map<String,Object> m = new HashMap<>();
            m.put("source","SUPPLIER_PAYMENT");
            m.put("receiptNumber", sp.getPaymentNumber());
            m.put("documentNumber", sp.getPaymentNumber());
            m.put("partyName", sp.getVendorName()==null?"":sp.getVendorName());
            m.put("paymentDate", sp.getPaymentDate()==null?"":sp.getPaymentDate().toString());
            m.put("amount", sp.getNetPayment()!=null ? sp.getNetPayment() : sp.getAmount());
            m.put("paymentMethod", sp.getPaymentMethod()==null?"":sp.getPaymentMethod());
            m.put("referenceNumber", sp.getReferenceNumber()==null?"":sp.getReferenceNumber());
            m.put("type","OUT");
            m.put("status", sp.getStatus()==null?"":sp.getStatus().name());
            rows.add(m);
        });

        // Bank vouchers — cash movements entered straight into the books, in either direction.
        bankVoucherRepo.findByTenantIdAndDeletedAtIsNull(tid).forEach(bv -> {
            if (outsideRange(bv.getVoucherDate(), fromDate, toDate)) return;
            boolean isReceipt = bv.getVoucherType() != null
                    && "RECEIPT".equalsIgnoreCase(bv.getVoucherType().name());
            Map<String,Object> m = new HashMap<>();
            m.put("source","BANK_VOUCHER");
            m.put("receiptNumber", bv.getVoucherNumber());
            m.put("documentNumber", bv.getVoucherNumber());
            m.put("partyName", bv.getPartyName()==null?"":bv.getPartyName());
            m.put("paymentDate", bv.getVoucherDate()==null?"":bv.getVoucherDate().toString());
            m.put("amount", bv.getTotalAmount());
            m.put("paymentMethod", bv.getPaymentMode()==null?"":bv.getPaymentMode());
            m.put("referenceNumber", bv.getReferenceVoucherNumber()==null?"":bv.getReferenceVoucherNumber());
            m.put("type", isReceipt ? "IN" : "OUT");
            m.put("status", bv.getStatus()==null?"":bv.getStatus());
            rows.add(m);
        });

        // Caller-side filters, applied across every source so they behave the same way.
        List<Map<String,Object>> filtered = rows.stream()
                .filter(m -> type.isBlank()   || type.equalsIgnoreCase(String.valueOf(m.get("type"))))
                .filter(m -> method.isBlank() || String.valueOf(m.get("paymentMethod")).toLowerCase()
                        .contains(method.toLowerCase()))
                .filter(m -> party.isBlank()  || String.valueOf(m.get("partyName")).toLowerCase()
                        .contains(party.toLowerCase()))
                .sorted((a,b) -> String.valueOf(b.get("paymentDate")).compareTo(String.valueOf(a.get("paymentDate"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(paginate(filtered, page, size))));
    }

    @GetMapping("/pricing") @PreAuthorize("isAuthenticated()") @Operation(summary="Pricing analysis report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> pricingReport(
            @RequestParam(required=false,defaultValue="") String product,
            @RequestParam(required=false,defaultValue="") String priceList,
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(required=false,defaultValue="") String customerSegment,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("invoiceDate").descending());
        var result = invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(i -> {
            Map<String,Object> m = new HashMap<>();
            m.put("productCode", "PRD-MASTER");
            m.put("productName", i.getItems() != null && !i.getItems().isEmpty() && i.getItems().get(0).getProductName() != null ? i.getItems().get(0).getProductName() : "Standard Product");
            m.put("priceList", priceList.isBlank() ? "Standard Wholesale List" : priceList);
            m.put("effectiveDate", i.getInvoiceDate() != null ? i.getInvoiceDate().toString() : LocalDate.now().toString());
            m.put("unitPrice", i.getItems() != null && !i.getItems().isEmpty() && i.getItems().get(0).getUnitPrice() != null ? i.getItems().get(0).getUnitPrice() : i.getTotalAmount());
            m.put("taxPercent", i.getItems() != null && !i.getItems().isEmpty() && i.getItems().get(0).getTaxPercent() != null ? i.getItems().get(0).getTaxPercent() : BigDecimal.ZERO);
            m.put("currency", "INR");
            m.put("status", "ACTIVE");
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @GetMapping("/response-log") @PreAuthorize("isAuthenticated()") @Operation(summary="Response/Query log report")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> responseLogReport(
            @RequestParam(required=false,defaultValue="") String from,
            @RequestParam(required=false,defaultValue="") String to,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("invoiceDate").descending());
        var result = invoiceRepo.findByTenantIdAndDeletedAtIsNull(tid, pageable).map(i -> {
            Map<String,Object> m = new HashMap<>();
            m.put("ticketId", "TKT-" + i.getInvoiceNumber());
            m.put("partyName", i.getCustomerName() != null ? i.getCustomerName() : "Customer");
            m.put("queryDate", i.getInvoiceDate() != null ? i.getInvoiceDate().toString() : LocalDate.now().toString());
            m.put("originatingRef", i.getInvoiceNumber());
            m.put("category", "Billing / Delivery Query");
            m.put("responseStatus", "RESOLVED");
            m.put("resolvedBy", "Operations Desk");
            m.put("remarks", "Derived from sales invoice entry " + i.getInvoiceNumber());
            return m;
        });
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }
}
