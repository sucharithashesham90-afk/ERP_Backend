package com.erp.platform.modules.ai.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Executes MCP tool calls by querying ERP data via JPQL.
 * Each method returns a plain-text summary suitable for Claude to interpret.
 *
 * @Lazy defers bean creation until first HTTP request so the JPA
 * entityManagerFactory is fully initialized before @PersistenceContext runs.
 */
@Service
@Lazy
@Slf4j
@Transactional(readOnly = true)
public class McpToolExecutor {

    @PersistenceContext
    private EntityManager em;

    /**
     * Which module a tool belongs to — mirrors the {@code if (allowedModules.contains(...))}
     * gates in {@link AiAssistantService#buildTools}. That method only decides what the model is
     * *offered*; this is the independent, second check at the layer that actually touches data, so
     * a hallucinated tool name (or any call that bypasses the offered list) can't silently return
     * real data instead of an authorization error. A tool with no entry here (e.g. the dashboard
     * summary) is available to everyone.
     */
    private static final Map<String, Set<String>> TOOL_MODULES = Map.ofEntries(
            Map.entry("get_unpaid_invoices",      Set.of("SALES", "INVOICES")),
            Map.entry("get_sales_orders",         Set.of("SALES", "INVOICES")),
            Map.entry("get_customer_list",        Set.of("SALES", "INVOICES")),
            Map.entry("get_sales_summary",        Set.of("SALES", "INVOICES")),
            Map.entry("get_customer_advances",    Set.of("SALES", "INVOICES")),
            Map.entry("get_low_stock_items",      Set.of("INVENTORY")),
            Map.entry("get_stock_summary",        Set.of("INVENTORY")),
            Map.entry("get_expiring_lots",        Set.of("INVENTORY")),
            Map.entry("get_quarantined_lots",     Set.of("INVENTORY")),
            Map.entry("get_pending_purchase_orders", Set.of("PURCHASE")),
            Map.entry("get_pending_grn",          Set.of("PURCHASE")),
            Map.entry("get_supplier_list",        Set.of("PURCHASE")),
            Map.entry("get_outstanding_receivables", Set.of("ACCOUNTING")),
            Map.entry("get_outstanding_payables", Set.of("ACCOUNTING")),
            Map.entry("get_draft_journal_entries", Set.of("ACCOUNTING")),
            Map.entry("get_pending_leave_requests", Set.of("HR")),
            Map.entry("get_employee_summary",     Set.of("HR")),
            Map.entry("get_attendance_summary",   Set.of("HR")),
            Map.entry("get_payroll_summary",      Set.of("PAYROLL")),
            Map.entry("get_pending_payslips",     Set.of("PAYROLL")),
            Map.entry("get_active_work_orders",   Set.of("MANUFACTURING")),
            Map.entry("get_production_summary",   Set.of("MANUFACTURING")),
            Map.entry("get_leads_pipeline",       Set.of("CRM", "LEADS")),
            Map.entry("get_won_leads",            Set.of("CRM", "LEADS"))
    );

