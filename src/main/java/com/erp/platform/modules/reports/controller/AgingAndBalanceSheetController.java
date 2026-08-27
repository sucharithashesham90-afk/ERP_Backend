package com.erp.platform.modules.reports.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aging and balance sheet.
 *
 * <p>The Accounts Receivable Aging, Accounts Payable Aging and Balance Sheet screens have been
 * calling /reports/debtors-aging, /reports/creditors-aging and /reports/balance-sheet since they
 * were written. None of the three existed, so all three reported "endpoint not found" — the screens
 * were complete and had nothing behind them.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports - Aging & Balance Sheet")
public class AgingAndBalanceSheetController {

    private final InvoiceRepository invoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final AccountRepository accountRepository;
    private final com.erp.platform.modules.accounting.repository.JournalEntryLineRepository journalEntryLineRepository;
    private final com.erp.platform.modules.accounting.repository.LedgerOpeningBalanceRepository openingBalanceRepository;
    private final TenantContext tenantContext;

    /**
     * A ledger's opening balance, signed the way a debit is positive. It lives on
     * ledger_opening_balances (one row per location), not on the account itself, so the rows for the
     * account are summed and DEBIT/CREDIT is read off each row.
     */
    private BigDecimal openingBalanceOf(UUID tenantId, UUID accountId) {
        BigDecimal total = BigDecimal.ZERO;
        for (var ob : openingBalanceRepository.findByTenantIdAndAccountIdAndDeletedAtIsNull(tenantId, accountId)) {
            BigDecimal amt = nz(ob.getOpeningBalance());
            total = "CREDIT".equalsIgnoreCase(ob.getBalanceType()) ? total.subtract(amt) : total.add(amt);
        }
        return total;
    }

    // ── Trial Balance ────────────────────────────────────────────────────────

