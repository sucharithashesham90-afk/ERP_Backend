package com.erp.platform.config;

import com.erp.platform.modules.accounting.entity.*;
import com.erp.platform.modules.accounting.repository.*;
import com.erp.platform.modules.dispatch.entity.*;
import com.erp.platform.modules.dispatch.repository.*;
import com.erp.platform.modules.inventory.entity.*;
import com.erp.platform.modules.inventory.repository.*;
import com.erp.platform.modules.master.entity.*;
import com.erp.platform.modules.master.repository.*;
import com.erp.platform.modules.promotions.entity.*;
import com.erp.platform.modules.promotions.repository.*;
import com.erp.platform.modules.purchase.entity.*;
import com.erp.platform.modules.purchase.repository.*;
import com.erp.platform.modules.reports.entity.*;
import com.erp.platform.modules.reports.repository.*;
import com.erp.platform.modules.sales.entity.*;
import com.erp.platform.modules.sales.repository.*;
import com.erp.platform.modules.supplier.entity.*;
import com.erp.platform.modules.supplier.repository.*;
import com.erp.platform.modules.workflow.entity.*;
import com.erp.platform.modules.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class Phase2Phase3DataSeeder implements CommandLineRunner {

    static final UUID T = DataInitializer.DEFAULT_TENANT_ID;

    // ── Lookup repos (read existing Phase 1 data) ─────────────────────────────
    private final VendorRepository         vendorRepo;
    private final CustomerRepository       customerRepo;
    private final ProductRepository        productRepo;
    private final WarehouseRepository      warehouseRepo;

    // ── Phase 2: Dispatch ─────────────────────────────────────────────────────
    private final DispatchRepository            dispatchRepo;

    // ── Phase 2: Promotions ───────────────────────────────────────────────────
    private final PromotionRepository           promotionRepo;

    // ── Phase 2: Supplier Contracts & Performance ─────────────────────────────
    private final SupplierContractRepository    supplierContractRepo;
    private final SupplierPerformanceRepository supplierPerfRepo;
    private final SupplierNonConformanceRepository supplierNcRepo;

    // ── Phase 2: Accounting Extensions ───────────────────────────────────────
    private final DimensionRepository           dimensionRepo;
    private final DimensionValueRepository      dimensionValueRepo;
    private final PeriodCloseRepository         periodCloseRepo;
    private final BudgetEntryRepository         budgetEntryRepo;

    // ── Phase 2: Storage Locations ────────────────────────────────────────────
    private final StorageLocationRepository     storageLocationRepo;
    private final LocationStockRepository       locationStockRepo;

    // ── Phase 3: UoM ──────────────────────────────────────────────────────────
    private final UnitOfMeasureRepository       uomRepo;
    private final UoMConversionRepository       uomConversionRepo;

    // ── Phase 3: Brand + ProductLine ──────────────────────────────────────────
    private final BrandRepository               brandRepo;
    private final ProductLineRepository         productLineRepo;

    // ── Phase 3: Opening Balances ─────────────────────────────────────────────
    private final OpeningBalanceRepository      openingBalanceRepo;
    private final MigrationBatchRepository      migrationBatchRepo;

    // ── Phase 3: Sales Planning ───────────────────────────────────────────────
    private final SalesPlanRepository           salesPlanRepo;
    private final DemandForecastRepository      demandForecastRepo;

    // ── Phase 3: Supplier Payments ────────────────────────────────────────────
    private final SupplierPaymentRepository     supplierPaymentRepo;

    // ── Phase 3: Scrap ────────────────────────────────────────────────────────
    private final ScrapEntryRepository          scrapEntryRepo;
    private final ScrapDisposalRepository       scrapDisposalRepo;

    // ── Phase 3: Approval Workflow ────────────────────────────────────────────
    private final ApprovalRuleRepository        approvalRuleRepo;

    // ── Phase 3: Reports ──────────────────────────────────────────────────────
    private final ReportDefinitionRepository    reportDefinitionRepo;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void run(String... args) {
        try {
            seedAll();
        } catch (Exception e) {
            // Demo data must never decide whether the application runs. This threw on a fresh
            // database and, unlike the seeder before it, let the exception out of run(), so Spring
            // treated it as a failed start and the process exited after reporting itself started.
            log.error("Phase2Phase3DataSeeder failed — demo data may be incomplete; "
                    + "application still starts. Cause: {}", e.getMessage(), e);
        }
    }

    private void seedAll() {
        if (dispatchRepo.count() > 0) {
            log.info("Phase 2/3 data already present — skipping Phase2Phase3DataSeeder");
            return;
        }
        log.info("Seeding Phase 2 & Phase 3 test data...");

        // Load existing Phase 1 entities
        List<Vendor>    vendors    = vendorRepo.findAll().stream()
                .filter(v -> T.equals(v.getTenantId()) && v.getDeletedAt() == null).toList();
        List<Customer>  customers  = customerRepo.findAll().stream()
                .filter(c -> T.equals(c.getTenantId()) && c.getDeletedAt() == null).toList();
        List<Product>   products   = productRepo.findAll().stream()
                .filter(p -> T.equals(p.getTenantId()) && p.getDeletedAt() == null).toList();
        List<Warehouse> warehouses = warehouseRepo.findAll().stream()
                .filter(w -> T.equals(w.getTenantId()) && w.getDeletedAt() == null).toList();

        if (vendors.isEmpty() || customers.isEmpty() || products.isEmpty() || warehouses.isEmpty()) {
            log.warn("Phase2Phase3DataSeeder: Base data (vendors/customers/products/warehouses) not found — skipping. Ensure TestDataSeeder has run first.");
            return;
        }

        Vendor vendorABC  = byCode(vendors,    "VND001");
        Vendor vendorXYZ  = byCode(vendors,    "VND002");
        Vendor vendorTech = byCode(vendors,    "VND003");

        Customer custMega  = byCode(customers, "CUST001");
        Customer custEnt   = byCode(customers, "CUST002");
        Customer custQuick = byCode(customers, "CUST003");

        Product pSteelRods  = byCode(products, "PRD001");
        Product pPlastic    = byCode(products, "PRD002");
        Product pWidgetA    = byCode(products, "PRD003");
        Product pWidgetB    = byCode(products, "PRD004");
        Product pMachinePart= byCode(products, "PRD007");
        Product pElecComp   = byCode(products, "PRD008");

        Warehouse whMain   = byCode(warehouses, "WH001");
        Warehouse whMumbai = byCode(warehouses, "WH002");

        // ── Phase 2 ──────────────────────────────────────────────────────────
        seedDispatches(custMega, custEnt, custQuick, pWidgetA, pWidgetB, pMachinePart, whMain);
        log.info("  ✓ Dispatches seeded");

        seedPromotions(pWidgetA, pWidgetB);
        log.info("  ✓ Promotions seeded");

        seedSupplierContracts(vendorABC, vendorXYZ, vendorTech, pSteelRods, pPlastic, pElecComp);
        log.info("  ✓ Supplier Contracts seeded");

        seedSupplierPerformance(vendorABC, vendorXYZ, vendorTech);
        log.info("  ✓ Supplier Performance seeded");

        seedSupplierNonConformances(vendorABC, vendorXYZ);
        log.info("  ✓ Supplier Non-Conformances seeded");

        seedDimensions();
        log.info("  ✓ Dimensions seeded");

        seedPeriodClose();
        log.info("  ✓ Period Close seeded");

        seedBudget();
        log.info("  ✓ Budget Entries seeded");

        seedStorageLocations(whMain, whMumbai, pWidgetA, pWidgetB, pSteelRods);
        log.info("  ✓ Storage Locations seeded");

        // ── Phase 3 ──────────────────────────────────────────────────────────
        seedUoM();
        log.info("  ✓ Units of Measure seeded");

        seedBrandsAndProductLines();
        log.info("  ✓ Brands & Product Lines seeded");

        seedOpeningBalances(custMega, custEnt, vendorABC, vendorXYZ, whMain, pWidgetA, pWidgetB);
        log.info("  ✓ Opening Balances seeded");

        seedSalesPlans(pWidgetA, pWidgetB, pMachinePart);
        log.info("  ✓ Sales Plans seeded");

        seedDemandForecasts(pWidgetA, pWidgetB, pSteelRods, pElecComp, whMain);
        log.info("  ✓ Demand Forecasts seeded");

        seedSupplierPayments(vendorABC, vendorXYZ);
        log.info("  ✓ Supplier Payments seeded");

        seedScrapEntries(whMain, pSteelRods, pWidgetA, pElecComp);
        log.info("  ✓ Scrap Entries seeded");

        seedApprovalRules();
        log.info("  ✓ Approval Rules seeded");

        seedReportDefinitions();
        log.info("  ✓ Report Definitions seeded");

        log.info("Phase 2 & Phase 3 test data seeding complete.");
    }

    // ── Generic lookup helper ─────────────────────────────────────────────────

    private <R> R byCode(List<R> list, String code) {
        return list.stream()
                .filter(e -> {
                    try {
                        return code.equals(e.getClass().getMethod("getCode").invoke(e));
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .findFirst()
                .orElseGet(() -> list.get(0));
    }

    // ══════════════════════════ Phase 2 Seed Methods ══════════════════════════

    private void seedDispatches(Customer c1, Customer c2, Customer c3,
                                 Product p1, Product p2, Product p3, Warehouse wh) {
        saveDispatch("DSP-2026-001", Dispatch.DispatchType.CUSTOMER, Dispatch.DispatchStatus.DELIVERED,
                c1.getId(), c1.getName(), "Plot 45, Jubilee Hills, Hyderabad",
                LocalDate.of(2026,4,10), LocalDate.of(2026,4,13), LocalDate.of(2026,4,13),
                "BlueDart Logistics", "9800111222", "TL-01-AB-1234", "BD-HYD-20260410-001",
                "LR/2026/04/1234", 350.0, 3, 45.5,
                List.<Object[]>of(new Object[]{p1, 100.0, "PCS", "LOT-20260401-001"},
                                  new Object[]{p2, 50.0,  "PCS", "LOT-20260401-002"}));

        saveDispatch("DSP-2026-002", Dispatch.DispatchType.CUSTOMER, Dispatch.DispatchStatus.IN_TRANSIT,
                c2.getId(), c2.getName(), "14th Floor, BKC, Mumbai",
                LocalDate.of(2026,5,5), LocalDate.of(2026,5,8), null,
                "DTDC Express", "9800222333", "MH-04-CD-5678", "DTDC-MUM-20260505-002",
                "LR/2026/05/5678", 500.0, 2, 32.0,
                List.<Object[]>of(new Object[]{p3, 10.0, "PCS", "LOT-20260501-003"}));

        saveDispatch("DSP-2026-003", Dispatch.DispatchType.INTER_BRANCH, Dispatch.DispatchStatus.PACKED,
                null, "Mumbai Branch", "Regional Hub Mumbai, MIDC",
                LocalDate.of(2026,5,15), LocalDate.of(2026,5,17), null,
                "Own Vehicle", "9800333444", "TS-09-EF-9012", null,
                null, 0.0, 4, 120.0,
                List.<Object[]>of(new Object[]{p1, 200.0, "PCS", "LOT-20260501-001"},
                                  new Object[]{p2, 80.0,  "PCS", "LOT-20260501-002"},
                                  new Object[]{p3, 5.0,   "PCS", "LOT-20260501-003"}));

        saveDispatch("DSP-2026-004", Dispatch.DispatchType.CUSTOMER, Dispatch.DispatchStatus.DRAFT,
                c3.getId(), c3.getName(), "Shop No 12, Ameerpet, Hyderabad",
                LocalDate.of(2026,6,1), LocalDate.of(2026,6,3), null,
                "Delhivery", "9800444555", null, null,
                null, 150.0, 1, 8.5,
                List.<Object[]>of(new Object[]{p1, 30.0, "PCS", "LOT-20260601-001"}));
    }

    private void saveDispatch(String num, Dispatch.DispatchType type, Dispatch.DispatchStatus status,
                               UUID custId, String custName, String addr,
                               LocalDate dispDate, LocalDate expDelivery, LocalDate actDelivery,
                               String carrier, String carrierPhone, String vehicle, String tracking,
                               String lr, double freight, int pkgs, double weight,
                               List<Object[]> items) {
        Dispatch d = new Dispatch(); d.setTenantId(T);
        d.setDispatchNumber(num); d.setDispatchType(type); d.setStatus(status);
        d.setCustomerId(custId); d.setCustomerName(custName); d.setDeliveryAddress(addr);
        d.setDispatchDate(dispDate); d.setExpectedDeliveryDate(expDelivery); d.setActualDeliveryDate(actDelivery);
        d.setCarrierName(carrier); d.setCarrierPhone(carrierPhone); d.setVehicleNumber(vehicle);
        d.setTrackingNumber(tracking); d.setLrNumber(lr);
        d.setFreightCharges(BigDecimal.valueOf(freight));
        d.setTotalPackages(pkgs); d.setTotalWeight(BigDecimal.valueOf(weight)); d.setWeightUnit("KG");
        if (status == Dispatch.DispatchStatus.DELIVERED) {
            d.setPodReceived(true); d.setPodDate(actDelivery); d.setPodReference("POD-" + num);
        }
        List<DispatchItem> dItems = new ArrayList<>();
        for (Object[] row : items) {
            Product p = (Product) row[0]; double qty = (double) row[1];
            String unit = (String) row[2]; String lot = (String) row[3];
            DispatchItem di = new DispatchItem(); di.setTenantId(T); di.setDispatch(d);
            di.setProductId(p.getId()); di.setProductName(p.getName());
            di.setLotNumber(lot); di.setQuantity(BigDecimal.valueOf(qty)); di.setUnit(unit);
            di.setPackageCount(1); di.setPackageType("CARTON");
            dItems.add(di);
        }
        d.setItems(dItems);
        dispatchRepo.save(d);
    }

    private void seedPromotions(Product p1, Product p2) {
        Promotion pr1 = new Promotion(); pr1.setTenantId(T);
        pr1.setPromotionCode("PROMO-2026-001"); pr1.setName("Summer Sale 15% Off");
        pr1.setPromotionType(Promotion.PromotionType.PERCENTAGE_DISCOUNT);
        pr1.setApplicableTo(Promotion.ApplicableTo.ALL_CUSTOMERS);
        pr1.setDiscountPercent(new BigDecimal("15")); pr1.setMinOrderValue(new BigDecimal("5000"));
        pr1.setStartDate(LocalDate.of(2026,5,1)); pr1.setEndDate(LocalDate.of(2026,5,31));
        pr1.setUsageLimit(500); pr1.setUsageCount(87); pr1.setStackable(false); pr1.setActive(true);
        promotionRepo.save(pr1);

        Promotion pr2 = new Promotion(); pr2.setTenantId(T);
        pr2.setPromotionCode("PROMO-2026-002"); pr2.setName("Wholesale Buy 10 Get 1 Free");
        pr2.setPromotionType(Promotion.PromotionType.BUY_X_GET_Y);
        pr2.setApplicableTo(Promotion.ApplicableTo.SPECIFIC_CATEGORY); pr2.setCustomerCategory("WHOLESALE");
        pr2.setBuyQuantity(10); pr2.setGetQuantity(1);
        pr2.setFreeProductId(p1.getId()); pr2.setFreeProductName(p1.getName());
        pr2.setMinOrderQty(new BigDecimal("10"));
        pr2.setStartDate(LocalDate.of(2026,4,1)); pr2.setEndDate(LocalDate.of(2027,3,31));
        pr2.setUsageLimit(0); pr2.setUsageCount(23); pr2.setStackable(false); pr2.setActive(true);
        promotionRepo.save(pr2);

        Promotion pr3 = new Promotion(); pr3.setTenantId(T);
        pr3.setPromotionCode("PROMO-2026-003"); pr3.setName("Premium Widget Bundle Deal");
        pr3.setPromotionType(Promotion.PromotionType.BUNDLE);
        pr3.setApplicableTo(Promotion.ApplicableTo.ALL_CUSTOMERS);
        pr3.setDiscountAmount(new BigDecimal("500")); pr3.setMinOrderValue(new BigDecimal("10000"));
        pr3.setStartDate(LocalDate.of(2026,6,1)); pr3.setEndDate(LocalDate.of(2026,6,30));
        pr3.setUsageLimit(100); pr3.setUsageCount(0); pr3.setStackable(true); pr3.setActive(true);
        promotionRepo.save(pr3);

        Promotion pr4 = new Promotion(); pr4.setTenantId(T);
        pr4.setPromotionCode("PROMO-2025-010"); pr4.setName("Year-End Cashback 5%");
        pr4.setPromotionType(Promotion.PromotionType.CASHBACK);
        pr4.setApplicableTo(Promotion.ApplicableTo.ALL_CUSTOMERS);
        pr4.setDiscountPercent(new BigDecimal("5")); pr4.setMinOrderValue(new BigDecimal("20000"));
        pr4.setMaxDiscountAmount(new BigDecimal("2000"));
        pr4.setStartDate(LocalDate.of(2025,12,15)); pr4.setEndDate(LocalDate.of(2025,12,31));
        pr4.setUsageLimit(200); pr4.setUsageCount(200); pr4.setStackable(false); pr4.setActive(false);
        promotionRepo.save(pr4);
    }

    private void seedSupplierContracts(Vendor v1, Vendor v2, Vendor v3,
                                        Product p1, Product p2, Product p3) {
        saveSupplierContract("SC-2026-001", v1, SupplierContract.ContractType.ANNUAL,
                SupplierContract.ContractStatus.ACTIVE,
                LocalDate.of(2026,4,1), LocalDate.of(2027,3,31), 5000000.0,
                "Net 30 days", "Ex-Works Hyderabad", "As per IS standards", 30, false,
                "Annual supply agreement for raw materials",
                List.<Object[]>of(new Object[]{p1, 58.0, 500.0, 5000.0, "KG", 14},
                                  new Object[]{p2, 98.0, 200.0, 2000.0, "KG", 14}));

        saveSupplierContract("SC-2026-002", v2, SupplierContract.ContractType.ANNUAL,
                SupplierContract.ContractStatus.ACTIVE,
                LocalDate.of(2026,1,1), LocalDate.of(2026,12,31), 3000000.0,
                "Net 45 days", "CIF Hyderabad", "REACH compliance required", 60, true,
                "Annual plastic pellets supply",
                List.<Object[]>of(new Object[]{p2, 97.0, 100.0, 1000.0, "KG", 21}));

        saveSupplierContract("SC-2025-005", v3, SupplierContract.ContractType.SERVICE_LEVEL_AGREEMENT,
                SupplierContract.ContractStatus.EXPIRING_SOON,
                LocalDate.of(2025,7,1), LocalDate.of(2026,6,30), 1200000.0,
                "Monthly advance", "Online", "99.9% uptime guaranteed", 90, false,
                "IT services and support SLA",
                List.<Object[]>of(new Object[]{p3, 0.0, 0.0, 0.0, "MONTHS", 0}));
    }

    private void saveSupplierContract(String num, Vendor vendor, SupplierContract.ContractType type,
                                       SupplierContract.ContractStatus status,
                                       LocalDate start, LocalDate end, double value,
                                       String payTerms, String delivTerms, String qualTerms,
                                       int noticeDays, boolean autoRenew, String notes,
                                       List<Object[]> items) {
        SupplierContract sc = new SupplierContract(); sc.setTenantId(T);
        sc.setContractNumber(num); sc.setVendorId(vendor.getId()); sc.setVendorName(vendor.getName());
        sc.setContractType(type); sc.setStatus(status);
        sc.setStartDate(start); sc.setEndDate(end);
        sc.setTotalContractValue(BigDecimal.valueOf(value)); sc.setCurrency("INR");
        sc.setPaymentTerms(payTerms); sc.setDeliveryTerms(delivTerms); sc.setQualityTerms(qualTerms);
        sc.setNoticePeriodDays(noticeDays); sc.setAutoRenew(autoRenew); sc.setNotes(notes);
        List<ContractItem> cItems = new ArrayList<>();
        for (Object[] row : items) {
            Product p = (Product) row[0]; double price = (double) row[1];
            double minQ = (double) row[2]; double maxQ = (double) row[3];
            String unit = (String) row[4]; int lead = (int) row[5];
            ContractItem ci = new ContractItem(); ci.setTenantId(T); ci.setContract(sc);
            ci.setProductId(p.getId()); ci.setProductName(p.getName());
            ci.setAgreedPrice(BigDecimal.valueOf(price));
            ci.setMinQuantity(BigDecimal.valueOf(minQ)); ci.setMaxQuantity(BigDecimal.valueOf(maxQ));
            ci.setUnit(unit); ci.setLeadTimeDays(lead);
            cItems.add(ci);
        }
        sc.setItems(cItems);
        supplierContractRepo.save(sc);
    }

    private void seedSupplierPerformance(Vendor v1, Vendor v2, Vendor v3) {
        saveSupplierPerf(v1, "Q1-2026", LocalDate.of(2026,4,5),  92.0, 95.0, 88.0, 90.0, 45, 42, 3, 1, 44, "Excellent overall performance");
        saveSupplierPerf(v2, "Q1-2026", LocalDate.of(2026,4,6),  78.0, 82.0, 75.0, 85.0, 30, 23, 7, 3, 27, "Delivery delays in March — needs improvement");
        saveSupplierPerf(v3, "Q1-2026", LocalDate.of(2026,4,7),  98.0, 99.0, 97.0, 88.0, 12, 12, 0, 0, 12, "Outstanding service quality");
        saveSupplierPerf(v1, "Q4-2025", LocalDate.of(2026,1,8),  88.0, 90.0, 85.0, 87.0, 40, 35, 5, 2, 38, "Good performance last quarter");
        saveSupplierPerf(v2, "Q4-2025", LocalDate.of(2026,1,9),  55.0, 60.0, 65.0, 70.0, 25, 14,11, 6, 19, "Significant quality issues in December");
    }

    private void saveSupplierPerf(Vendor v, String period, LocalDate evalDate,
                                   double otd, double qual, double resp, double price,
                                   int total, int onTime, int late, int rejected, int accepted, String remarks) {
        SupplierPerformance sp = new SupplierPerformance(); sp.setTenantId(T);
        sp.setVendorId(v.getId()); sp.setVendorName(v.getName());
        sp.setEvaluationPeriod(period); sp.setEvaluationDate(evalDate);
        sp.setOnTimeDeliveryScore(BigDecimal.valueOf(otd)); sp.setQualityScore(BigDecimal.valueOf(qual));
        sp.setResponseScore(BigDecimal.valueOf(resp)); sp.setPricingScore(BigDecimal.valueOf(price));
        BigDecimal overall = BigDecimal.valueOf((otd + qual + resp + price) / 4.0)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        sp.setOverallScore(overall);
        int score = overall.intValue();
        if      (score >= 80) sp.setStatus(SupplierPerformance.PerfStatus.PREFERRED);
        else if (score >= 60) sp.setStatus(SupplierPerformance.PerfStatus.APPROVED);
        else if (score >= 40) sp.setStatus(SupplierPerformance.PerfStatus.CONDITIONAL);
        else                  sp.setStatus(SupplierPerformance.PerfStatus.BLACKLISTED);
        sp.setTotalOrders(total); sp.setOnTimeOrders(onTime); sp.setLateOrders(late);
        sp.setRejectedLots(rejected); sp.setAcceptedLots(accepted);
        sp.setRemarks(remarks); sp.setEvaluatedBy("Quality Manager");
        supplierPerfRepo.save(sp);
    }

    private void seedSupplierNonConformances(Vendor v1, Vendor v2) {
        saveSupplierNc("NC-2026-001", v2, SupplierNonConformance.NCType.QUALITY,
                SupplierNonConformance.Severity.MAJOR, SupplierNonConformance.NCStatus.CLOSED,
                LocalDate.of(2026,2,10), LocalDate.of(2026,2,25),
                "Plastic pellets batch failed moisture content test — 3.2% vs spec max 1.5%",
                "Inadequate drying process at supplier plant",
                "Installed new moisture control equipment; enhanced pre-dispatch testing");
        saveSupplierNc("NC-2026-002", v2, SupplierNonConformance.NCType.DELIVERY,
                SupplierNonConformance.Severity.MINOR, SupplierNonConformance.NCStatus.VENDOR_RESPONDED,
                LocalDate.of(2026,3,15), null,
                "Delivery 5 days late in March — impacted production schedule",
                "Logistics partner issues during state transport strike",
                "Will use backup logistics partner for future deliveries");
        saveSupplierNc("NC-2026-003", v1, SupplierNonConformance.NCType.DOCUMENTATION,
                SupplierNonConformance.Severity.MINOR, SupplierNonConformance.NCStatus.OPEN,
                LocalDate.of(2026,5,20), null,
                "Test certificates missing for steel rod batch PR-2026-042",
                null, null);
    }

    private void saveSupplierNc(String num, Vendor v, SupplierNonConformance.NCType type,
                                 SupplierNonConformance.Severity severity, SupplierNonConformance.NCStatus status,
                                 LocalDate issueDate, LocalDate closureDate,
                                 String desc, String rootCause, String corrective) {
        SupplierNonConformance nc = new SupplierNonConformance(); nc.setTenantId(T);
        nc.setNcNumber(num); nc.setVendorId(v.getId()); nc.setVendorName(v.getName());
        nc.setIssueDate(issueDate); nc.setIssueType(type); nc.setSeverity(severity);
        nc.setDescription(desc); nc.setRootCause(rootCause); nc.setCorrectiveAction(corrective);
        nc.setClosureDate(closureDate); nc.setStatus(status);
        if (status == SupplierNonConformance.NCStatus.VENDOR_RESPONDED
                || status == SupplierNonConformance.NCStatus.CLOSED) {
            nc.setVendorResponse("Acknowledged — corrective actions in progress");
        }
        supplierNcRepo.save(nc);
    }

    private void seedDimensions() {
        Dimension dimBU = saveDimension("Business Unit", "BU",  "Segment by business unit", false);
        saveDimensionValue(dimBU, "MFGD", "Manufacturing Division");
        saveDimensionValue(dimBU, "SALD", "Sales Division");
        saveDimensionValue(dimBU, "ITDV", "IT Division");

        Dimension dimGeo = saveDimension("Geography", "GEO", "Regional P&L tracking", false);
        saveDimensionValue(dimGeo, "SOUTH", "South India");
        saveDimensionValue(dimGeo, "WEST",  "West India");
        saveDimensionValue(dimGeo, "NORTH", "North India");
        saveDimensionValue(dimGeo, "EXPORT","Export Markets");

        Dimension dimProj = saveDimension("Project", "PROJ", "Project-based cost tracking", false);
        saveDimensionValue(dimProj, "PROJ001", "ERP Implementation Project");
        saveDimensionValue(dimProj, "PROJ002", "Factory Expansion Project");
        saveDimensionValue(dimProj, "PROJ003", "Digital Transformation Initiative");
    }

    private Dimension saveDimension(String name, String code, String desc, boolean mandatory) {
        Dimension d = new Dimension(); d.setTenantId(T);
        d.setName(name); d.setCode(code); d.setDescription(desc);
        d.setMandatory(mandatory); d.setActive(true);
        return dimensionRepo.save(d);
    }

    private void saveDimensionValue(Dimension dim, String code, String name) {
        DimensionValue dv = new DimensionValue(); dv.setTenantId(T);
        dv.setDimension(dim); dv.setCode(code); dv.setName(name); dv.setActive(true);
        dimensionValueRepo.save(dv);
    }

    private void seedPeriodClose() {
        String[][] periods = {
            {"2026","1","Jan 2026","CLOSED"}, {"2026","2","Feb 2026","CLOSED"},
            {"2026","3","Mar 2026","CLOSED"}, {"2026","4","Apr 2026","CLOSED"},
            {"2026","5","May 2026","CLOSED"}, {"2026","6","Jun 2026","OPEN"}
        };
        for (String[] p : periods) {
            PeriodClose pc = new PeriodClose(); pc.setTenantId(T);
            pc.setPeriodYear(Integer.parseInt(p[0])); pc.setPeriodMonth(Integer.parseInt(p[1]));
            pc.setPeriodName(p[2]);
            pc.setStatus(PeriodClose.CloseStatus.valueOf(p[3]));
            if (!"OPEN".equals(p[3])) {
                int m = Integer.parseInt(p[1]);
                pc.setClosedAt(LocalDateTime.of(Integer.parseInt(p[0]), m < 12 ? m + 1 : 1, 5, 18, 0));
                pc.setClosedBy("Finance Manager");
            }
            periodCloseRepo.save(pc);
        }
    }

    private void seedBudget() {
        Object[][] budgets = {
            {"ACC001","Revenue - Product Sales",       5000000.0, 4876543.0},
            {"ACC002","Revenue - Service Income",       800000.0,  712000.0},
            {"ACC003","Cost of Goods Sold",           3000000.0, 2934210.0},
            {"ACC004","Employee Salaries",             1200000.0, 1187500.0},
            {"ACC005","Office & Administration",        180000.0,  165320.0},
            {"ACC006","Marketing & Promotions",         250000.0,  198750.0},
            {"ACC007","Travel & Conveyance",             80000.0,   72450.0},
            {"ACC008","Depreciation",                   150000.0,  150000.0},
        };
        for (Object[] b : budgets) {
            BudgetEntry be = new BudgetEntry(); be.setTenantId(T);
            be.setPeriodYear(2026); be.setPeriodMonth(5);
            be.setAccountCode((String) b[0]); be.setAccountName((String) b[1]);
            be.setBudgetAmount(BigDecimal.valueOf((double) b[2]));
            be.setActualAmount(BigDecimal.valueOf((double) b[3]));
            be.setVariance(BigDecimal.valueOf((double) b[2] - (double) b[3]));
            budgetEntryRepo.save(be);
        }
    }

    private void seedStorageLocations(Warehouse wh1, Warehouse wh2,
                                       Product p1, Product p2, Product p3) {
        StorageLocation locA01  = saveStorageLocation(wh1, "A-01-01", "Aisle A Rack 1 Bin 1", StorageLocation.LocationType.BIN,            "A",null,"01","01", 500.0);
        StorageLocation locA01b = saveStorageLocation(wh1, "A-01-02", "Aisle A Rack 1 Bin 2", StorageLocation.LocationType.BIN,            "A",null,"01","02", 500.0);
        StorageLocation locB01  = saveStorageLocation(wh1, "B-01-01", "Aisle B Rack 1 Bin 1", StorageLocation.LocationType.BIN,            "B",null,"01","01",1000.0);
        StorageLocation locCold = saveStorageLocation(wh1, "COLD-01", "Cold Storage Zone 1",  StorageLocation.LocationType.COLD_STORAGE,   null,null,null,"01", 200.0);
        saveStorageLocation(wh1, "DISP-01", "Dispatch Staging Area",  StorageLocation.LocationType.DISPATCH_AREA, null,null,null,null,2000.0);
        saveStorageLocation(wh2, "M-A-01",  "Mumbai Rack A Bin 1",    StorageLocation.LocationType.BIN,           "A",null,"01",null, 300.0);
        saveStorageLocation(wh2, "M-B-01",  "Mumbai Rack B Bin 1",    StorageLocation.LocationType.BIN,           "B",null,"01",null, 300.0);

        saveLocationStock(locA01,  wh1, p1, "LOT-20260401-001", 250.0, 45.0);
        saveLocationStock(locA01b, wh1, p2, "LOT-20260401-002", 120.0,350.0);
        saveLocationStock(locB01,  wh1, p3, "LOT-20260501-003", 800.0, 55.0);
        saveLocationStock(locCold, wh1, p1, "LOT-20260501-001", 100.0, 45.0);
    }

    private StorageLocation saveStorageLocation(Warehouse wh, String code, String name,
                                                 StorageLocation.LocationType type,
                                                 String aisle, String rack, String bin, String bin2, double cap) {
        StorageLocation loc = new StorageLocation(); loc.setTenantId(T);
        loc.setWarehouseId(wh.getId()); loc.setWarehouseName(wh.getName());
        loc.setCode(code); loc.setName(name); loc.setLocationType(type);
        loc.setAisle(aisle); loc.setRack(rack != null ? rack : bin); loc.setBin(bin2);
        loc.setCapacity(BigDecimal.valueOf(cap)); loc.setCapacityUnit("PCS");
        loc.setCurrentOccupancy(BigDecimal.ZERO); loc.setActive(true);
        return storageLocationRepo.save(loc);
    }

    private void saveLocationStock(StorageLocation loc, Warehouse wh, Product prod, String lot, double qty, double cost) {
        LocationStock ls = new LocationStock(); ls.setTenantId(T);
        ls.setLocationId(loc.getId()); ls.setLocationCode(loc.getCode());
        ls.setWarehouseId(wh.getId());
        ls.setProductId(prod.getId()); ls.setProductName(prod.getName());
        ls.setLotNumber(lot); ls.setQuantity(BigDecimal.valueOf(qty));
        ls.setReservedQuantity(BigDecimal.ZERO); ls.setUnitCost(BigDecimal.valueOf(cost));
        locationStockRepo.save(ls);
    }

    // ══════════════════════════ Phase 3 Seed Methods ══════════════════════════

    private void seedUoM() {
        UnitOfMeasure uomKg  = saveUoM("KG",  "Kilogram",     "kg",  UnitOfMeasure.UoMType.WEIGHT,   true,  3);
        UnitOfMeasure uomGm  = saveUoM("GM",  "Gram",         "g",   UnitOfMeasure.UoMType.WEIGHT,   false, 3);
        UnitOfMeasure uomMt  = saveUoM("MT",  "Metric Tonne", "MT",  UnitOfMeasure.UoMType.WEIGHT,   false, 3);
        UnitOfMeasure uomPcs = saveUoM("PCS", "Pieces",       "pcs", UnitOfMeasure.UoMType.QUANTITY,  true,  0);
        UnitOfMeasure uomBox = saveUoM("BOX", "Box",          "box", UnitOfMeasure.UoMType.QUANTITY,  false, 0);
        UnitOfMeasure uomDzn = saveUoM("DZN", "Dozen",        "dz",  UnitOfMeasure.UoMType.QUANTITY,  false, 0);
        UnitOfMeasure uomLtr = saveUoM("LTR", "Litre",        "L",   UnitOfMeasure.UoMType.VOLUME,    true,  3);
        UnitOfMeasure uomMl  = saveUoM("ML",  "Millilitre",   "mL",  UnitOfMeasure.UoMType.VOLUME,    false, 3);
        UnitOfMeasure uomMtr = saveUoM("MTR", "Metre",        "m",   UnitOfMeasure.UoMType.LENGTH,    true,  3);
        UnitOfMeasure uomCm  = saveUoM("CM",  "Centimetre",   "cm",  UnitOfMeasure.UoMType.LENGTH,    false, 2);
        UnitOfMeasure uomHr  = saveUoM("HRS", "Hours",        "hr",  UnitOfMeasure.UoMType.TIME,      true,  2);
        UnitOfMeasure uomDay = saveUoM("DAY", "Day",          "day", UnitOfMeasure.UoMType.TIME,      false, 0);

        saveUoMConversion(uomKg,  uomGm,  1000.0);
        saveUoMConversion(uomMt,  uomKg,  1000.0);
        saveUoMConversion(uomDzn, uomPcs,   12.0);
        saveUoMConversion(uomBox, uomPcs,   24.0);
        saveUoMConversion(uomLtr, uomMl,  1000.0);
        saveUoMConversion(uomMtr, uomCm,   100.0);
        saveUoMConversion(uomDay, uomHr,     8.0);
    }

    private UnitOfMeasure saveUoM(String code, String name, String symbol,
                                   UnitOfMeasure.UoMType type, boolean base, int decimals) {
        UnitOfMeasure u = new UnitOfMeasure(); u.setTenantId(T);
        u.setCode(code); u.setName(name); u.setSymbol(symbol);
        u.setUomType(type); u.setBaseUnit(base); u.setDecimalPlaces(decimals); u.setActive(true);
        return uomRepo.save(u);
    }

    private void saveUoMConversion(UnitOfMeasure from, UnitOfMeasure to, double factor) {
        UoMConversion c = new UoMConversion(); c.setTenantId(T);
        c.setFromUomId(from.getId()); c.setFromUomCode(from.getCode());
        c.setToUomId(to.getId()); c.setToUomCode(to.getCode());
        c.setConversionFactor(BigDecimal.valueOf(factor)); c.setBidirectional(true); c.setActive(true);
        uomConversionRepo.save(c);
        UoMConversion rev = new UoMConversion(); rev.setTenantId(T);
        rev.setFromUomId(to.getId()); rev.setFromUomCode(to.getCode());
        rev.setToUomId(from.getId()); rev.setToUomCode(from.getCode());
        rev.setConversionFactor(BigDecimal.ONE.divide(BigDecimal.valueOf(factor), 8, java.math.RoundingMode.HALF_UP));
        rev.setBidirectional(true); rev.setActive(true);
        uomConversionRepo.save(rev);
    }

    private void seedBrandsAndProductLines() {
        Brand bGlobal = saveBrand("GlobalMake", "GLB", "India",   "Domestic manufacturing brand");
        Brand bPremium= saveBrand("PremiumTech","PMT", "Germany", "Premium imported components");
        Brand bEco    = saveBrand("EcoLine",    "ECO", "India",   "Eco-friendly product range");

        saveProductLine("WD-SERIES", "Widget Series",        bGlobal,  "Industrial Equipment",   "B2B Manufacturing");
        saveProductLine("PC-SERIES", "Precision Components", bPremium, "Electronic Components",  "OEM Manufacturers");
        saveProductLine("ECO-PKG",   "Eco Packaging",        bEco,     "Packaging",              "FMCG Companies");
        saveProductLine("IT-INFRA",  "IT Infrastructure",    bGlobal,  "IT Equipment",           "Corporate Offices");
    }

    private Brand saveBrand(String name, String code, String country, String desc) {
        Brand b = new Brand(); b.setTenantId(T);
        b.setName(name); b.setCode(code); b.setCountry(country); b.setDescription(desc); b.setActive(true);
        return brandRepo.save(b);
    }

    private void saveProductLine(String code, String name, Brand brand, String cat, String target) {
        ProductLine pl = new ProductLine(); pl.setTenantId(T);
        pl.setCode(code); pl.setName(name);
        pl.setBrandId(brand.getId()); pl.setBrandName(brand.getName());
        pl.setCategory(cat); pl.setTargetMarket(target); pl.setActive(true);
        productLineRepo.save(pl);
    }

    private void seedOpeningBalances(Customer c1, Customer c2, Vendor v1, Vendor v2,
                                      Warehouse wh, Product p1, Product p2) {
        MigrationBatch mb = new MigrationBatch(); mb.setTenantId(T);
        mb.setBatchNumber("MB-2026-001");
        mb.setDescription("Opening Balances as of 01-Apr-2026 — FY2026-27 Start");
        mb.setAsOfDate(LocalDate.of(2026,4,1));
        mb.setStatus(MigrationBatch.BatchStatus.COMPLETED);
        mb.setTotalRecords(8); mb.setProcessedRecords(8); mb.setErrorRecords(0);
        mb.setCompletedAt(LocalDateTime.of(2026,4,1,9,0));
        migrationBatchRepo.save(mb);

        String ref = "MB-2026-001";
        saveOpeningBalance(ref, OpeningBalance.BalanceType.ACCOUNTS_RECEIVABLE, LocalDate.of(2026,4,1),
                "AR001","Accounts Receivable",c1.getId(),c1.getName(),"CUSTOMER",null,null,null,null,null,null,485000.0,0.0,"Opening AR balance");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.ACCOUNTS_RECEIVABLE, LocalDate.of(2026,4,1),
                "AR001","Accounts Receivable",c2.getId(),c2.getName(),"CUSTOMER",null,null,null,null,null,null,960000.0,0.0,"Opening AR balance");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.ACCOUNTS_PAYABLE, LocalDate.of(2026,4,1),
                "AP001","Accounts Payable",v1.getId(),v1.getName(),"VENDOR",null,null,null,null,null,null,0.0,320000.0,"Opening AP balance");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.ACCOUNTS_PAYABLE, LocalDate.of(2026,4,1),
                "AP001","Accounts Payable",v2.getId(),v2.getName(),"VENDOR",null,null,null,null,null,null,0.0,195000.0,"Opening AP balance");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.INVENTORY, LocalDate.of(2026,4,1),
                null,null,null,null,null,p1.getId(),p1.getName(),wh.getId(),wh.getName(),320.0,180.0,57600.0,0.0,"Opening inventory");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.INVENTORY, LocalDate.of(2026,4,1),
                null,null,null,null,null,p2.getId(),p2.getName(),wh.getId(),wh.getName(),150.0,350.0,52500.0,0.0,"Opening inventory");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.BANK, LocalDate.of(2026,4,1),
                "BANK001","HDFC Current Account",null,null,null,null,null,null,null,null,null,2500000.0,0.0,"Opening bank balance");
        saveOpeningBalance(ref, OpeningBalance.BalanceType.GENERAL_LEDGER, LocalDate.of(2026,4,1),
                "CAP001","Share Capital",null,null,null,null,null,null,null,null,null,0.0,5000000.0,"Paid-up capital");
    }

    private void saveOpeningBalance(String migRef, OpeningBalance.BalanceType type, LocalDate asOf,
                                     String accCode, String accName,
                                     UUID partyId, String partyName, String partyType,
                                     UUID productId, String productName,
                                     UUID warehouseId, String warehouseName,
                                     Double qty, Double unitCost,
                                     double debit, double credit, String notes) {
        OpeningBalance ob = new OpeningBalance(); ob.setTenantId(T);
        ob.setMigrationRef(migRef); ob.setBalanceType(type); ob.setAsOfDate(asOf);
        ob.setAccountCode(accCode); ob.setAccountName(accName);
        ob.setPartyId(partyId); ob.setPartyName(partyName); ob.setPartyType(partyType);
        ob.setProductId(productId); ob.setProductName(productName);
        ob.setWarehouseId(warehouseId); ob.setWarehouseName(warehouseName);
        if (qty != null)     ob.setQuantity(BigDecimal.valueOf(qty));
        if (unitCost != null) ob.setUnitCost(BigDecimal.valueOf(unitCost));
        ob.setDebitAmount(BigDecimal.valueOf(debit)); ob.setCreditAmount(BigDecimal.valueOf(credit));
        ob.setCurrency("INR"); ob.setStatus(OpeningBalance.MigrationStatus.POSTED);
        ob.setPostedAt(LocalDateTime.of(2026,4,1,9,0)); ob.setNotes(notes);
        openingBalanceRepo.save(ob);
    }

    private void seedSalesPlans(Product p1, Product p2, Product p3) {
        SalesPlan sp1 = new SalesPlan(); sp1.setTenantId(T);
        sp1.setPlanNumber("SP-2026-001"); sp1.setName("Annual Sales Plan FY 2026-27");
        sp1.setPlanType(SalesPlan.PlanType.ANNUAL); sp1.setPlanYear(2026);
        sp1.setStatus(SalesPlan.PlanStatus.ACTIVE);
        sp1.setTotalTargetRevenue(new BigDecimal("50000000"));
        sp1.setTotalActualRevenue(new BigDecimal("18750000"));
        sp1.setTerritory("Pan India"); sp1.setSalesRepName("National Sales Head");
        sp1.setApprovedBy("CEO"); sp1.setApprovedAt(LocalDateTime.of(2026,4,1,10,0));
        List<SalesPlanTarget> t1 = new ArrayList<>();
        t1.add(makeSalesPlanTarget(sp1, p1,10000.0,2500000.0,4200.0,1050000.0,"PCS"));
        t1.add(makeSalesPlanTarget(sp1, p2, 5000.0,2600000.0,1950.0, 975000.0,"PCS"));
        t1.add(makeSalesPlanTarget(sp1, p3,  500.0, 875000.0, 210.0, 367500.0,"PCS"));
        sp1.setTargets(t1);
        salesPlanRepo.save(sp1);

        SalesPlan sp2 = new SalesPlan(); sp2.setTenantId(T);
        sp2.setPlanNumber("SP-2026-002"); sp2.setName("Q2 2026 Sales Plan — South Region");
        sp2.setPlanType(SalesPlan.PlanType.QUARTERLY); sp2.setPlanYear(2026); sp2.setPlanQuarter(2);
        sp2.setStatus(SalesPlan.PlanStatus.APPROVED);
        sp2.setTotalTargetRevenue(new BigDecimal("8000000"));
        sp2.setTotalActualRevenue(BigDecimal.ZERO);
        sp2.setTerritory("South India"); sp2.setSalesRepName("Priya Nair");
        sp2.setApprovedBy("Sales Director"); sp2.setApprovedAt(LocalDateTime.of(2026,4,5,11,0));
        List<SalesPlanTarget> t2 = new ArrayList<>();
        t2.add(makeSalesPlanTarget(sp2, p1,2000.0,500000.0,0.0,0.0,"PCS"));
        t2.add(makeSalesPlanTarget(sp2, p2, 800.0,416000.0,0.0,0.0,"PCS"));
        sp2.setTargets(t2);
        salesPlanRepo.save(sp2);
    }

    private SalesPlanTarget makeSalesPlanTarget(SalesPlan plan, Product prod,
                                                 double targetQty, double targetRev,
                                                 double actualQty, double actualRev, String unit) {
        SalesPlanTarget t = new SalesPlanTarget(); t.setTenantId(T);
        t.setSalesPlan(plan); t.setProductId(prod.getId()); t.setProductName(prod.getName());
        t.setTargetQuantity(BigDecimal.valueOf(targetQty)); t.setTargetRevenue(BigDecimal.valueOf(targetRev));
        t.setActualQuantity(BigDecimal.valueOf(actualQty)); t.setActualRevenue(BigDecimal.valueOf(actualRev));
        t.setUnit(unit);
        return t;
    }

    private void seedDemandForecasts(Product p1, Product p2, Product p3, Product p4, Warehouse wh) {
        saveDemandForecast("DF-2026-001",p1,"2026-Q2",2026,null,2,DemandForecast.ForecastMethod.HISTORICAL,      3500.0,3420.0,"PCS",85,wh,DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-002",p2,"2026-Q2",2026,null,2,DemandForecast.ForecastMethod.HISTORICAL,      1400.0,1380.0,"PCS",82,wh,DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-003",p3,"2026-07", 2026,7,null,DemandForecast.ForecastMethod.MOVING_AVERAGE,  800.0,   0.0,"KG", 70,wh,DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-004",p4,"2026-07", 2026,7,null,DemandForecast.ForecastMethod.MANUAL,          600.0,   0.0,"PCS",60,wh,DemandForecast.ForecastStatus.DRAFT);
        saveDemandForecast("DF-2026-005",p1,"2026-Q3",2026,null,3,DemandForecast.ForecastMethod.EXPONENTIAL_SMOOTHING,3800.0,0.0,"PCS",75,wh,DemandForecast.ForecastStatus.DRAFT);
    }

    private void saveDemandForecast(String num, Product prod, String period, int year,
                                     Integer month, Integer quarter,
                                     DemandForecast.ForecastMethod method,
                                     double fQty, double aQty, String unit,
                                     int confidence, Warehouse wh, DemandForecast.ForecastStatus status) {
        DemandForecast df = new DemandForecast(); df.setTenantId(T);
        df.setForecastNumber(num); df.setProductId(prod.getId()); df.setProductName(prod.getName());
        df.setForecastPeriod(period); df.setForecastYear(year);
        df.setForecastMonth(month); df.setForecastQuarter(quarter);
        df.setForecastMethod(method); df.setForecastedQty(BigDecimal.valueOf(fQty));
        df.setActualQty(BigDecimal.valueOf(aQty));
        df.setVariance(BigDecimal.valueOf(aQty - fQty));
        df.setConfidenceLevel(confidence); df.setUnit(unit);
        df.setWarehouseId(wh.getId()); df.setStatus(status);
        demandForecastRepo.save(df);
    }

    private void seedSupplierPayments(Vendor v1, Vendor v2) {
        saveSupplierPayment("SPAY-2026-001", v1, "PINV-2026-001", LocalDate.of(2026,4,15),
                SupplierPayment.PaymentMethod.NEFT, 320000.0, 3200.0, 1.0,
                "HDFC Current Account","UTR2026041500123",SupplierPayment.PayStatus.RECONCILED,
                "Rajesh Kumar", LocalDateTime.of(2026,4,15,10,30));
        saveSupplierPayment("SPAY-2026-002", v2, "PINV-2026-002", LocalDate.of(2026,5,10),
                SupplierPayment.PaymentMethod.RTGS, 195000.0, 1950.0, 1.0,
                "HDFC Current Account","RTGS20260510ABC45",SupplierPayment.PayStatus.PROCESSED,
                "Finance Manager", LocalDateTime.of(2026,5,10,14,0));
        saveSupplierPayment("SPAY-2026-003", v1, "PINV-2026-003", LocalDate.of(2026,6,5),
                SupplierPayment.PaymentMethod.CHEQUE, 280000.0, 2800.0, 1.0,
                "HDFC Current Account","CHQ-004521",SupplierPayment.PayStatus.APPROVED,
                "Finance Manager", LocalDateTime.of(2026,6,5,11,0));
        saveSupplierPayment("SPAY-2026-004", v2, null, LocalDate.of(2026,6,20),
                SupplierPayment.PaymentMethod.BANK_TRANSFER, 150000.0, 0.0, 0.0,
                "HDFC Current Account", null, SupplierPayment.PayStatus.DRAFT, null, null);
    }

    private void saveSupplierPayment(String num, Vendor vendor, String invoiceNum, LocalDate payDate,
                                      SupplierPayment.PaymentMethod method, double amount, double tds, double tdsPct,
                                      String bankAcc, String refNum, SupplierPayment.PayStatus status,
                                      String approvedBy, LocalDateTime approvedAt) {
        SupplierPayment sp = new SupplierPayment(); sp.setTenantId(T);
        sp.setPaymentNumber(num); sp.setVendorId(vendor.getId()); sp.setVendorName(vendor.getName());
        sp.setInvoiceNumber(invoiceNum); sp.setPaymentDate(payDate); sp.setPaymentMethod(method);
        sp.setAmount(BigDecimal.valueOf(amount)); sp.setTdsAmount(BigDecimal.valueOf(tds));
        sp.setTdsPercent(BigDecimal.valueOf(tdsPct));
        sp.setNetPayment(BigDecimal.valueOf(amount - tds));
        sp.setCurrency("INR"); sp.setExchangeRate(BigDecimal.ONE);
        sp.setBankAccountName(bankAcc); sp.setReferenceNumber(refNum);
        sp.setStatus(status); sp.setApprovedBy(approvedBy); sp.setApprovedAt(approvedAt);
        if (status == SupplierPayment.PayStatus.PROCESSED || status == SupplierPayment.PayStatus.RECONCILED)
            sp.setProcessedAt(approvedAt != null ? approvedAt.plusHours(2) : null);
        supplierPaymentRepo.save(sp);
    }

    private void seedScrapEntries(Warehouse wh, Product p1, Product p2, Product p3) {
        saveScrapEntry("SCR-2026-001", ScrapEntry.ScrapType.QUALITY_REJECTION,
                wh, p1, "LOT-20260201-003", 45.0, "KG", 2475.0, 500.0,
                ScrapEntry.ScrapStatus.DISPOSED, "Quality Manager",
                "Steel rods rejected — surface corrosion detected in incoming inspection",
                ScrapDisposal.DisposalMethod.SOLD, "Metal Scrap Merchants Pvt Ltd", 1800.0, 150.0, "SCR-SALE-001");
        saveScrapEntry("SCR-2026-002", ScrapEntry.ScrapType.PRODUCTION_WASTE,
                wh, p2, null, 12.0, "PCS", 2160.0, 0.0,
                ScrapEntry.ScrapStatus.APPROVED, "Production Supervisor",
                "Defective widgets from machine calibration run — cannot be reworked",
                null, null, 0.0, 0.0, null);
        saveScrapEntry("SCR-2026-003", ScrapEntry.ScrapType.EXPIRED_STOCK,
                wh, p3, "LOT-20250301-007", 80.0, "PCS", 6800.0, 200.0,
                ScrapEntry.ScrapStatus.DISPOSED, "Warehouse Manager",
                "Electronic components — shelf life expired per manufacturer guidelines",
                ScrapDisposal.DisposalMethod.RECYCLED, "E-Waste Recyclers Ltd", 500.0, 200.0, "EWASTE-2026-045");
        saveScrapEntry("SCR-2026-004", ScrapEntry.ScrapType.DAMAGED_GOODS,
                wh, p1, "LOT-20260501-002", 8.0, "KG", 440.0, 0.0,
                ScrapEntry.ScrapStatus.DRAFT, null,
                "Steel rods damaged during warehouse forklift incident",
                null, null, 0.0, 0.0, null);
    }

    private void saveScrapEntry(String num, ScrapEntry.ScrapType type, Warehouse wh, Product prod,
                                 String lot, double qty, String unit, double scrapVal, double recoveryVal,
                                 ScrapEntry.ScrapStatus status, String approvedBy, String reason,
                                 ScrapDisposal.DisposalMethod dispMethod, String disposedTo,
                                 double dispAmount, double transCost, String dispRef) {
        ScrapEntry se = new ScrapEntry(); se.setTenantId(T);
        se.setScrapNumber(num); se.setScrapDate(LocalDate.now().minusDays(30));
        se.setScrapType(type); se.setWarehouseId(wh.getId()); se.setWarehouseName(wh.getName());
        se.setProductId(prod.getId()); se.setProductName(prod.getName());
        se.setLotNumber(lot); se.setQuantity(BigDecimal.valueOf(qty)); se.setUnit(unit);
        se.setScrapValue(BigDecimal.valueOf(scrapVal)); se.setRecoveryValue(BigDecimal.valueOf(recoveryVal));
        se.setReason(reason); se.setStatus(status); se.setApprovedBy(approvedBy);
        if (status == ScrapEntry.ScrapStatus.DISPOSED) se.setDisposedAt(LocalDateTime.now().minusDays(15));
        se = scrapEntryRepo.save(se);

        if (dispMethod != null) {
            ScrapDisposal sd = new ScrapDisposal(); sd.setTenantId(T);
            sd.setScrapEntry(se); sd.setDisposalDate(LocalDate.now().minusDays(15));
            sd.setDisposalMethod(dispMethod); sd.setDisposedTo(disposedTo);
            sd.setDisposalAmount(BigDecimal.valueOf(dispAmount));
            sd.setTransportCost(BigDecimal.valueOf(transCost));
            sd.setNetRecovery(BigDecimal.valueOf(dispAmount - transCost));
            sd.setReferenceNumber(dispRef);
            scrapDisposalRepo.save(sd);
        }
    }

    private void seedApprovalRules() {
        saveApprovalRule("PO Approval — High Value",  "PURCHASE_ORDER","amount",
                ApprovalRule.Operator.GREATER_THAN,"100000",null,
                ApprovalRule.ApproverType.DEPARTMENT_HEAD,"Purchase Head",1,3,"CEO");
        saveApprovalRule("PO Approval — Very High Value","PURCHASE_ORDER","amount",
                ApprovalRule.Operator.GREATER_THAN,"500000",null,
                ApprovalRule.ApproverType.CEO,"CEO",2,2,"Board");
        saveApprovalRule("Invoice Approval","INVOICE","amount",
                ApprovalRule.Operator.GREATER_THAN,"50000",null,
                ApprovalRule.ApproverType.MANAGER,"Finance Manager",1,5,"CFO");
        saveApprovalRule("Leave Approval","LEAVE","days",
                ApprovalRule.Operator.GREATER_THAN,"3",null,
                ApprovalRule.ApproverType.DEPARTMENT_HEAD,"HR Head",1,1,"HR Director");
        saveApprovalRule("Payment Approval","PAYMENT","amount",
                ApprovalRule.Operator.GREATER_THAN,"200000",null,
                ApprovalRule.ApproverType.CEO,"CEO",1,2,"Board");
        saveApprovalRule("Expense Approval — Mid Range","EXPENSE","amount",
                ApprovalRule.Operator.BETWEEN,"5000","50000",
                ApprovalRule.ApproverType.MANAGER,"Department Manager",1,3,"Finance Head");
    }

    private void saveApprovalRule(String name, String docType, String condField,
                                   ApprovalRule.Operator op, String val1, String val2,
                                   ApprovalRule.ApproverType approverType, String approverVal,
                                   int level, int slaDays, String escalateTo) {
        ApprovalRule ar = new ApprovalRule(); ar.setTenantId(T);
        ar.setRuleName(name); ar.setDocumentType(docType);
        ar.setConditionField(condField); ar.setConditionOperator(op);
        ar.setConditionValue(val1); ar.setConditionValue2(val2);
        ar.setApproverType(approverType); ar.setApproverValue(approverVal);
        ar.setApprovalLevel(level); ar.setSlaDays(slaDays); ar.setEscalateTo(escalateTo);
        ar.setActive(true);
        approvalRuleRepo.save(ar);
    }

    private void seedReportDefinitions() {
        saveReportDefinition("RPT-2026-001","Sales Summary by Customer",
                ReportDefinition.ReportCategory.SALES,"sales_orders",
                "[{\"field\":\"customerName\",\"label\":\"Customer\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"totalAmount\",\"label\":\"Total\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"status\",\"label\":\"Status\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[{\"field\":\"orderDate\",\"operator\":\"BETWEEN\",\"value\":\"2026-04-01\"}]",
                "customerName","ASC","customerName",true,"admin");
        saveReportDefinition("RPT-2026-002","Purchase Order Aging Report",
                ReportDefinition.ReportCategory.PURCHASE,"purchase_orders",
                "[{\"field\":\"vendorName\",\"label\":\"Vendor\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"poNumber\",\"label\":\"PO Number\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"totalAmount\",\"label\":\"Amount\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"status\",\"label\":\"Status\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[{\"field\":\"status\",\"operator\":\"NOT_EQUALS\",\"value\":\"RECEIVED\"}]",
                "createdAt","ASC",null,true,"admin");
        saveReportDefinition("RPT-2026-003","Inventory Stock Valuation",
                ReportDefinition.ReportCategory.INVENTORY,"stock_items",
                "[{\"field\":\"productName\",\"label\":\"Product\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"warehouseId\",\"label\":\"Warehouse\",\"dataType\":\"UUID\",\"visible\":true},{\"field\":\"quantityOnHand\",\"label\":\"Qty\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"averageCost\",\"label\":\"Avg Cost\",\"dataType\":\"DECIMAL\",\"visible\":true}]",
                "[]","productName","ASC",null,true,"admin");
        saveReportDefinition("RPT-2026-004","Employee Leave Balance Summary",
                ReportDefinition.ReportCategory.HR,"employees",
                "[{\"field\":\"fullName\",\"label\":\"Employee\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"department\",\"label\":\"Dept\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[]","fullName","ASC","department",false,"hr.manager");
        saveReportDefinition("RPT-2026-005","Monthly P&L Summary",
                ReportDefinition.ReportCategory.FINANCIAL,"journal_entries",
                "[{\"field\":\"accountCode\",\"label\":\"Account\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"debitAmount\",\"label\":\"Debit\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"creditAmount\",\"label\":\"Credit\",\"dataType\":\"DECIMAL\",\"visible\":true}]",
                "[{\"field\":\"entryDate\",\"operator\":\"BETWEEN\",\"value\":\"2026-04-01\"}]",
                "accountCode","ASC","accountCode",true,"admin");
    }

    private void saveReportDefinition(String code, String name,
                                       ReportDefinition.ReportCategory cat, String baseEntity,
                                       String columns, String filters,
                                       String sortField, String sortDir, String groupBy,
                                       boolean isPublic, String createdBy) {
        ReportDefinition rd = new ReportDefinition(); rd.setTenantId(T);
        rd.setReportCode(code); rd.setName(name); rd.setReportCategory(cat);
        rd.setBaseEntity(baseEntity); rd.setColumns(columns); rd.setDefaultFilters(filters);
        rd.setDefaultSortField(sortField); rd.setDefaultSortDirection(sortDir);
        rd.setGroupByField(groupBy); rd.setDefaultPageSize(20);
        rd.setPublic(isPublic); rd.setCreatedBy(createdBy); rd.setActive(true); rd.setRunCount(0);
        reportDefinitionRepo.save(rd);
    }
}