    public String execute(String toolName, Map<String, Object> input, UUID tenantId, List<String> allowedModules) {
        Set<String> requiredModules = TOOL_MODULES.get(toolName);
        if (requiredModules != null && requiredModules.stream().noneMatch(allowedModules::contains)) {
            log.warn("Blocked tool '{}' for tenant {} — user's modules {} don't include any of {}",
                    toolName, tenantId, allowedModules, requiredModules);
            return "You are not authorized to view this information.";
        }
        try {
            return switch (toolName) {
                // ── SALES ──────────────────────────────────────────────────────
                case "get_unpaid_invoices"      -> getUnpaidInvoices(input, tenantId);
                case "get_sales_orders"         -> getSalesOrders(input, tenantId);
                case "get_customer_list"        -> getCustomerList(input, tenantId);
                case "get_sales_summary"        -> getSalesSummary(tenantId);
                case "get_customer_advances"    -> getCustomerAdvances(tenantId);
                // ── INVENTORY ──────────────────────────────────────────────────
                case "get_low_stock_items"      -> getLowStockItems(input, tenantId);
                case "get_stock_summary"        -> getStockSummary(tenantId);
                case "get_expiring_lots"        -> getExpiringLots(input, tenantId);
                case "get_quarantined_lots"     -> getQuarantinedLots(tenantId);
                // ── PURCHASE ───────────────────────────────────────────────────
                case "get_pending_purchase_orders" -> getPendingPurchaseOrders(tenantId);
                case "get_pending_grn"          -> getPendingGrn(tenantId);
                case "get_supplier_list"        -> getSupplierList(input, tenantId);
                // ── ACCOUNTING ─────────────────────────────────────────────────
                case "get_outstanding_receivables" -> getOutstandingReceivables(tenantId);
                case "get_outstanding_payables"    -> getOutstandingPayables(tenantId);
                case "get_draft_journal_entries"   -> getDraftJournalEntries(tenantId);
                // ── HR ─────────────────────────────────────────────────────────
                case "get_pending_leave_requests"  -> getPendingLeaveRequests(tenantId);
                case "get_employee_summary"        -> getEmployeeSummary(tenantId);
                case "get_attendance_summary"      -> getAttendanceSummary(input, tenantId);
                // ── PAYROLL ────────────────────────────────────────────────────
                case "get_payroll_summary"      -> getPayrollSummary(input, tenantId);
                case "get_pending_payslips"     -> getPendingPayslips(tenantId);
                // ── MANUFACTURING ──────────────────────────────────────────────
                case "get_active_work_orders"   -> getActiveWorkOrders(tenantId);
                case "get_production_summary"   -> getProductionSummary(tenantId);
                // ── CRM ────────────────────────────────────────────────────────
                case "get_leads_pipeline"       -> getLeadsPipeline(tenantId);
                case "get_won_leads"            -> getWonLeads(tenantId);
                // ── GENERAL ────────────────────────────────────────────────────
                case "get_dashboard_summary"    -> getDashboardSummary(tenantId);
                default -> "Tool '" + toolName + "' is not implemented.";
            };
        } catch (Exception e) {
            log.warn("Tool execution failed [{}]: {}", toolName, e.getMessage());
            return "Unable to retrieve data for '" + toolName + "': " + e.getMessage();
        }
    }

    // ── SALES ─────────────────────────────────────────────────────────────────

    private String getUnpaidInvoices(Map<String, Object> input, UUID tenantId) {
        BigDecimal minAmount = getBigDecimal(input, "min_amount", BigDecimal.ZERO);
        String customerName = getString(input, "customer_name", null);

        String jpql = "SELECT i.invoiceNumber, i.customerName, i.totalAmount, i.balanceDue, i.dueDate, i.status " +
                      "FROM Invoice i WHERE i.tenantId = :tid AND i.deletedAt IS NULL " +
                      "AND i.status IN ('SENT','PARTIALLY_PAID') " +
                      "AND i.totalAmount >= :minAmt " +
                      (customerName != null ? "AND LOWER(i.customerName) LIKE :cust " : "") +
                      "ORDER BY i.balanceDue DESC";

        var q = em.createQuery(jpql)
                .setParameter("tid", tenantId)
                .setParameter("minAmt", minAmount)
                .setMaxResults(20);
        if (customerName != null) q.setParameter("cust", "%" + customerName.toLowerCase() + "%");

        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return "No unpaid invoices found matching the criteria.";

        var sb = new StringBuilder("Unpaid Invoices (top " + rows.size() + "):\n");
        sb.append(String.format("%-15s %-25s %12s %12s %-12s %-15s%n",
                "Invoice#", "Customer", "Total(₹)", "Due(₹)", "Due Date", "Status"));
        sb.append("-".repeat(90)).append("\n");
        for (Object[] r : rows) {
            sb.append(String.format("%-15s %-25s %12s %12s %-12s %-15s%n",
                    r[0], truncate((String) r[1], 24),
                    fmt(r[2]), fmt(r[3]), r[4], r[5]));
        }
        return sb.toString();
    }