    @GetMapping("/trial-balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Trial Balance report with Opening, Period and Closing balances per account")
    public ResponseEntity<ApiResponse<Map<String, Object>>> trialBalance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "true") boolean hideZeroBalance,
            @RequestParam(required = false) String group) {

        UUID tenantId = tenantContext.current();
        LocalDate startDate = parse(from);
        LocalDate endDate = parse(to);

        List<Account> accounts = accountRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<Map<String, Object>> rows = new ArrayList<>();

        BigDecimal totalOpeningDebit = BigDecimal.ZERO;
        BigDecimal totalOpeningCredit = BigDecimal.ZERO;
        BigDecimal totalPeriodDebit = BigDecimal.ZERO;
        BigDecimal totalPeriodCredit = BigDecimal.ZERO;
        BigDecimal totalClosingDebit = BigDecimal.ZERO;
        BigDecimal totalClosingCredit = BigDecimal.ZERO;

        for (Account acc : accounts) {
            if (group != null && !group.isBlank() && !acc.getType().equalsIgnoreCase(group) &&
                    (acc.getSubType() == null || !acc.getSubType().equalsIgnoreCase(group))) {
                continue;
            }

            // Opening balance before startDate
            BigDecimal opDebit = BigDecimal.ZERO;
            BigDecimal opCredit = BigDecimal.ZERO;
            BigDecimal opening = openingBalanceOf(tenantId, acc.getId());
            if (opening.signum() >= 0) opDebit = opening; else opCredit = opening.negate();

            var priorLines = journalEntryLineRepository
                    .findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountId(
                            tenantId, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, startDate, acc.getId());
            for (var pl : priorLines) {
                opDebit = opDebit.add(nz(pl.getDebitAmount()));
                opCredit = opCredit.add(nz(pl.getCreditAmount()));
            }

            // Period activity between startDate and endDate
            BigDecimal periodDebit = BigDecimal.ZERO;
            BigDecimal periodCredit = BigDecimal.ZERO;
            var periodLines = journalEntryLineRepository
                    .findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountIdOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                            tenantId, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, startDate, endDate, acc.getId());
            for (var pl : periodLines) {
                periodDebit = periodDebit.add(nz(pl.getDebitAmount()));
                periodCredit = periodCredit.add(nz(pl.getCreditAmount()));
            }

            BigDecimal closingDebit = opDebit.add(periodDebit);
            BigDecimal closingCredit = opCredit.add(periodCredit);

            BigDecimal netClosing = closingDebit.subtract(closingCredit);
            BigDecimal balance = netClosing;

            if (hideZeroBalance && closingDebit.compareTo(BigDecimal.ZERO) == 0 && closingCredit.compareTo(BigDecimal.ZERO) == 0 && opDebit.compareTo(BigDecimal.ZERO) == 0 && opCredit.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            Map<String, Object> r = new LinkedHashMap<>();
            r.put("accountId", acc.getId());
            r.put("accountCode", blankTo(acc.getCode(), ""));
            r.put("accountName", blankTo(acc.getName(), ""));
            r.put("accountType", blankTo(acc.getType(), "OTHER"));
            r.put("subType", blankTo(acc.getSubType(), ""));
            r.put("groupCode", blankTo(acc.getType(), ""));
            r.put("groupName", blankTo(acc.getSubType(), acc.getType()));
            r.put("openingDebit", opDebit);
            r.put("openingCredit", opCredit);
            r.put("openingBalance", opDebit.subtract(opCredit));
            r.put("periodDebit", periodDebit);
            r.put("periodCredit", periodCredit);
            r.put("closingDebit", closingDebit);
            r.put("closingCredit", closingCredit);
            r.put("balance", balance);
            rows.add(r);

            totalOpeningDebit = totalOpeningDebit.add(opDebit);
            totalOpeningCredit = totalOpeningCredit.add(opCredit);
            totalPeriodDebit = totalPeriodDebit.add(periodDebit);
            totalPeriodCredit = totalPeriodCredit.add(periodCredit);
            totalClosingDebit = totalClosingDebit.add(closingDebit);
            totalClosingCredit = totalClosingCredit.add(closingCredit);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", startDate.toString());
        out.put("to", endDate.toString());
        out.put("rows", rows);
        out.put("totalOpeningDebit", totalOpeningDebit);
        out.put("totalOpeningCredit", totalOpeningCredit);
        out.put("totalPeriodDebit", totalPeriodDebit);
        out.put("totalPeriodCredit", totalPeriodCredit);
        out.put("totalClosingDebit", totalClosingDebit);
        out.put("totalClosingCredit", totalClosingCredit);

        return ResponseEntity.ok(ApiResponse.success(out));
    }

    // ── Profit and Loss ──────────────────────────────────────────────────────

    @GetMapping("/profit-and-loss")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Profit & Loss statement: Income vs Expenses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> profitAndLoss(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        UUID tenantId = tenantContext.current();
        LocalDate startDate = parse(from);
        LocalDate endDate = parse(to);

        List<Account> accounts = accountRepository.findByTenantIdAndDeletedAtIsNull(tenantId);

        // The screen groups these into subtotal panels client-side (by subType), so each line
        // just needs to carry its own subType rather than arrive pre-grouped.
        List<Map<String, Object>> incomeItems = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;

        List<Map<String, Object>> expenseItems = new ArrayList<>();
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Account acc : accounts) {
            String type = acc.getType() != null ? acc.getType().toUpperCase() : "";
            if (!"INCOME".equals(type) && !"REVENUE".equals(type) && !"EXPENSE".equals(type)) {
                continue;
            }

            BigDecimal periodDebit = BigDecimal.ZERO;
            BigDecimal periodCredit = BigDecimal.ZERO;
            var lines = journalEntryLineRepository
                    .findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountIdOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                            tenantId, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, startDate, endDate, acc.getId());
            for (var pl : lines) {
                periodDebit = periodDebit.add(nz(pl.getDebitAmount()));
                periodCredit = periodCredit.add(nz(pl.getCreditAmount()));
            }

            String subType = blankTo(acc.getSubType(), "General");

            // A P&L reports what happened between the two dates. An opening balance is what was
            // carried in, so it has no business padding a line here.
            if ("INCOME".equals(type) || "REVENUE".equals(type)) {
                BigDecimal netIncome = periodCredit.subtract(periodDebit);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("code", acc.getCode());
                item.put("name", acc.getName());
                item.put("subType", subType);
                item.put("amount", netIncome);

                incomeItems.add(item);
                totalIncome = totalIncome.add(netIncome);
            } else {
                BigDecimal netExpense = periodDebit.subtract(periodCredit);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("code", acc.getCode());
                item.put("name", acc.getName());
                item.put("subType", subType);
                item.put("amount", netExpense);

                expenseItems.add(item);
                totalExpense = totalExpense.add(netExpense);
            }
        }

        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", startDate.toString());
        out.put("to", endDate.toString());
        out.put("income", incomeItems);
        out.put("totalIncome", totalIncome);
        out.put("expenses", expenseItems);
        out.put("totalExpense", totalExpense);
        out.put("netProfit", netProfit);

        return ResponseEntity.ok(ApiResponse.success(out));
    }

    // ── Cash Flow ────────────────────────────────────────────────────────────

    @GetMapping("/cash-flow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cash Flow statement: Operating, Investing & Financing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cashFlow(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        UUID tenantId = tenantContext.current();
        LocalDate startDate = parse(from);
        LocalDate endDate = parse(to);

        // Find bank and cash accounts. The seeded chart uses the sub-type "BANK"; "BANK_ACCOUNT"
        // is the spelling some imports use, so both are asked for.
        List<Account> bankAccounts = new ArrayList<>();
        for (String subType : List.of("BANK", "BANK_ACCOUNT", "CASH")) {
            bankAccounts.addAll(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, subType));
        }

        BigDecimal openingCash = BigDecimal.ZERO;
        BigDecimal closingCash = BigDecimal.ZERO;

        for (Account b : bankAccounts) {
            BigDecimal bOp = openingBalanceOf(tenantId, b.getId());
            var prior = journalEntryLineRepository
                    .findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountId(
                            tenantId, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, startDate, b.getId());
            for (var pl : prior) {
                bOp = bOp.add(nz(pl.getDebitAmount())).subtract(nz(pl.getCreditAmount()));
            }
            openingCash = openingCash.add(bOp);

            BigDecimal bPeriod = BigDecimal.ZERO;
            var period = journalEntryLineRepository
                    .findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountIdOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
                            tenantId, com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus.POSTED, startDate, endDate, b.getId());
            for (var pl : period) {
                bPeriod = bPeriod.add(nz(pl.getDebitAmount())).subtract(nz(pl.getCreditAmount()));
            }
            closingCash = closingCash.add(bOp.add(bPeriod));
        }

        BigDecimal netChange = closingCash.subtract(openingCash);

        // Operating activities
        List<Map<String, Object>> adjustments = new ArrayList<>();
        List<Map<String, Object>> wcChanges = new ArrayList<>();
        BigDecimal operatingTotal = netChange;

        Map<String, Object> operating = new LinkedHashMap<>();
        operating.put("netProfit", netChange);
        operating.put("adjustments", adjustments);
        operating.put("workingCapitalChanges", wcChanges);
        operating.put("total", operatingTotal);

        // Investing activities
        List<Map<String, Object>> investingItems = new ArrayList<>();
        Map<String, Object> investing = new LinkedHashMap<>();
        investing.put("items", investingItems);
        investing.put("total", BigDecimal.ZERO);

        // Financing activities
        List<Map<String, Object>> financingItems = new ArrayList<>();
        Map<String, Object> financing = new LinkedHashMap<>();
        financing.put("items", financingItems);
        financing.put("total", BigDecimal.ZERO);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", startDate.toString());
        out.put("to", endDate.toString());
        out.put("operating", operating);
        out.put("investing", investing);
        out.put("financing", financing);
        out.put("netChange", netChange);
        out.put("openingCash", openingCash);
        out.put("closingCash", closingCash);

        return ResponseEntity.ok(ApiResponse.success(out));
    }

    // ── Receivables ──────────────────────────────────────────────────────────

    @GetMapping("/debtors-aging")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accounts receivable aging, bucketed by days overdue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debtorsAging(
            @RequestParam(required = false) String asOf,
            @RequestParam(required = false, defaultValue = "") String filter) {

        UUID tenantId = tenantContext.current();
        LocalDate on = parse(asOf);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (var inv : invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000))) {
            BigDecimal due = nz(inv.getTotalAmount()).subtract(nz(inv.getPaidAmount()));
            if (due.signum() <= 0) continue;                       // settled invoices are not aging
            if (inv.getInvoiceDate() != null && inv.getInvoiceDate().isAfter(on)) continue;

            String party = blankTo(inv.getCustomerName(), "General Customer");
            if (!matches(filter, party)) continue;

            LocalDate dueDate = inv.getDueDate() != null ? inv.getDueDate() : inv.getInvoiceDate();
            long overdue = dueDate == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(dueDate, on));

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("invoiceNumber", inv.getInvoiceNumber());
            m.put("customerName", party);
            m.put("invoiceDate", inv.getInvoiceDate() == null ? null : inv.getInvoiceDate().toString());
            m.put("dueDate", dueDate == null ? null : dueDate.toString());
            m.put("totalAmount", nz(inv.getTotalAmount()));
            m.put("balanceDue", due);
            m.put("overdueDays", overdue);
            m.put("bucket", bucketName(overdue));
            rows.add(m);
        }

        return ResponseEntity.ok(ApiResponse.success(assemble(on, filter, rows, "customerName")));
    }

    // ── Payables ─────────────────────────────────────────────────────────────

    @GetMapping("/creditors-aging")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accounts payable aging, bucketed by days overdue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> creditorsAging(
            @RequestParam(required = false) String asOf,
            @RequestParam(required = false, defaultValue = "") String filter) {

        UUID tenantId = tenantContext.current();
        LocalDate on = parse(asOf);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (var inv : purchaseInvoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000))) {
            BigDecimal due = nz(inv.getTotalAmount()).subtract(nz(inv.getPaidAmount()));
            if (due.signum() <= 0) continue;
            if (inv.getInvoiceDate() != null && inv.getInvoiceDate().isAfter(on)) continue;

            String party = blankTo(inv.getVendorName(), "General Supplier");
            if (!matches(filter, party)) continue;

            LocalDate dueDate = inv.getDueDate() != null ? inv.getDueDate() : inv.getInvoiceDate();
            long overdue = dueDate == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(dueDate, on));

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("invoiceNumber", blankTo(inv.getPiNumber(), inv.getVendorInvoiceNumber()));
            m.put("customerName", party);      // the screen reads one field name for the party
            m.put("vendorName", party);
            m.put("invoiceDate", inv.getInvoiceDate() == null ? null : inv.getInvoiceDate().toString());
            m.put("dueDate", dueDate == null ? null : dueDate.toString());
            m.put("totalAmount", nz(inv.getTotalAmount()));
            m.put("balanceDue", due);
            m.put("overdueDays", overdue);
            m.put("bucket", bucketName(overdue));
            rows.add(m);
        }

        return ResponseEntity.ok(ApiResponse.success(assemble(on, filter, rows, "customerName")));
    }

    // ── Balance sheet ────────────────────────────────────────────────────────

    @GetMapping("/balance-sheet")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Balance sheet as at a date")
    public ResponseEntity<ApiResponse<Map<String, Object>>> balanceSheet(
            @RequestParam(required = false) String asOf) {

        UUID tenantId = tenantContext.current();
        LocalDate on = parse(asOf);

        List<Map<String, Object>> assets = new ArrayList<>();
        List<Map<String, Object>> liabilities = new ArrayList<>();
        List<Map<String, Object>> equity = new ArrayList<>();
        BigDecimal ta = BigDecimal.ZERO, tl = BigDecimal.ZERO, te = BigDecimal.ZERO;

        for (var a : accountRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000))) {
            BigDecimal bal = nz(a.getBalance());
            if (bal.signum() == 0) continue;                 // a nil account is noise on the face of it

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("code", a.getCode());
            line.put("name", a.getName());
            line.put("subType", blankTo(a.getSubType(), blankTo(a.getGroupName(), "Other")));
            line.put("balance", bal);

            String type = a.getType() == null ? "" : a.getType().toUpperCase();
            switch (type) {
                case "ASSET"     -> { assets.add(line);      ta = ta.add(bal); }
                case "LIABILITY" -> { liabilities.add(line); tl = tl.add(bal); }
                case "EQUITY"    -> { equity.add(line);      te = te.add(bal); }
                default -> { /* income and expense belong on the P&L, not here */ }
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("asOf", on.toString());
        out.put("assets", assets);
        out.put("totalAssets", ta);
        out.put("liabilities", liabilities);
        out.put("totalLiabilities", tl);
        out.put("equity", equity);
        out.put("totalEquity", te);
        out.put("totalLiabilitiesAndEquity", tl.add(te));
        // Said plainly rather than left for the reader to subtract: a sheet that does not balance is
        // the single most important thing on it.
        out.put("balanced", ta.compareTo(tl.add(te)) == 0);
        return ResponseEntity.ok(ApiResponse.success(out));
    }

    // ── Shared ───────────────────────────────────────────────────────────────

    /** Bucket the rows by party and total each bucket, which is the shape both screens render. */
    private static Map<String, Object> assemble(LocalDate on, String filter,
                                                List<Map<String, Object>> rows, String partyField) {
        Map<String, Map<String, Object>> byParty = new LinkedHashMap<>();
        BigDecimal sCur = BigDecimal.ZERO, s30 = BigDecimal.ZERO, s60 = BigDecimal.ZERO,
                   s90 = BigDecimal.ZERO, s90p = BigDecimal.ZERO, sTot = BigDecimal.ZERO;

        for (Map<String, Object> r : rows) {
            String party = String.valueOf(r.get(partyField));
            var g = byParty.computeIfAbsent(party, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("partyName", k);
                m.put("current", BigDecimal.ZERO);
                m.put("days1to30", BigDecimal.ZERO);
                m.put("days31to60", BigDecimal.ZERO);
                m.put("days61to90", BigDecimal.ZERO);
                m.put("days90plus", BigDecimal.ZERO);
                m.put("total", BigDecimal.ZERO);
                m.put("invoices", new ArrayList<Map<String, Object>>());
                return m;
            });

            BigDecimal due = (BigDecimal) r.get("balanceDue");
            String key = bucketKey((Long) r.get("overdueDays"));
            g.put(key, ((BigDecimal) g.get(key)).add(due));
            g.put("total", ((BigDecimal) g.get("total")).add(due));
            @SuppressWarnings("unchecked")
            var invs = (List<Map<String, Object>>) g.get("invoices");
            invs.add(r);

            switch (key) {
                case "current"    -> sCur = sCur.add(due);
                case "days1to30"  -> s30 = s30.add(due);
                case "days31to60" -> s60 = s60.add(due);
                case "days61to90" -> s90 = s90.add(due);
                default           -> s90p = s90p.add(due);
            }
            sTot = sTot.add(due);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("current", sCur);
        summary.put("days1to30", s30);
        summary.put("days31to60", s60);
        summary.put("days61to90", s90);
        summary.put("days90plus", s90p);
        summary.put("total", sTot);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("asOf", on.toString());
        out.put("filter", filter == null ? "" : filter);
        out.put("summary", summary);
        out.put("grouped", new ArrayList<>(byParty.values()));
        return out;
    }

    private static String bucketKey(long overdueDays) {
        if (overdueDays <= 0) return "current";
        if (overdueDays <= 30) return "days1to30";
        if (overdueDays <= 60) return "days31to60";
        if (overdueDays <= 90) return "days61to90";
        return "days90plus";
    }

    private static String bucketName(long overdueDays) {
        if (overdueDays <= 0) return "Current";
        if (overdueDays <= 30) return "1-30 days";
        if (overdueDays <= 60) return "31-60 days";
        if (overdueDays <= 90) return "61-90 days";
        return "90+ days";
    }

    private static LocalDate parse(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        try { return LocalDate.parse(s); } catch (Exception e) { return LocalDate.now(); }
    }

    private static BigDecimal nz(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    private static String blankTo(String v, String fallback) {
        return v != null && !v.isBlank() ? v : fallback;
    }

    private static boolean matches(String filter, String value) {
        if (filter == null || filter.isBlank()) return true;
        return value != null && value.toLowerCase().contains(filter.toLowerCase());
    }
}
