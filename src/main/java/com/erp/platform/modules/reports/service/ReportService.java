package com.erp.platform.modules.reports.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.crm.entity.Lead.LeadStatus;
import com.erp.platform.modules.crm.repository.LeadRepository;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.inventory.entity.StockItem;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final SalesOrderRepository salesOrderRepo;
    private final InvoiceRepository invoiceRepo;
    private final PurchaseOrderRepository poRepo;
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;
    private final CustomerRepository customerRepo;
    private final VendorRepository vendorRepo;
    private final LeadRepository leadRepo;
    private final EmployeeRepository employeeRepo;
    private final AccountRepository accountRepo;
    private final JournalEntryRepository journalEntryRepo;
    private final StockItemRepository stockItemRepo;
    private final com.erp.platform.modules.workflow.repository.ApprovalInstanceRepository approvalRepo;
    private final TenantContext tenantContext;

    public Map<String, Object> getDashboardSummary() {
        UUID tenantId = tenantContext.current();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("salesThisMonth",         salesOrderRepo.sumTotalByDateRange(tenantId, monthStart, today));
        summary.put("salesThisYear",          salesOrderRepo.sumTotalByDateRange(tenantId, yearStart, today));
        summary.put("invoicesThisMonth",      invoiceRepo.sumTotalByDateRange(tenantId, monthStart, today));
        summary.put("outstandingReceivables", invoiceRepo.totalOutstanding(tenantId));
        summary.put("purchasesThisMonth",     poRepo.sumTotalByDateRange(tenantId, monthStart, today));
        summary.put("totalCustomers",         customerRepo.count());
        summary.put("totalVendors",           vendorRepo.count());
        summary.put("totalEmployees",
                employeeRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, EmployeeStatus.ACTIVE));
        summary.put("newLeads",
                leadRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.NEW));
        summary.put("wonLeads",
                leadRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.WON));
        summary.put("lostLeads",
                leadRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.LOST));
        summary.put("openOrders",
                salesOrderRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, SalesOrderStatus.CONFIRMED)
                + salesOrderRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, SalesOrderStatus.PROCESSING));
        summary.put("pendingPOs",
                poRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, POStatus.SENT)
                + poRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, POStatus.CONFIRMED));
        // The mobile dashboard has a Pending Approvals tile and nothing ever filled it, because the
        // summary did not carry the figure.
        summary.put("pendingApprovals",
                approvalRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId,
                        com.erp.platform.modules.workflow.entity.ApprovalInstance.ApprovalStatus.PENDING));
        summary.put("overdueInvoices",
                invoiceRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, InvoiceStatus.OVERDUE));
        summary.put("paidInvoicesThisMonth",
                invoiceRepo.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, InvoiceStatus.PAID));

        return summary;
    }

    /**
     * Best-selling products by invoiced revenue.
     *
     * <p>The dashboard donut shipped with five hard-coded product names, so every tenant saw the
     * same imaginary catalogue. topProductsByRevenue has been on the repository the whole time and
     * nothing called it.
     */
    public List<Map<String, Object>> getTopProducts(int limit, LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.withDayOfYear(1);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : invoiceRepo.topProductsByRevenue(tenantId, start, end, PageRequest.of(0, Math.max(1, limit)))) {
            String name = row[0] != null ? row[0].toString() : null;
            if (name == null || name.isBlank()) continue;     // unnamed line items are not a product
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("quantity", row[1] != null ? row[1] : BigDecimal.ZERO);
            m.put("value", row[2] != null ? row[2] : BigDecimal.ZERO);
            out.add(m);
        }
        return out;
    }

    /** Highest-billed customers by invoiced revenue, over the same window as the products chart. */
    public List<Map<String, Object>> getTopCustomers(int limit, LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.withDayOfYear(1);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : invoiceRepo.topCustomersByRevenue(tenantId, start, end, PageRequest.of(0, Math.max(1, limit)))) {
            String name = row[0] != null ? row[0].toString() : null;
            if (name == null || name.isBlank()) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("value", row[1] != null ? row[1] : BigDecimal.ZERO);
            out.add(m);
        }
        return out;
    }

    /**
     * Cash position off the chart of accounts, for the book-balance and cash-equation cards that
     * were previously showing five fixed numbers regardless of the books.
     */
    public Map<String, Object> getFinancialPosition() {
        UUID tenantId = tenantContext.current();

        BigDecimal cashAndBank = BigDecimal.ZERO;
        BigDecimal debitBalance = BigDecimal.ZERO;
        BigDecimal creditBalance = BigDecimal.ZERO;

        for (Account a : accountRepo.findByTenantIdAndDeletedAtIsNull(tenantId)) {
            BigDecimal bal = a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO;
            String type = a.getType() != null ? a.getType().toUpperCase() : "";
            String subType = a.getSubType() != null ? a.getSubType().toUpperCase() : "";

            if (subType.equals("BANK") || subType.equals("BANK_ACCOUNT") || subType.equals("CASH")) {
                cashAndBank = cashAndBank.add(bal);
            }
            // Assets and expenses carry a debit balance; everything else carries a credit balance.
            if (type.equals("ASSET") || type.equals("EXPENSE")) {
                debitBalance = debitBalance.add(bal);
            } else if (!type.isBlank()) {
                creditBalance = creditBalance.add(bal);
            }
        }

        BigDecimal receivable = invoiceRepo.totalOutstanding(tenantId);
        BigDecimal payable = purchaseInvoiceRepo.totalPayable(tenantId);
        if (receivable == null) receivable = BigDecimal.ZERO;
        if (payable == null) payable = BigDecimal.ZERO;

        BigDecimal combined = receivable.add(payable);
        BigDecimal recoveryPercent = combined.signum() > 0
                ? receivable.multiply(BigDecimal.valueOf(100)).divide(combined, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("cashAndBank", cashAndBank);
        m.put("debitBalance", debitBalance);
        m.put("creditBalance", creditBalance);
        m.put("receivable", receivable);
        m.put("payable", payable);
        m.put("recoveryPercent", recoveryPercent);
        return m;
    }

    public Map<String, Object> getMonthlySalesTrend(int year) {
        UUID tenantId = tenantContext.current();
        Map<String, Object> trend = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            BigDecimal sales = salesOrderRepo.sumTotalByDateRange(tenantId, start, end);
            BigDecimal purchases = poRepo.sumTotalByDateRange(tenantId, start, end);
            trend.put(start.getMonth().name(), Map.of("sales", sales, "purchases", purchases));
        }
        return trend;
    }

    /**
     * Debtors aging report — outstanding invoices bucketed by days overdue.
     * Grouped by customer with per-party subtotals.
     * Buckets: Current (not yet due), 1-30, 31-60, 61-90, 90+ days.
     */
    public Map<String, Object> getDebtorsAging(LocalDate asOf, String partyFilter) {
        if (asOf == null) asOf = LocalDate.now();
        UUID tenantId = tenantContext.current();
        final LocalDate effectiveDate = asOf;

        List<Invoice> outstanding = invoiceRepo.findOutstandingInvoices(tenantId);

        if (partyFilter != null && !partyFilter.isBlank()) {
            String fl = partyFilter.toLowerCase();
            outstanding = outstanding.stream()
                    .filter(inv -> inv.getCustomerName() != null
                            && inv.getCustomerName().toLowerCase().contains(fl))
                    .collect(Collectors.toList());
        }

        BigDecimal totCurrent    = BigDecimal.ZERO;
        BigDecimal totDays1to30  = BigDecimal.ZERO;
        BigDecimal totDays31to60 = BigDecimal.ZERO;
        BigDecimal totDays61to90 = BigDecimal.ZERO;
        BigDecimal totDays90plus = BigDecimal.ZERO;

        Map<String, List<Invoice>> byCustomer = outstanding.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getCustomerName() != null ? inv.getCustomerName() : "Unknown",
                        LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> grouped = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map.Entry<String, List<Invoice>> entry : byCustomer.entrySet()) {
            String partyName = entry.getKey();
            BigDecimal pCur = BigDecimal.ZERO, p1 = BigDecimal.ZERO,
                       p31  = BigDecimal.ZERO, p61 = BigDecimal.ZERO, p90 = BigDecimal.ZERO;
            List<Map<String, Object>> invList = new ArrayList<>();

            for (Invoice inv : entry.getValue()) {
                BigDecimal balance = inv.getBalanceDue();
                LocalDate dueDate = inv.getDueDate();
                long overdueDays = dueDate != null ? ChronoUnit.DAYS.between(dueDate, effectiveDate) : 0;

                if      (overdueDays <= 0)  pCur = pCur.add(balance);
                else if (overdueDays <= 30) p1   = p1.add(balance);
                else if (overdueDays <= 60) p31  = p31.add(balance);
                else if (overdueDays <= 90) p61  = p61.add(balance);
                else                        p90  = p90.add(balance);

                String bucket = overdueDays <= 0 ? "Current"
                        : overdueDays <= 30 ? "1-30 days"
                        : overdueDays <= 60 ? "31-60 days"
                        : overdueDays <= 90 ? "61-90 days" : "90+ days";

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("invoiceNumber", inv.getInvoiceNumber());
                row.put("customerName",  inv.getCustomerName());
                row.put("invoiceDate",   inv.getInvoiceDate());
                row.put("dueDate",       dueDate);
                row.put("totalAmount",   inv.getTotalAmount());
                row.put("balanceDue",    balance);
                row.put("overdueDays",   overdueDays > 0 ? overdueDays : 0);
                row.put("bucket",        bucket);
                invList.add(row);
                details.add(row);
            }

            BigDecimal pTotal = pCur.add(p1).add(p31).add(p61).add(p90);
            totCurrent    = totCurrent.add(pCur);
            totDays1to30  = totDays1to30.add(p1);
            totDays31to60 = totDays31to60.add(p31);
            totDays61to90 = totDays61to90.add(p61);
            totDays90plus = totDays90plus.add(p90);

            Map<String, Object> grp = new LinkedHashMap<>();
            grp.put("partyName",  partyName);
            grp.put("current",    pCur);
            grp.put("days1to30",  p1);
            grp.put("days31to60", p31);
            grp.put("days61to90", p61);
            grp.put("days90plus", p90);
            grp.put("total",      pTotal);
            grp.put("invoices",   invList);
            grouped.add(grp);
        }

        grouped.sort(Comparator.comparing(m -> (String) m.get("partyName")));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("current",    totCurrent);
        summary.put("days1to30",  totDays1to30);
        summary.put("days31to60", totDays31to60);
        summary.put("days61to90", totDays61to90);
        summary.put("days90plus", totDays90plus);
        summary.put("total", totCurrent.add(totDays1to30).add(totDays31to60).add(totDays61to90).add(totDays90plus));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("asOf",    asOf);
        report.put("filter",  partyFilter != null ? partyFilter : "");
        report.put("summary", summary);
        report.put("grouped", grouped);
        report.put("details", details);
        return report;
    }

    /**
     * Trial Balance — sum of debits and credits per account from posted journal entries.
     * group filter: if non-blank, only rows whose accountType or accountName contains the value (case-insensitive)
     */
    public Map<String, Object> getTrialBalance(LocalDate from, LocalDate to, String group, boolean hideZeroBalance) {
        UUID tenantId = tenantContext.current();
        List<Object[]> dbRows = journalEntryRepo.trialBalance(tenantId, from, to);

        // Build account type lookup map
        Map<String, Account> accountMap = accountRepo.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream().collect(Collectors.toMap(Account::getCode, a -> a, (a, b) -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Object[] r : dbRows) {
            String code = (String) r[0];
            String name = (String) r[1];
            BigDecimal debit  = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal credit = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
            BigDecimal balance = debit.subtract(credit);

            // Hide zero-balance rows if requested
            if (hideZeroBalance && balance.compareTo(BigDecimal.ZERO) == 0
                    && debit.compareTo(BigDecimal.ZERO) == 0) continue;

            Account acc = accountMap.get(code);
            String accountType = acc != null && acc.getType() != null ? acc.getType() : "OTHER";

            // Group filter — match against accountType or accountName
            if (group != null && !group.isBlank()) {
                String gLower = group.toLowerCase();
                if (!accountType.toLowerCase().contains(gLower)
                        && !name.toLowerCase().contains(gLower)) continue;
            }

            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountCode", code);
            row.put("accountName", name);
            row.put("accountType", accountType);
            row.put("debit", debit);
            row.put("credit", credit);
            row.put("balance", balance);
            rows.add(row);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("from", from);
        report.put("to", to);
        report.put("totalDebit", totalDebit);
        report.put("totalCredit", totalCredit);
        report.put("balanced", totalDebit.compareTo(totalCredit) == 0);
        report.put("rows", rows);
        return report;
    }

    /**
     * Profit & Loss — computed from posted journal entry sums for the date range.
     * INCOME accounts: net credit = revenue earned.
     * EXPENSE accounts: net debit = expense incurred.
     */
    public Map<String, Object> getProfitAndLoss(LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();

        Map<String, Account> accountMap = accountRepo.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream().collect(Collectors.toMap(Account::getCode, a -> a, (a, b) -> a));

        List<Object[]> dbRows = journalEntryRepo.trialBalance(tenantId, from, to);

        List<Map<String, Object>> income = new ArrayList<>();
        List<Map<String, Object>> expenses = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Object[] r : dbRows) {
            String code = (String) r[0];
            String name = (String) r[1];
            BigDecimal debit  = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal credit = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;

            Account acc = accountMap.get(code);
            if (acc == null || acc.getType() == null) continue;

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("code", code);
            line.put("name", name);
            line.put("subType", acc.getSubType() != null ? acc.getSubType() : "");

            if ("INCOME".equals(acc.getType())) {
                BigDecimal amount = credit.subtract(debit).max(BigDecimal.ZERO);
                line.put("amount", amount);
                totalIncome = totalIncome.add(amount);
                income.add(line);
            } else if ("EXPENSE".equals(acc.getType())) {
                BigDecimal amount = debit.subtract(credit).max(BigDecimal.ZERO);
                line.put("amount", amount);
                totalExpense = totalExpense.add(amount);
                expenses.add(line);
            }
        }

        income.sort(Comparator.comparing(m -> (String) m.get("name")));
        expenses.sort(Comparator.comparing(m -> (String) m.get("name")));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("from", from);
        report.put("to", to);
        report.put("income", income);
        report.put("totalIncome", totalIncome);
        report.put("expenses", expenses);
        report.put("totalExpense", totalExpense);
        report.put("netProfit", totalIncome.subtract(totalExpense));
        return report;
    }

    /**
     * Balance Sheet — cumulative journal entry sums from inception to asOf.
     * ASSET: debit-normal (debit - credit = balance).
     * LIABILITY / EQUITY: credit-normal (credit - debit = balance).
     */
    public Map<String, Object> getBalanceSheet(LocalDate asOf) {
        UUID tenantId = tenantContext.current();
        LocalDate inception = LocalDate.of(2000, 1, 1);

        Map<String, Account> accountMap = accountRepo.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream().collect(Collectors.toMap(Account::getCode, a -> a, (a, b) -> a));

        List<Object[]> dbRows = journalEntryRepo.trialBalance(tenantId, inception, asOf);

        List<Map<String, Object>> assets = new ArrayList<>();
        List<Map<String, Object>> liabilities = new ArrayList<>();
        List<Map<String, Object>> equity = new ArrayList<>();
        BigDecimal totalAssets      = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity      = BigDecimal.ZERO;

        for (Object[] r : dbRows) {
            String code = (String) r[0];
            String name = (String) r[1];
            BigDecimal debit  = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal credit = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;

            Account acc = accountMap.get(code);
            if (acc == null || acc.getType() == null) continue;

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("code", code);
            line.put("name", name);
            line.put("subType", acc.getSubType() != null ? acc.getSubType() : "");

            switch (acc.getType()) {
                case "ASSET": {
                    BigDecimal bal = debit.subtract(credit);
                    line.put("balance", bal);
                    totalAssets = totalAssets.add(bal);
                    assets.add(line);
                    break;
                }
                case "LIABILITY": {
                    BigDecimal bal = credit.subtract(debit);
                    line.put("balance", bal);
                    totalLiabilities = totalLiabilities.add(bal);
                    liabilities.add(line);
                    break;
                }
                case "EQUITY": {
                    BigDecimal bal = credit.subtract(debit);
                    line.put("balance", bal);
                    totalEquity = totalEquity.add(bal);
                    equity.add(line);
                    break;
                }
                default: break;
            }
        }

        assets.sort(Comparator.comparing(m -> (String) m.get("name")));
        liabilities.sort(Comparator.comparing(m -> (String) m.get("name")));
        equity.sort(Comparator.comparing(m -> (String) m.get("name")));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("asOf", asOf);
        report.put("assets", assets);
        report.put("totalAssets", totalAssets);
        report.put("liabilities", liabilities);
        report.put("totalLiabilities", totalLiabilities);
        report.put("equity", equity);
        report.put("totalEquity", totalEquity);
        report.put("totalLiabilitiesAndEquity", totalLiabilities.add(totalEquity));
        report.put("balanced", totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0);
        return report;
    }

    public Map<String, Object> getSalesReport(LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();
        var top10 = PageRequest.of(0, 10);

        BigDecimal totalRevenue   = invoiceRepo.sumTotalByDateRange(tenantId, from, to);
        long       totalInvoices  = invoiceRepo.countByDateRange(tenantId, from, to);
        BigDecimal outstanding    = invoiceRepo.totalOutstanding(tenantId);
        BigDecimal avgInvoiceValue = totalInvoices > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalInvoices), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Map<String, Object>> topCustomers = invoiceRepo
                .topCustomersByRevenue(tenantId, from, to, top10).stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name",   r[0] != null ? r[0] : "Unknown");
                    m.put("amount", r[1] != null ? r[1] : BigDecimal.ZERO);
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> topProducts = invoiceRepo
                .topProductsByRevenue(tenantId, from, to, top10).stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name",     r[0] != null ? r[0] : "Unknown");
                    m.put("quantity", r[1] != null ? r[1] : BigDecimal.ZERO);
                    m.put("amount",   r[2] != null ? r[2] : BigDecimal.ZERO);
                    return m;
                })
                .collect(Collectors.toList());

        // Monthly revenue trend across the requested range
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(to)) {
            LocalDate periodEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());
            if (periodEnd.isAfter(to)) periodEnd = to;
            BigDecimal rev = invoiceRepo.sumTotalByDateRange(tenantId, cursor, periodEnd);
            String label = cursor.getMonth().name().substring(0, 3) + " " + cursor.getYear();
            trend.add(Map.of("label", label, "value", rev));
            cursor = cursor.plusMonths(1);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", Map.of(
                "totalRevenue",    totalRevenue,
                "totalInvoices",   totalInvoices,
                "avgInvoiceValue", avgInvoiceValue,
                "outstanding",     outstanding
        ));
        report.put("topCustomers", topCustomers);
        report.put("topProducts",  topProducts);
        report.put("trend",        trend);
        return report;
    }

    /**
     * Creditors / AP aging — outstanding purchase invoices bucketed by days overdue.
     * Grouped by vendor with per-party subtotals.
     */
    public Map<String, Object> getCreditorsAging(LocalDate asOf, String partyFilter) {
        if (asOf == null) asOf = LocalDate.now();
        UUID tenantId = tenantContext.current();
        final LocalDate effectiveDate = asOf;

        List<PurchaseInvoice> outstanding = purchaseInvoiceRepo.findOutstandingPayables(tenantId);

        if (partyFilter != null && !partyFilter.isBlank()) {
            String fl = partyFilter.toLowerCase();
            outstanding = outstanding.stream()
                    .filter(pi -> pi.getVendorName() != null
                            && pi.getVendorName().toLowerCase().contains(fl))
                    .collect(Collectors.toList());
        }

        BigDecimal totCurrent    = BigDecimal.ZERO;
        BigDecimal totDays1to30  = BigDecimal.ZERO;
        BigDecimal totDays31to60 = BigDecimal.ZERO;
        BigDecimal totDays61to90 = BigDecimal.ZERO;
        BigDecimal totDays90plus = BigDecimal.ZERO;

        Map<String, List<PurchaseInvoice>> byVendor = outstanding.stream()
                .collect(Collectors.groupingBy(
                        pi -> pi.getVendorName() != null ? pi.getVendorName() : "Unknown",
                        LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> grouped = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();

        for (Map.Entry<String, List<PurchaseInvoice>> entry : byVendor.entrySet()) {
            String partyName = entry.getKey();
            BigDecimal pCur = BigDecimal.ZERO, p1 = BigDecimal.ZERO,
                       p31  = BigDecimal.ZERO, p61 = BigDecimal.ZERO, p90 = BigDecimal.ZERO;
            List<Map<String, Object>> invList = new ArrayList<>();

            for (PurchaseInvoice pi : entry.getValue()) {
                BigDecimal balance = pi.getBalanceDue() != null ? pi.getBalanceDue() : BigDecimal.ZERO;
                LocalDate dueDate = pi.getDueDate();
                long overdueDays = dueDate != null ? ChronoUnit.DAYS.between(dueDate, effectiveDate) : 0;

                if      (overdueDays <= 0)  pCur = pCur.add(balance);
                else if (overdueDays <= 30) p1   = p1.add(balance);
                else if (overdueDays <= 60) p31  = p31.add(balance);
                else if (overdueDays <= 90) p61  = p61.add(balance);
                else                        p90  = p90.add(balance);

                String bucket = overdueDays <= 0 ? "Current"
                        : overdueDays <= 30 ? "1-30 days"
                        : overdueDays <= 60 ? "31-60 days"
                        : overdueDays <= 90 ? "61-90 days" : "90+ days";

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("piNumber",    pi.getPiNumber());
                row.put("vendorName",  pi.getVendorName());
                row.put("invoiceDate", pi.getInvoiceDate());
                row.put("dueDate",     dueDate);
                row.put("netPayable",  pi.getNetPayable());
                row.put("balanceDue",  balance);
                row.put("overdueDays", overdueDays > 0 ? overdueDays : 0);
                row.put("bucket",      bucket);
                invList.add(row);
                details.add(row);
            }

            BigDecimal pTotal = pCur.add(p1).add(p31).add(p61).add(p90);
            totCurrent    = totCurrent.add(pCur);
            totDays1to30  = totDays1to30.add(p1);
            totDays31to60 = totDays31to60.add(p31);
            totDays61to90 = totDays61to90.add(p61);
            totDays90plus = totDays90plus.add(p90);

            Map<String, Object> grp = new LinkedHashMap<>();
            grp.put("partyName",  partyName);
            grp.put("current",    pCur);
            grp.put("days1to30",  p1);
            grp.put("days31to60", p31);
            grp.put("days61to90", p61);
            grp.put("days90plus", p90);
            grp.put("total",      pTotal);
            grp.put("invoices",   invList);
            grouped.add(grp);
        }

        grouped.sort(Comparator.comparing(m -> (String) m.get("partyName")));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("current",    totCurrent);
        summary.put("days1to30",  totDays1to30);
        summary.put("days31to60", totDays31to60);
        summary.put("days61to90", totDays61to90);
        summary.put("days90plus", totDays90plus);
        summary.put("total", totCurrent.add(totDays1to30).add(totDays31to60).add(totDays61to90).add(totDays90plus));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("asOf",    asOf);
        report.put("filter",  partyFilter != null ? partyFilter : "");
        report.put("summary", summary);
        report.put("grouped", grouped);
        report.put("details", details);
        return report;
    }

    /**
     * Stock valuation report — current quantity × average cost per product/warehouse.
     */
    public Map<String, Object> getStockValuation() {
        UUID tenantId = tenantContext.current();
        List<StockItem> items = stockItemRepo.findByTenantIdAndDeletedAtIsNull(tenantId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        BigDecimal totalValue = BigDecimal.ZERO;
        List<Map<String, Object>> lines = new ArrayList<>();

        for (StockItem s : items) {
            BigDecimal qty  = s.getQuantityOnHand() != null ? s.getQuantityOnHand() : BigDecimal.ZERO;
            BigDecimal cost = s.getAverageCost()     != null ? s.getAverageCost()    : BigDecimal.ZERO;
            BigDecimal value = qty.multiply(cost).setScale(2, java.math.RoundingMode.HALF_UP);
            totalValue = totalValue.add(value);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("productName",   s.getProductName());
            row.put("warehouseName", s.getWarehouseName());
            row.put("quantityOnHand",s.getQuantityOnHand());
            row.put("averageCost",   cost);
            row.put("totalValue",    value);
            lines.add(row);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("asOf",       LocalDate.now());
        report.put("totalValue", totalValue);
        report.put("lines",      lines);
        return report;
    }

    /**
     * Cash Flow Statement — indirect method.
     * Operating: Net Profit + depreciation adjustments + working capital changes (AR, AP, Inventory).
     * Investing: Changes in fixed asset accounts.
     * Financing: Changes in loan/equity accounts.
     * Opening/closing cash: cumulative bank+cash ASSET account balances.
     */
    public Map<String, Object> getCashFlow(LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();
        LocalDate inception = LocalDate.of(2000, 1, 1);

        Map<String, Account> accountMap = accountRepo.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream().collect(Collectors.toMap(Account::getCode, a -> a, (a, b) -> a));

        List<Object[]> periodRows = journalEntryRepo.trialBalance(tenantId, from, to);

        BigDecimal netProfit        = BigDecimal.ZERO;
        BigDecimal depreciation     = BigDecimal.ZERO;
        BigDecimal arChange         = BigDecimal.ZERO;
        BigDecimal apChange         = BigDecimal.ZERO;
        BigDecimal inventoryChange  = BigDecimal.ZERO;
        BigDecimal fixedAssetChange = BigDecimal.ZERO;
        BigDecimal loanChange       = BigDecimal.ZERO;
        BigDecimal equityChange     = BigDecimal.ZERO;

        for (Object[] r : periodRows) {
            String code = (String) r[0];
            BigDecimal dr = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal cr = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;

            Account acc = accountMap.get(code);
            if (acc == null || acc.getType() == null) continue;
            String type = acc.getType();
            String sub  = acc.getSubType() != null ? acc.getSubType().toUpperCase() : "";
            String nm   = acc.getName()    != null ? acc.getName().toLowerCase()    : "";

            switch (type) {
                case "INCOME":
                    netProfit = netProfit.add(cr.subtract(dr));
                    break;
                case "EXPENSE":
                    netProfit = netProfit.subtract(dr.subtract(cr));
                    if (sub.contains("DEPRECIATION") || nm.contains("depreciation") || nm.contains("amortis")) {
                        depreciation = depreciation.add(dr.subtract(cr).max(BigDecimal.ZERO));
                    }
                    break;
                case "ASSET":
                    if (sub.equals("ACCOUNTS_RECEIVABLE") || nm.contains("receivable") || nm.contains("debtor")) {
                        arChange = arChange.subtract(dr.subtract(cr));
                    } else if (sub.equals("INVENTORY") || nm.contains("inventor") || nm.contains("stock")) {
                        inventoryChange = inventoryChange.subtract(dr.subtract(cr));
                    } else if (sub.equals("FIXED_ASSET") || sub.equals("PROPERTY_PLANT_EQUIPMENT")
                               || nm.contains("machinery") || nm.contains("equipment")
                               || nm.contains("vehicle") || nm.contains("furniture") || nm.contains("building")) {
                        fixedAssetChange = fixedAssetChange.subtract(dr.subtract(cr));
                    }
                    break;
                case "LIABILITY":
                    if (sub.equals("ACCOUNTS_PAYABLE") || nm.contains("payable") || nm.contains("creditor")) {
                        apChange = apChange.add(cr.subtract(dr));
                    } else if (sub.contains("LOAN") || sub.contains("DEBT") || sub.contains("BORROW")
                               || nm.contains("loan") || nm.contains("borrowing") || nm.contains("term loan")) {
                        loanChange = loanChange.add(cr.subtract(dr));
                    }
                    break;
                case "EQUITY":
                    equityChange = equityChange.add(cr.subtract(dr));
                    break;
                default: break;
            }
        }

        // Opening and closing cash (cumulative bank+cash ASSET balances)
        BigDecimal openingCash = BigDecimal.ZERO;
        BigDecimal closingCash = BigDecimal.ZERO;

        if (!from.minusDays(1).isBefore(inception)) {
            for (Object[] r : journalEntryRepo.trialBalance(tenantId, inception, from.minusDays(1))) {
                Account acc = accountMap.get(r[0]);
                if (acc != null && isCashAccount(acc)) {
                    BigDecimal dr = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                    BigDecimal cr = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
                    openingCash = openingCash.add(dr.subtract(cr));
                }
            }
        }
        for (Object[] r : journalEntryRepo.trialBalance(tenantId, inception, to)) {
            Account acc = accountMap.get(r[0]);
            if (acc != null && isCashAccount(acc)) {
                BigDecimal dr = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
                BigDecimal cr = r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO;
                closingCash = closingCash.add(dr.subtract(cr));
            }
        }

        // Build sections
        List<Map<String, Object>> adjustments = new ArrayList<>();
        if (depreciation.compareTo(BigDecimal.ZERO) != 0)
            adjustments.add(Map.of("label", "Add: Depreciation & Amortisation", "amount", depreciation));

        List<Map<String, Object>> wcChanges = new ArrayList<>();
        if (arChange.compareTo(BigDecimal.ZERO) != 0)
            wcChanges.add(Map.of("label", arChange.compareTo(BigDecimal.ZERO) < 0
                    ? "Increase in Accounts Receivable" : "Decrease in Accounts Receivable", "amount", arChange));
        if (apChange.compareTo(BigDecimal.ZERO) != 0)
            wcChanges.add(Map.of("label", apChange.compareTo(BigDecimal.ZERO) > 0
                    ? "Increase in Accounts Payable" : "Decrease in Accounts Payable", "amount", apChange));
        if (inventoryChange.compareTo(BigDecimal.ZERO) != 0)
            wcChanges.add(Map.of("label", inventoryChange.compareTo(BigDecimal.ZERO) < 0
                    ? "Increase in Inventory" : "Decrease in Inventory", "amount", inventoryChange));

        BigDecimal opTotal = netProfit.add(depreciation).add(arChange).add(apChange).add(inventoryChange);

        List<Map<String, Object>> investingItems = new ArrayList<>();
        if (fixedAssetChange.compareTo(BigDecimal.ZERO) != 0)
            investingItems.add(Map.of("label",
                    fixedAssetChange.compareTo(BigDecimal.ZERO) < 0 ? "Purchase of Fixed Assets" : "Proceeds from Asset Disposal",
                    "amount", fixedAssetChange));

        List<Map<String, Object>> financingItems = new ArrayList<>();
        if (loanChange.compareTo(BigDecimal.ZERO) != 0)
            financingItems.add(Map.of("label",
                    loanChange.compareTo(BigDecimal.ZERO) > 0 ? "Loan Proceeds" : "Loan Repayments",
                    "amount", loanChange));
        if (equityChange.compareTo(BigDecimal.ZERO) != 0)
            financingItems.add(Map.of("label",
                    equityChange.compareTo(BigDecimal.ZERO) > 0 ? "Equity Contributions" : "Equity Withdrawals / Dividends",
                    "amount", equityChange));

        BigDecimal finTotal = loanChange.add(equityChange);

        Map<String, Object> operating = new LinkedHashMap<>();
        operating.put("netProfit", netProfit);
        operating.put("adjustments", adjustments);
        operating.put("workingCapitalChanges", wcChanges);
        operating.put("total", opTotal);

        Map<String, Object> investing = new LinkedHashMap<>();
        investing.put("items", investingItems);
        investing.put("total", fixedAssetChange);

        Map<String, Object> financing = new LinkedHashMap<>();
        financing.put("items", financingItems);
        financing.put("total", finTotal);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("from", from);
        report.put("to", to);
        report.put("operating", operating);
        report.put("investing", investing);
        report.put("financing", financing);
        report.put("netChange", opTotal.add(fixedAssetChange).add(finTotal));
        report.put("openingCash", openingCash);
        report.put("closingCash", closingCash);
        return report;
    }

    private boolean isCashAccount(Account acc) {
        String sub = acc.getSubType() != null ? acc.getSubType().toUpperCase() : "";
        String nm  = acc.getName()    != null ? acc.getName().toLowerCase()    : "";
        return "ASSET".equals(acc.getType())
                && (sub.equals("BANK") || sub.equals("CASH") || sub.equals("PETTY_CASH")
                    || nm.contains("bank") || nm.contains("cash") || nm.contains("petty cash"));
    }

    public Map<String, Object> getInventoryReport(UUID warehouseId) {
        UUID tenantId = tenantContext.current();

        List<StockItem> items = warehouseId != null
                ? stockItemRepo.findByTenantIdAndWarehouseIdAndDeletedAtIsNull(tenantId, warehouseId, PageRequest.of(0, Integer.MAX_VALUE)).getContent()
                : stockItemRepo.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        BigDecimal lowThreshold = BigDecimal.TEN;
        BigDecimal totalValue = BigDecimal.ZERO;
        int outOfStockCount = 0;
        int lowStockCount = 0;
        List<Map<String, Object>> valuation = new ArrayList<>();
        List<Map<String, Object>> lowStock = new ArrayList<>();

        for (StockItem s : items) {
            BigDecimal qty = s.getQuantityOnHand() != null ? s.getQuantityOnHand() : BigDecimal.ZERO;
            BigDecimal cost = s.getAverageCost() != null ? s.getAverageCost() : BigDecimal.ZERO;
            BigDecimal value = qty.multiply(cost).setScale(2, RoundingMode.HALF_UP);
            totalValue = totalValue.add(value);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", s.getProductName());
            row.put("category", s.getProductName());
            row.put("quantity", qty);
            row.put("value", value);
            valuation.add(row);

            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                outOfStockCount++;
            } else if (qty.compareTo(lowThreshold) <= 0) {
                lowStockCount++;
                Map<String, Object> ls = new LinkedHashMap<>();
                ls.put("name", s.getProductName());
                ls.put("current", qty);
                ls.put("reorderLevel", lowThreshold);
                lowStock.add(ls);
            }
        }

        valuation.sort((a, b) -> ((BigDecimal) b.get("value")).compareTo((BigDecimal) a.get("value")));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalValue", totalValue);
        summary.put("totalSkus", items.size());
        summary.put("lowStockCount", lowStockCount);
        summary.put("outOfStockCount", outOfStockCount);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("valuation", valuation.size() > 20 ? valuation.subList(0, 20) : valuation);
        report.put("lowStock", lowStock);
        report.put("movements", Collections.emptyList());
        report.put("summary", summary);
        return report;
    }

    public Map<String, Object> getPurchasesReport(LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();

        List<PurchaseOrder> allPOs = poRepo
                .findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .stream()
                .filter(p -> p.getOrderDate() != null
                        && !p.getOrderDate().isBefore(from)
                        && !p.getOrderDate().isAfter(to))
                .collect(Collectors.toList());

        BigDecimal totalSpend = allPOs.stream()
                .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPOs = allPOs.size();
        BigDecimal avgPOValue = totalPOs > 0
                ? totalSpend.divide(BigDecimal.valueOf(totalPOs), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal outstanding = purchaseInvoiceRepo.totalPayable(tenantId);
        if (outstanding == null) outstanding = BigDecimal.ZERO;

        Map<String, BigDecimal> vendorSpendMap = new LinkedHashMap<>();
        for (PurchaseOrder po : allPOs) {
            String name = po.getVendorName() != null ? po.getVendorName() : "Unknown";
            BigDecimal amount = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
            vendorSpendMap.merge(name, amount, BigDecimal::add);
        }

        List<Map<String, Object>> topVendors = vendorSpendMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(to)) {
            final LocalDate pStart = cursor;
            LocalDate monthEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());
            final LocalDate pEnd = monthEnd.isAfter(to) ? to : monthEnd;
            BigDecimal monthSpend = allPOs.stream()
                    .filter(p -> p.getOrderDate() != null
                            && !p.getOrderDate().isBefore(pStart)
                            && !p.getOrderDate().isAfter(pEnd))
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String label = cursor.getMonth().name().substring(0, 3) + " " + cursor.getYear();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("label", label);
            point.put("value", monthSpend);
            trend.add(point);
            cursor = cursor.plusMonths(1);
        }

        final BigDecimal finalOutstanding = outstanding;
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", Map.of(
                "totalSpend", totalSpend,
                "totalPOs", totalPOs,
                "avgPOValue", avgPOValue,
                "outstanding", finalOutstanding
        ));
        report.put("topVendors", topVendors);
        report.put("trend", trend);
        return report;
    }

    /**
     * AR/AP reconciliation — compares invoice subledger outstanding vs GL account balance.
     * Variance highlights posting gaps or unrecorded payments.
     */
    public Map<String, Object> getArApReconciliation() {
        UUID tenantId = tenantContext.current();

        // AR: subledger (sum of invoice.balanceDue) vs GL (AR account balance)
        BigDecimal arSubledger = invoiceRepo.totalOutstanding(tenantId);
        if (arSubledger == null) arSubledger = BigDecimal.ZERO;
        List<Account> arAccounts = accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_RECEIVABLE");
        BigDecimal arGlBalance = arAccounts.stream()
                .map(a -> a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal arVariance = arSubledger.subtract(arGlBalance);

        // AP: subledger (sum of purchaseInvoice.balanceDue) vs GL (AP account balance)
        BigDecimal apSubledger = purchaseInvoiceRepo.totalPayable(tenantId);
        if (apSubledger == null) apSubledger = BigDecimal.ZERO;
        List<Account> apAccounts = accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_PAYABLE");
        BigDecimal apGlBalance = apAccounts.stream()
                .map(a -> a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal apVariance = apSubledger.subtract(apGlBalance);

        List<Map<String, Object>> arAccountLines = arAccounts.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", a.getCode());
            m.put("name", a.getName());
            m.put("glBalance", a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO);
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> apAccountLines = apAccounts.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", a.getCode());
            m.put("name", a.getName());
            m.put("glBalance", a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> ar = new LinkedHashMap<>();
        ar.put("subledgerOutstanding", arSubledger);
        ar.put("glBalance", arGlBalance);
        ar.put("variance", arVariance);
        ar.put("reconciled", arVariance.compareTo(BigDecimal.ZERO) == 0);
        ar.put("glAccounts", arAccountLines);

        Map<String, Object> ap = new LinkedHashMap<>();
        ap.put("subledgerOutstanding", apSubledger);
        ap.put("glBalance", apGlBalance);
        ap.put("variance", apVariance);
        ap.put("reconciled", apVariance.compareTo(BigDecimal.ZERO) == 0);
        ap.put("glAccounts", apAccountLines);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("asOf", LocalDate.now());
        report.put("accountsReceivable", ar);
        report.put("accountsPayable", ap);
        return report;
    }

    private BigDecimal sumBalances(List<Account> accounts) {
        return accounts.stream()
                .map(a -> a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> toAccountLines(List<Account> accounts) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (Account a : accounts) {
            lines.add(Map.of("code", a.getCode(), "name", a.getName(),
                    "balance", a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO));
        }
        return lines;
    }
}