    private String getSalesOrders(Map<String, Object> input, UUID tenantId) {
        String status = getString(input, "status", null);
        String jpql = "SELECT o.orderNumber, o.customerName, o.totalAmount, o.status, o.orderDate " +
                      "FROM SalesOrder o WHERE o.tenantId = :tid AND o.deletedAt IS NULL " +
                      (status != null ? "AND o.status = :status " : "") +
                      "ORDER BY o.orderDate DESC";
        var q = em.createQuery(jpql).setParameter("tid", tenantId).setMaxResults(20);
        if (status != null) q.setParameter("status", status);
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return "No sales orders found.";
        var sb = new StringBuilder("Sales Orders:\n");
        for (Object[] r : rows)
            sb.append(String.format("  %s | %-20s | ₹%s | %s | %s%n",
                    r[0], truncate((String) r[1], 20), fmt(r[2]), r[3], r[4]));
        return sb.toString();
    }

    private String getCustomerList(Map<String, Object> input, UUID tenantId) {
        String query = getString(input, "query", null);
        String jpql = "SELECT c.name, c.email, c.phone, c.creditLimit FROM Customer c " +
                      "WHERE c.tenantId = :tid AND c.deletedAt IS NULL " +
                      (query != null ? "AND (LOWER(c.name) LIKE :q OR LOWER(c.email) LIKE :q) " : "") +
                      "ORDER BY c.name";
        var q = em.createQuery(jpql).setParameter("tid", tenantId).setMaxResults(15);
        if (query != null) q.setParameter("q", "%" + query.toLowerCase() + "%");
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return "No customers found.";
        var sb = new StringBuilder("Customers (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-25s | %-25s | %-15s | Credit: ₹%s%n",
                    truncate((String) r[0], 24), r[1], r[2], fmt(r[3])));
        return sb.toString();
    }

    private String getSalesSummary(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(i), SUM(i.totalAmount), SUM(i.balanceDue) FROM Invoice i " +
                "WHERE i.tenantId = :tid AND i.deletedAt IS NULL AND i.status != 'CANCELLED'")
                .setParameter("tid", tenantId).getSingleResult();
        Object[] o = (Object[]) em.createQuery(
                "SELECT COUNT(o), SUM(o.totalAmount) FROM SalesOrder o " +
                "WHERE o.tenantId = :tid AND o.deletedAt IS NULL")
                .setParameter("tid", tenantId).getSingleResult();
        return String.format(
                "Sales Summary:\n  Total Invoices: %s | Total Billed: ₹%s | Outstanding: ₹%s%n" +
                "  Total Sales Orders: %s | Order Value: ₹%s",
                r[0], fmt(r[1]), fmt(r[2]), o[0], fmt(o[1]));
    }

    private String getCustomerAdvances(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT ca.customerName, ca.amount, ca.amountAvailable, ca.status, ca.paymentDate " +
                "FROM CustomerAdvance ca WHERE ca.tenantId = :tid AND ca.deletedAt IS NULL " +
                "AND ca.status IN ('AVAILABLE','PARTIALLY_APPLIED') ORDER BY ca.createdAt DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No active customer advances found.";
        var sb = new StringBuilder("Active Customer Advances:\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-25s | Total: ₹%s | Available: ₹%s | %s | %s%n",
                    truncate((String) r[0], 24), fmt(r[1]), fmt(r[2]), r[3], r[4]));
        return sb.toString();
    }

    // ── INVENTORY ─────────────────────────────────────────────────────────────

    private String getLowStockItems(Map<String, Object> input, UUID tenantId) {
        BigDecimal threshold = getBigDecimal(input, "threshold", new BigDecimal("10"));
        List<Object[]> rows = em.createQuery(
                "SELECT s.productName, s.warehouseName, s.quantityOnHand, s.quantityReserved " +
                "FROM StockItem s WHERE s.tenantId = :tid AND s.deletedAt IS NULL " +
                "AND s.quantityOnHand <= :thresh ORDER BY s.quantityOnHand ASC",
                Object[].class)
                .setParameter("tid", tenantId).setParameter("thresh", threshold).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No low-stock items found at threshold " + threshold + ".";
        var sb = new StringBuilder("Low Stock Items (qty ≤ " + threshold + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-30s | Warehouse: %-20s | On Hand: %s | Reserved: %s%n",
                    truncate((String) r[0], 29), truncate((String) r[1], 19), fmt(r[2]), fmt(r[3])));
        return sb.toString();
    }

    private String getStockSummary(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(s), SUM(s.quantityOnHand) FROM StockItem s " +
                "WHERE s.tenantId = :tid AND s.deletedAt IS NULL")
                .setParameter("tid", tenantId).getSingleResult();
        long zero = ((Number) em.createQuery(
                "SELECT COUNT(s) FROM StockItem s WHERE s.tenantId = :tid " +
                "AND s.deletedAt IS NULL AND s.quantityOnHand = 0")
                .setParameter("tid", tenantId).getSingleResult()).longValue();
        return String.format("Inventory Summary:\n  Total SKUs tracked: %s\n  Total quantity on hand: %s\n  Out-of-stock SKUs: %d",
                r[0], fmt(r[1]), zero);
    }

    private String getExpiringLots(Map<String, Object> input, UUID tenantId) {
        int days = getInt(input, "days", 30);
        LocalDate cutoff = LocalDate.now().plusDays(days);
        List<Object[]> rows = em.createQuery(
                "SELECT l.lotNumber, l.productName, l.quantityOnHand, l.expiryDate, l.status " +
                "FROM LotStock l WHERE l.tenantId = :tid AND l.deletedAt IS NULL " +
                "AND l.expiryDate IS NOT NULL AND l.expiryDate <= :cutoff " +
                "AND l.status = 'AVAILABLE' ORDER BY l.expiryDate ASC",
                Object[].class)
                .setParameter("tid", tenantId).setParameter("cutoff", cutoff).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No lots expiring within " + days + " days.";
        var sb = new StringBuilder("Lots expiring within " + days + " days:\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-15s | %-25s | Qty: %s | Expiry: %s | %s%n",
                    r[0], truncate((String) r[1], 24), fmt(r[2]), r[3], r[4]));
        return sb.toString();
    }

    private String getQuarantinedLots(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT l.lotNumber, l.productName, l.quantityOnHand, l.warehouseName " +
                "FROM LotStock l WHERE l.tenantId = :tid AND l.deletedAt IS NULL " +
                "AND l.status = 'QUARANTINE' ORDER BY l.createdAt DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No quarantined lots.";
        var sb = new StringBuilder("Quarantined Lots (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-15s | %-25s | Qty: %s | %s%n",
                    r[0], truncate((String) r[1], 24), fmt(r[2]), r[3]));
        return sb.toString();
    }

    // ── PURCHASE ──────────────────────────────────────────────────────────────

    private String getPendingPurchaseOrders(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT p.poNumber, p.vendorName, p.totalAmount, p.status, p.orderDate " +
                "FROM PurchaseOrder p WHERE p.tenantId = :tid AND p.deletedAt IS NULL " +
                "AND p.status IN ('DRAFT','CONFIRMED','APPROVED') ORDER BY p.orderDate DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No pending purchase orders.";
        var sb = new StringBuilder("Pending Purchase Orders (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-15s | %-25s | ₹%s | %s | %s%n",
                    r[0], truncate((String) r[1], 24), fmt(r[2]), r[3], r[4]));
        return sb.toString();
    }

    private String getPendingGrn(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(g), SUM(g.totalValue) FROM GoodsReceipt g " +
                "WHERE g.tenantId = :tid AND g.deletedAt IS NULL AND g.status = 'PENDING'")
                .setParameter("tid", tenantId).getSingleResult();
        return String.format("Pending GRN: %s receipts awaiting processing, total value ₹%s", r[0], fmt(r[1]));
    }

    private String getSupplierList(Map<String, Object> input, UUID tenantId) {
        String query = getString(input, "query", null);
        String jpql = "SELECT v.name, v.email, v.phone, v.active FROM Vendor v " +
                      "WHERE v.tenantId = :tid AND v.deletedAt IS NULL " +
                      (query != null ? "AND LOWER(v.name) LIKE :q " : "") +
                      "ORDER BY v.name";
        var q = em.createQuery(jpql).setParameter("tid", tenantId).setMaxResults(15);
        if (query != null) q.setParameter("q", "%" + query.toLowerCase() + "%");
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return "No suppliers found.";
        var sb = new StringBuilder("Suppliers (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-25s | %-25s | %-15s | Active: %s%n",
                    truncate((String) r[0], 24), r[1], r[2], r[3]));
        return sb.toString();
    }

    // ── ACCOUNTING ────────────────────────────────────────────────────────────

    private String getOutstandingReceivables(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(i), SUM(i.balanceDue) FROM Invoice i " +
                "WHERE i.tenantId = :tid AND i.deletedAt IS NULL AND i.balanceDue > 0 " +
                "AND i.status IN ('SENT','PARTIALLY_PAID')")
                .setParameter("tid", tenantId).getSingleResult();
        List<Object[]> top = em.createQuery(
                "SELECT i.customerName, SUM(i.balanceDue) FROM Invoice i " +
                "WHERE i.tenantId = :tid AND i.deletedAt IS NULL AND i.balanceDue > 0 " +
                "AND i.status IN ('SENT','PARTIALLY_PAID') GROUP BY i.customerName ORDER BY 2 DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(5).getResultList();
        var sb = new StringBuilder(String.format("Accounts Receivable:\n  Total outstanding: ₹%s across %s invoices%n", fmt(r[1]), r[0]));
        if (!top.isEmpty()) {
            sb.append("  Top customers by outstanding balance:\n");
            top.forEach(t -> sb.append(String.format("    %-25s ₹%s%n", truncate((String) t[0], 24), fmt(t[1]))));
        }
        return sb.toString();
    }

    private String getOutstandingPayables(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(p), SUM(p.totalAmount) FROM PurchaseOrder p " +
                "WHERE p.tenantId = :tid AND p.deletedAt IS NULL " +
                "AND p.status IN ('CONFIRMED','APPROVED','RECEIVED')")
                .setParameter("tid", tenantId).getSingleResult();
        return String.format("Accounts Payable:\n  Open POs pending payment: %s | Total value: ₹%s", r[0], fmt(r[1]));
    }

    private String getDraftJournalEntries(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT j.entryNumber, j.entryDate, j.description, j.referenceType " +
                "FROM JournalEntry j WHERE j.tenantId = :tid AND j.deletedAt IS NULL " +
                "AND j.status = 'DRAFT' ORDER BY j.createdAt DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(10).getResultList();
        if (rows.isEmpty()) return "No draft journal entries pending review.";
        var sb = new StringBuilder("Draft Journal Entries (" + rows.size() + " pending review):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-15s | %s | %-30s | %s%n", r[0], r[1], truncate((String) r[2], 29), r[3]));
        return sb.toString();
    }

    // ── HR ────────────────────────────────────────────────────────────────────

    private String getPendingLeaveRequests(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT la.employeeId, la.leaveType, la.fromDate, la.toDate, la.days, la.reason " +
                "FROM LeaveApplication la WHERE la.tenantId = :tid AND la.deletedAt IS NULL " +
                "AND la.status = 'PENDING' ORDER BY la.fromDate ASC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No pending leave requests.";
        var sb = new StringBuilder("Pending Leave Requests (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  Employee: %s | Type: %-10s | From: %s To: %s (%s days) | %s%n",
                    r[0], r[1], r[2], r[3], r[4], truncate((String) r[5], 30)));
        return sb.toString();
    }

    private String getEmployeeSummary(UUID tenantId) {
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tid AND e.deletedAt IS NULL AND e.status = 'ACTIVE'")
                .setParameter("tid", tenantId).getSingleResult();
        List<Object[]> byDept = em.createQuery(
                "SELECT e.department, COUNT(e) FROM Employee e WHERE e.tenantId = :tid " +
                "AND e.deletedAt IS NULL AND e.status = 'ACTIVE' GROUP BY e.department ORDER BY 2 DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(5).getResultList();
        var sb = new StringBuilder(String.format("Employee Summary:\n  Total active employees: %s%n", r[0]));
        if (!byDept.isEmpty()) {
            sb.append("  By department:\n");
            byDept.forEach(d -> sb.append(String.format("    %-25s %s employees%n", d[0], d[1])));
        }
        return sb.toString();
    }

    private String getAttendanceSummary(Map<String, Object> input, UUID tenantId) {
        return "Attendance summary queries require specifying a date range. Please check the Attendance module for detailed records.";
    }

    // ── PAYROLL ───────────────────────────────────────────────────────────────

    private String getPayrollSummary(Map<String, Object> input, UUID tenantId) {
        int month = getInt(input, "month", LocalDate.now().getMonthValue());
        int year  = getInt(input, "year",  LocalDate.now().getYear());
        Object[] r = (Object[]) em.createQuery(
                "SELECT COUNT(p), SUM(p.netSalary), SUM(p.grossEarnings), SUM(p.totalDeductions) " +
                "FROM Payslip p WHERE p.tenantId = :tid AND p.deletedAt IS NULL " +
                "AND p.payMonth = :month AND p.payYear = :year")
                .setParameter("tid", tenantId).setParameter("month", month).setParameter("year", year)
                .getSingleResult();
        return String.format("Payroll Summary — %02d/%d:\n  Payslips: %s | Gross: ₹%s | Deductions: ₹%s | Net: ₹%s",
                month, year, r[0], fmt(r[2]), fmt(r[3]), fmt(r[1]));
    }

    private String getPendingPayslips(UUID tenantId) {
        Object count = em.createQuery(
                "SELECT COUNT(p) FROM Payslip p WHERE p.tenantId = :tid AND p.deletedAt IS NULL AND p.status = 'DRAFT'")
                .setParameter("tid", tenantId).getSingleResult();
        return "Pending Payslips (DRAFT — awaiting approval): " + count;
    }

    // ── MANUFACTURING ─────────────────────────────────────────────────────────

    private String getActiveWorkOrders(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT w.orderNumber, w.productName, w.quantity, w.status, w.plannedStartDate " +
                "FROM WorkOrder w WHERE w.tenantId = :tid AND w.deletedAt IS NULL " +
                "AND w.status IN ('PLANNED','IN_PROGRESS') ORDER BY w.plannedStartDate ASC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(20).getResultList();
        if (rows.isEmpty()) return "No active work orders.";
        var sb = new StringBuilder("Active Work Orders (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-15s | %-25s | Qty: %s | %s | Start: %s%n",
                    r[0], truncate((String) r[1], 24), fmt(r[2]), r[3], r[4]));
        return sb.toString();
    }

    private String getProductionSummary(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT w.status, COUNT(w) FROM WorkOrder w WHERE w.tenantId = :tid AND w.deletedAt IS NULL " +
                "GROUP BY w.status",
                Object[].class)
                .setParameter("tid", tenantId).getResultList();
        var sb = new StringBuilder("Manufacturing Summary:\n");
        rows.forEach(r -> sb.append(String.format("  %-15s %s work orders%n", r[0], r[1])));
        return sb.toString();
    }

    // ── CRM ───────────────────────────────────────────────────────────────────

    private String getLeadsPipeline(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT l.status, COUNT(l), SUM(l.estimatedValue) FROM Lead l " +
                "WHERE l.tenantId = :tid AND l.deletedAt IS NULL GROUP BY l.status",
                Object[].class)
                .setParameter("tid", tenantId).getResultList();
        if (rows.isEmpty()) return "No leads found.";
        var sb = new StringBuilder("CRM Leads Pipeline:\n");
        rows.forEach(r -> sb.append(String.format("  %-15s %s leads | Estimated value: ₹%s%n", r[0], r[1], fmt(r[2]))));
        return sb.toString();
    }

    private String getWonLeads(UUID tenantId) {
        List<Object[]> rows = em.createQuery(
                "SELECT l.name, l.company, l.estimatedValue, l.expectedCloseDate FROM Lead l " +
                "WHERE l.tenantId = :tid AND l.deletedAt IS NULL AND l.status = 'WON' " +
                "ORDER BY l.updatedAt DESC",
                Object[].class)
                .setParameter("tid", tenantId).setMaxResults(10).getResultList();
        if (rows.isEmpty()) return "No won leads found.";
        var sb = new StringBuilder("Won Leads (" + rows.size() + "):\n");
        for (Object[] r : rows)
            sb.append(String.format("  %-25s | %-20s | ₹%s | Close: %s%n",
                    truncate((String) r[0], 24), truncate((String) r[1], 19), fmt(r[2]), r[3]));
        return sb.toString();
    }

    // ── GENERAL ───────────────────────────────────────────────────────────────

    private String getDashboardSummary(UUID tenantId) {
        var sb = new StringBuilder("ERP Dashboard Summary:\n");
        try {
            Object customers = em.createQuery("SELECT COUNT(c) FROM Customer c WHERE c.tenantId=:t AND c.deletedAt IS NULL")
                    .setParameter("t", tenantId).getSingleResult();
            sb.append("  Customers: ").append(customers).append("\n");
        } catch (Exception ignored) {}
        try {
            Object[] inv = (Object[]) em.createQuery("SELECT COUNT(i), SUM(i.balanceDue) FROM Invoice i WHERE i.tenantId=:t AND i.deletedAt IS NULL AND i.status IN ('SENT','PARTIALLY_PAID')")
                    .setParameter("t", tenantId).getSingleResult();
            sb.append("  Outstanding invoices: ").append(inv[0]).append(" | Total: ₹").append(fmt(inv[1])).append("\n");
        } catch (Exception ignored) {}
        try {
            Object pos = em.createQuery("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.tenantId=:t AND p.deletedAt IS NULL AND p.status IN ('DRAFT','CONFIRMED','APPROVED')")
                    .setParameter("t", tenantId).getSingleResult();
            sb.append("  Pending purchase orders: ").append(pos).append("\n");
        } catch (Exception ignored) {}
        try {
            Object emp = em.createQuery("SELECT COUNT(e) FROM Employee e WHERE e.tenantId=:t AND e.deletedAt IS NULL AND e.status='ACTIVE'")
                    .setParameter("t", tenantId).getSingleResult();
            sb.append("  Active employees: ").append(emp).append("\n");
        } catch (Exception ignored) {}
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String fmt(Object val) {
        if (val == null) return "0";
        if (val instanceof BigDecimal bd) return String.format("%,.2f", bd);
        if (val instanceof Number n) return String.format("%,.2f", n.doubleValue());
        return val.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private BigDecimal getBigDecimal(Map<String, Object> input, String key, BigDecimal def) {
        if (input == null || !input.containsKey(key) || input.get(key) == null) return def;
        try { return new BigDecimal(input.get(key).toString()); } catch (Exception e) { return def; }
    }

    private String getString(Map<String, Object> input, String key, String def) {
        if (input == null || !input.containsKey(key) || input.get(key) == null) return def;
        String v = input.get(key).toString().trim();
        return v.isEmpty() ? def : v;
    }

    private int getInt(Map<String, Object> input, String key, int def) {
        if (input == null || !input.containsKey(key) || input.get(key) == null) return def;
        try { return Integer.parseInt(input.get(key).toString()); } catch (Exception e) { return def; }
    }
}
