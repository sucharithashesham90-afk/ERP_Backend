package com.erp.platform.config;

import com.erp.platform.modules.accounting.entity.*;
import com.erp.platform.modules.accounting.repository.*;
import com.erp.platform.modules.crm.entity.*;
import com.erp.platform.modules.crm.repository.*;
import com.erp.platform.modules.hr.entity.*;
import com.erp.platform.modules.hr.repository.*;
import com.erp.platform.modules.inventory.entity.*;
import com.erp.platform.modules.inventory.repository.*;
import com.erp.platform.modules.manufacturing.entity.*;
import com.erp.platform.modules.manufacturing.repository.*;
import com.erp.platform.modules.master.entity.*;
import com.erp.platform.modules.master.repository.*;
import com.erp.platform.modules.organization.entity.*;
import com.erp.platform.modules.organization.repository.*;
import com.erp.platform.modules.payroll.entity.*;
import com.erp.platform.modules.payroll.repository.*;
import com.erp.platform.modules.pricing.entity.*;
import com.erp.platform.modules.pricing.repository.*;
import com.erp.platform.modules.purchase.entity.*;
import com.erp.platform.modules.purchase.repository.*;
import com.erp.platform.modules.quality.entity.*;
import com.erp.platform.modules.quality.repository.*;
import com.erp.platform.modules.sales.entity.*;
import com.erp.platform.modules.sales.repository.*;
import com.erp.platform.modules.dispatch.entity.*;
import com.erp.platform.modules.dispatch.repository.*;
import com.erp.platform.modules.promotions.entity.*;
import com.erp.platform.modules.promotions.repository.*;
import com.erp.platform.modules.reports.entity.*;
import com.erp.platform.modules.reports.repository.*;
import com.erp.platform.modules.supplier.entity.*;
import com.erp.platform.modules.supplier.repository.*;
import com.erp.platform.modules.workflow.entity.*;
import com.erp.platform.modules.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Demo data for local work.
 *
 * <p>Gated. This used to be an unguarded CommandLineRunner, so it ran wherever the app started —
 * including production — and its only check was "skip if any vendor exists". On a fresh database
 * that means it invents a company's whole history: leads called Sunrise Industries and Metro Tech
 * Solutions, customers, vendors, orders, invoices, payslips, 44 kinds of record in all. That is
 * what the Sales Tracker was showing, and it is indistinguishable from real data once it is in.
 *
 * <p>Now off unless app.seed.test-data is explicitly true. Development turns it on; the Railway
 * profile leaves it off. Nothing here deletes anything — a database already carrying this data
 * keeps it until someone decides to clear it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
@ConditionalOnProperty(name = "app.seed.test-data", havingValue = "true")
public class TestDataSeeder implements CommandLineRunner {

    static final UUID T = DataInitializer.DEFAULT_TENANT_ID;

    // ── Organization ──────────────────────────────────────────────────────────
    private final CompanyRepository       companyRepo;
    private final BranchRepository        branchRepo;
    private final DepartmentRepository    deptRepo;

    // ── Master ────────────────────────────────────────────────────────────────
    private final CustomerRepository      customerRepo;
    private final VendorRepository        vendorRepo;
    private final ProductRepository       productRepo;
    private final ProductCategoryRepository categoryRepo;
    private final TaxRepository           taxRepo;
    private final ProductSpecificationRepository productSpecRepo;

    // ── CRM ───────────────────────────────────────────────────────────────────
    private final LeadRepository          leadRepo;
    private final ContactRepository       contactRepo;

    // ── Sales ─────────────────────────────────────────────────────────────────
    private final QuotationRepository     quotationRepo;
    private final SalesOrderRepository    salesOrderRepo;
    private final InvoiceRepository       invoiceRepo;
    private final DeliveryNoteRepository  deliveryNoteRepo;
    private final SalesReturnRepository   salesReturnRepo;
    private final CustomerAdvanceRepository customerAdvanceRepo;
    private final SalesTerritoryRepository  salesTerritoryRepo;
    private final SalesRepresentativeRepository salesRepRepo;
    private final SalesTargetRepository   salesTargetRepo;

    // ── Purchase ──────────────────────────────────────────────────────────────
    private final PurchaseOrderRepository purchaseOrderRepo;
    private final GoodsReceiptRepository  goodsReceiptRepo;
    private final PurchaseReturnRepository purchaseReturnRepo;
    private final SupplierAdvanceRepository supplierAdvanceRepo;
    private final ServiceCategoryRepository serviceCategoryRepo;
    private final ServiceDefinitionRepository serviceDefinitionRepo;
    private final ServiceOrderRepository  serviceOrderRepo;
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;

    // ── Inventory ─────────────────────────────────────────────────────────────
    private final WarehouseRepository     warehouseRepo;
    private final StockItemRepository     stockItemRepo;
    private final StockTransferRepository stockTransferRepo;
    private final PhysicalCountRepository physicalCountRepo;

    // ── Manufacturing ─────────────────────────────────────────────────────────
    private final BOMRepository              bomRepo;
    private final WorkOrderRepository        workOrderRepo;
    private final OperationTypeRepository    operationTypeRepo;
    private final ProcessStepRepository      processStepRepo;
    private final ProcessTemplateRepository  processTemplateRepo;
    private final ProcessTemplateStepRepository processTemplateStepRepo;
    private final BatchHistoryRepository     batchHistoryRepo;
    private final QualityRejectionRepository qualityRejectionRepo;

    // ── Quality ───────────────────────────────────────────────────────────────
    private final QualityInspectionRepository qualityInspectionRepo;

    // ── Accounting ────────────────────────────────────────────────────────────
    private final BankAccountRepository         bankAccountRepo;
    private final BankReconciliationRepository  bankReconRepo;
    private final CostCenterRepository          costCenterRepo;
    private final AssetGroupRepository          assetGroupRepo;
    private final FixedAssetRepository          fixedAssetRepo;
    private final PaymentTermRepository         paymentTermRepo;
    private final EarlyPaymentDiscountRepository earlyPayDiscountRepo;
    private final JournalEntryRepository        journalEntryRepo;
    private final AccountingPeriodRepository    accountingPeriodRepo;

    // ── HR ────────────────────────────────────────────────────────────────────
    private final LeaveTypeRepository       leaveTypeRepo;
    private final EmployeeRepository        employeeRepo;
    private final LeaveApplicationRepository leaveAppRepo;
    private final AttendanceRepository      attendanceRepo;

    // ── Payroll ───────────────────────────────────────────────────────────────
    private final SalaryComponentRepository salaryComponentRepo;
    private final PayslipRepository         payslipRepo;

    // ── Pricing ───────────────────────────────────────────────────────────────
    private final PriceListRepository       priceListRepo;
    private final PriceListItemRepository   priceListItemRepo;
    private final DiscountSchemeRepository  discountSchemeRepo;
    private final DiscountSlabRepository    discountSlabRepo;

    // ── Workflow ──────────────────────────────────────────────────────────────
    private final WorkflowDefinitionRepository  workflowDefRepo;
    private final WorkflowInstanceRepository    workflowInstanceRepo;

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
    private final SalesPlanTargetRepository     salesPlanTargetRepo;
    private final DemandForecastRepository      demandForecastRepo;

    // ── Phase 3: Supplier Payments ────────────────────────────────────────────
    private final SupplierPaymentRepository     supplierPaymentRepo;

    // ── Phase 3: Scrap ────────────────────────────────────────────────────────
    private final ScrapEntryRepository          scrapEntryRepo;

    // ── Phase 3: Approval Workflow ────────────────────────────────────────────
    private final ApprovalRuleRepository        approvalRuleRepo;

    // ── Phase 3: Reports ──────────────────────────────────────────────────────
    private final ReportDefinitionRepository    reportDefinitionRepo;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void run(String... args) {
        if (vendorRepo.count() > 0) {
            log.info("Test data already present — skipping TestDataSeeder");
            return;
        }
        log.info("Seeding test data for all modules...");

        // Foundation (these already exist from DataInitializer but we read them)
        List<Tax>             taxes      = taxRepo.findAll();
        List<ProductCategory> categories = categoryRepo.findAll();

        Tax   tax18 = taxes.stream().filter(t -> t.getName().equals("GST 18%")).findFirst().orElse(taxes.get(0));
        Tax   tax12 = taxes.stream().filter(t -> t.getName().equals("GST 12%")).findFirst().orElse(taxes.get(0));
        Tax   tax5  = taxes.stream().filter(t -> t.getName().equals("GST 5%")).findFirst().orElse(taxes.get(0));
        ProductCategory catRaw  = categories.stream().filter(c -> c.getName().equals("Raw Materials")).findFirst().orElse(categories.get(0));
        ProductCategory catGoods = categories.stream().filter(c -> c.getName().equals("Finished Goods")).findFirst().orElse(categories.get(0));
        ProductCategory catSvc   = categories.stream().filter(c -> c.getName().equals("Services")).findFirst().orElse(categories.get(0));
        ProductCategory catConsumable = categories.stream().filter(c -> c.getName().equals("Consumables")).findFirst().orElse(categories.get(0));
        ProductCategory catIT   = categories.stream().filter(c -> c.getName().equals("IT Equipment")).findFirst().orElse(categories.get(0));

        // ── Organization ─────────────────────────────────────────────────────
        Branch hoBranch  = saveBranch("Head Office",  "HO",  "Hyderabad", "Telangana", true);
        Branch mumBranch = saveBranch("Mumbai Branch", "MUM", "Mumbai",    "Maharashtra", false);
        Branch delBranch = saveBranch("Delhi Branch",  "DEL", "New Delhi", "Delhi",       false);

        Department salesDept = saveDept("Sales & Marketing", "SALES");
        Department purchDept = saveDept("Purchase",          "PURCH");
        Department finDept   = saveDept("Finance",           "FIN");
        Department hrDept    = saveDept("Human Resources",   "HR");
        Department mfgDept   = saveDept("Manufacturing",     "MFG");
        Department itDept    = saveDept("Information Technology", "IT");

        // ── Vendors ──────────────────────────────────────────────────────────
        Vendor vendorABC  = saveVendor("ABC Suppliers Pvt Ltd",   "VND001", "purchase@abc-suppliers.com",   "9800001111", "SUPPLIER");
        Vendor vendorXYZ  = saveVendor("XYZ Raw Materials Co",    "VND002", "orders@xyzrawmat.com",         "9800002222", "SUPPLIER");
        Vendor vendorTech = saveVendor("Tech Services India LLP",  "VND003", "service@techservicesindia.com","9800003333", "SERVICE_PROVIDER");
        Vendor vendorLog  = saveVendor("Global Logistics Ltd",    "VND004", "ops@globallogistics.in",       "9800004444", "TRANSPORT");
        Vendor vendorPrem = saveVendor("Premium Components Inc",  "VND005", "sales@premiumcomp.com",        "9800005555", "SUPPLIER");

        // ── Customers ─────────────────────────────────────────────────────────
        Customer custMega  = saveCustomer("Mega Retail Chain Pvt Ltd",   "CUST001", "orders@megaretail.com",   "9900001111", "RETAIL",     500000.0);
        Customer custEnt   = saveCustomer("Enterprise Solutions Ltd",    "CUST002", "purchase@enterprise.com", "9900002222", "WHOLESALE",  1000000.0);
        Customer custQuick = saveCustomer("Quick Mart Stores",           "CUST003", "buying@quickmart.in",     "9900003333", "RETAIL",     200000.0);
        Customer custDist  = saveCustomer("Distribution Hub Pvt Ltd",   "CUST004", "dist@distrhub.com",       "9900004444", "DISTRIBUTOR",750000.0);
        Customer custCorp  = saveCustomer("Direct Corporate Ltd",        "CUST005", "accounts@directcorp.com", "9900005555", "DIRECT",     300000.0);

        // ── Products ──────────────────────────────────────────────────────────
        Product pSteelRods  = saveProduct("Steel Rods 12mm",       "PRD001", catRaw,      tax18, "KG",  55.0,    60.0,   70.0,  Product.ProductType.RAW_MATERIAL);
        Product pPlastic    = saveProduct("Plastic Pellets HDPE",   "PRD002", catRaw,      tax12, "KG",  95.0,   110.0,  125.0,  Product.ProductType.RAW_MATERIAL);
        Product pWidgetA    = saveProduct("Finished Widget Model A", "PRD003", catGoods,   tax18, "PCS", 180.0,  250.0,  299.0,  Product.ProductType.GOODS);
        Product pWidgetB    = saveProduct("Premium Widget Model B",  "PRD004", catGoods,   tax18, "PCS", 350.0,  520.0,  599.0,  Product.ProductType.GOODS);
        Product pPacking    = saveProduct("Packaging Material",      "PRD005", catConsumable,tax5, "BOX", 25.0,   35.0,   40.0,  Product.ProductType.CONSUMABLE);
        Product pLubricant  = saveProduct("Lubricant Oil 5L",        "PRD006", catConsumable,tax18,"CAN", 420.0,  550.0,  620.0,  Product.ProductType.CONSUMABLE);
        Product pMachinePart= saveProduct("Machine Part X100",       "PRD007", catGoods,   tax18, "PCS", 1200.0,1750.0, 1999.0,  Product.ProductType.GOODS);
        Product pElecComp   = saveProduct("Electronic Component PCB","PRD008", catGoods,   tax18, "PCS", 85.0,  120.0,  145.0,  Product.ProductType.GOODS);
        Product pMaintSvc   = saveProduct("Annual Maintenance Service","PRD009",catSvc,    tax18, "HOURS",0.0,  1500.0, 1500.0,  Product.ProductType.SERVICE);
        Product pConsultSvc = saveProduct("IT Consulting Service",   "PRD010", catSvc,     tax18, "HOURS",0.0,  2000.0, 2000.0,  Product.ProductType.SERVICE);
        Product pLaptop     = saveProduct("Business Laptop Pro 15",  "PRD011", catIT,      tax18, "PCS",45000.0,60000.0,65000.0, Product.ProductType.ASSET);
        Product pComputer   = saveProduct("Office Desktop Computer", "PRD012", catIT,      tax18, "PCS",30000.0,42000.0,45000.0, Product.ProductType.ASSET);

        // Product Specifications
        saveProductSpec(pWidgetA, "DIMENSION", "Length",    "mm",  "120", null, null);
        saveProductSpec(pWidgetA, "DIMENSION", "Width",     "mm",   "60", null, null);
        saveProductSpec(pWidgetA, "WEIGHT",    "Net Weight","gm",  "250", null, null);
        saveProductSpec(pSteelRods,"MATERIAL",  "Grade",    "",  "IS:2062", null, null);
        saveProductSpec(pLaptop,   "ELECTRICAL","RAM",      "GB",   "16", null, null);
        saveProductSpec(pLaptop,   "ELECTRICAL","Storage",  "GB",  "512", null, null);

        // ── Warehouses ───────────────────────────────────────────────────────
        Warehouse whMain    = saveWarehouse("Main Warehouse",    "WH001", "Hyderabad", true);
        Warehouse whMumbai  = saveWarehouse("Regional Hub Mumbai","WH002","Mumbai",    false);
        Warehouse whDelhi   = saveWarehouse("Transit Store Delhi","WH003","New Delhi", false);

        // ── Stock Items ───────────────────────────────────────────────────────
        saveStockItem(whMain, pSteelRods,   new BigDecimal("1500"), new BigDecimal("55.00"));
        saveStockItem(whMain, pPlastic,     new BigDecimal("800"),  new BigDecimal("95.00"));
        saveStockItem(whMain, pWidgetA,     new BigDecimal("320"),  new BigDecimal("180.00"));
        saveStockItem(whMain, pWidgetB,     new BigDecimal("150"),  new BigDecimal("350.00"));
        saveStockItem(whMain, pPacking,     new BigDecimal("500"),  new BigDecimal("25.00"));
        saveStockItem(whMain, pMachinePart, new BigDecimal("75"),   new BigDecimal("1200.00"));
        saveStockItem(whMain, pElecComp,    new BigDecimal("400"),  new BigDecimal("85.00"));
        saveStockItem(whMain, pLaptop,      new BigDecimal("12"),   new BigDecimal("45000.00"));
        saveStockItem(whMumbai, pWidgetA,   new BigDecimal("80"),   new BigDecimal("180.00"));
        saveStockItem(whMumbai, pWidgetB,   new BigDecimal("40"),   new BigDecimal("350.00"));

        // ── CRM ───────────────────────────────────────────────────────────────
        saveLead("Sunrise Industries",      "Ankit Sharma",    "ankit@sunrise.com",     "9871001001", "REFERRAL",    "NEW",       "HIGH",   500000.0);
        saveLead("Metro Tech Solutions",    "Priya Nair",      "priya@metrotech.com",   "9871002002", "WEBSITE",     "QUALIFIED", "MEDIUM", 250000.0);
        saveLead("Pinnacle Manufacturing",  "Ravi Kumar",      "ravi@pinnacle.com",     "9871003003", "COLD_CALL",   "PROPOSAL",  "HIGH",   750000.0);
        saveLead("Star Distributors",       "Neha Singh",      "neha@stardistr.com",    "9871004004", "SOCIAL_MEDIA","CONTACTED", "LOW",    180000.0);
        saveLead("Horizon Retail Group",    "Sanjay Patel",    "sanjay@horizonretail.in","9871005005","EMAIL",       "NEGOTIATION","HIGH",  900000.0);

        saveContact("Rajesh",  "Mehta",    "rajesh.mehta@megaretail.com",   "9872001001", "Purchase Manager",   custMega.getId());
        saveContact("Sunita",  "Verma",    "sunita.verma@enterprise.com",   "9872002002", "Director",           custEnt.getId());
        saveContact("Arun",    "Joshi",    "arun.joshi@quickmart.in",       "9872003003", "Buying Head",        custQuick.getId());
        saveContact("Kavita",  "Rao",      "kavita.rao@distrhub.com",       "9872004004", "Supply Chain Head",  custDist.getId());
        saveContact("Deepak",  "Sharma",   "deepak.sharma@directcorp.com",  "9872005005", "CFO",                custCorp.getId());

        // ── Sales Territories & Reps ──────────────────────────────────────────
        SalesTerritory terrSouth  = saveTerritory("South India",  "TER-S",  "South",  "Telangana, Karnataka, Tamil Nadu, Kerala");
        SalesTerritory terrWest   = saveTerritory("West India",   "TER-W",  "West",   "Maharashtra, Gujarat, Rajasthan");
        SalesTerritory terrNorth  = saveTerritory("North India",  "TER-N",  "North",  "Delhi, UP, Punjab, Haryana");

        SalesRepresentative repRam   = saveSalesRep("Ramesh Gupta",   "REP001", "ramesh.gupta@erp.com",  "9873001001", terrSouth, 1200000.0, 5.0);
        SalesRepresentative repDeep  = saveSalesRep("Deepa Krishnan", "REP002", "deepa.k@erp.com",       "9873002002", terrWest,  1500000.0, 5.5);
        SalesRepresentative repVikas = saveSalesRep("Vikas Tomar",    "REP003", "vikas.tomar@erp.com",   "9873003003", terrNorth, 1000000.0, 4.5);

        saveSalesTarget("Q1 FY26 South", "QUARTERLY", terrSouth, repRam,   LocalDate.of(2026,4,1), LocalDate.of(2026,6,30), 1200000.0, 0.0);
        saveSalesTarget("Q1 FY26 West",  "QUARTERLY", terrWest,  repDeep,  LocalDate.of(2026,4,1), LocalDate.of(2026,6,30), 1500000.0, 0.0);

        // ── Quotations ────────────────────────────────────────────────────────
        Quotation quot1 = saveQuotation("QT-2026-001", custMega, LocalDate.of(2026,4,1), LocalDate.of(2026,4,30),
                List.of(qi(pWidgetA, 100, 250.0, 5.0, 18.0), qi(pWidgetB, 50, 520.0, 3.0, 18.0)));
        Quotation quot2 = saveQuotation("QT-2026-002", custEnt,  LocalDate.of(2026,4,5), LocalDate.of(2026,5,5),
                List.of(qi(pMachinePart, 20, 1750.0, 0.0, 18.0), qi(pElecComp, 200, 120.0, 2.0, 18.0)));
        Quotation quot3 = saveQuotation("QT-2026-003", custDist, LocalDate.of(2026,4,10),LocalDate.of(2026,5,10),
                List.of(qi(pWidgetA, 200, 230.0, 8.0, 18.0)));

        // ── Sales Orders ──────────────────────────────────────────────────────
        SalesOrder so1 = saveSalesOrder("SO-2026-001", custMega, LocalDate.of(2026,4,5), LocalDate.of(2026,4,20),
                List.of(qi(pWidgetA, 100, 250.0, 5.0, 18.0), qi(pWidgetB, 50, 520.0, 3.0, 18.0)), "CONFIRMED");
        SalesOrder so2 = saveSalesOrder("SO-2026-002", custEnt,  LocalDate.of(2026,4,8), LocalDate.of(2026,4,25),
                List.of(qi(pMachinePart, 20, 1750.0, 0.0, 18.0), qi(pElecComp, 200, 120.0, 2.0, 18.0)), "PROCESSING");
        SalesOrder so3 = saveSalesOrder("SO-2026-003", custQuick,LocalDate.of(2026,4,12),LocalDate.of(2026,4,28),
                List.of(qi(pWidgetA, 50, 250.0, 0.0, 18.0), qi(pPacking, 100, 35.0, 0.0, 5.0)), "DELIVERED");

        // ── Invoices ─────────────────────────────────────────────────────────
        Invoice inv1 = saveInvoice("INV-2026-001", custMega, so1, LocalDate.of(2026,4,20), LocalDate.of(2026,5,20),
                List.of(qi(pWidgetA, 100, 250.0, 5.0, 18.0), qi(pWidgetB, 50, 520.0, 3.0, 18.0)),
                "PAID", 58025.0, 58025.0);
        Invoice inv2 = saveInvoice("INV-2026-002", custEnt,  so2, LocalDate.of(2026,4,25), LocalDate.of(2026,5,25),
                List.of(qi(pMachinePart, 20, 1750.0, 0.0, 18.0), qi(pElecComp, 200, 120.0, 2.0, 18.0)),
                "PARTIALLY_PAID", 70576.0, 35000.0);
        Invoice inv3 = saveInvoice("INV-2026-003", custQuick,so3, LocalDate.of(2026,4,28), LocalDate.of(2026,5,28),
                List.of(qi(pWidgetA, 50, 250.0, 0.0, 18.0), qi(pPacking, 100, 35.0, 0.0, 5.0)),
                "SENT", 18388.0, 0.0);

        // ── Delivery Notes ────────────────────────────────────────────────────
        saveDeliveryNote("DN-2026-001", so1.getId(), custMega.getName(), LocalDate.of(2026,4,19), "COMPLETED", "Mega Retail Warehouse, Hyderabad");
        saveDeliveryNote("DN-2026-002", so3.getId(), custQuick.getName(),LocalDate.of(2026,4,27), "COMPLETED", "Quick Mart DC, Bangalore");

        // ── Sales Returns ─────────────────────────────────────────────────────
        saveSalesReturn("SR-2026-001", inv1.getId(), custMega.getName(), LocalDate.of(2026,5,2), "Defective units found in batch", 5900.0);

        // ── Customer Advances ─────────────────────────────────────────────────
        saveCustomerAdvance("CA-2026-001", custEnt,  LocalDate.of(2026,4,3), 50000.0, "BANK_TRANSFER", "NEFT20260403ENT");
        saveCustomerAdvance("CA-2026-002", custDist, LocalDate.of(2026,4,7), 25000.0, "CHEQUE",        "CHQ-0045821");

        // ── Purchase Orders ───────────────────────────────────────────────────
        PurchaseOrder po1 = savePurchaseOrder("PO-2026-001", vendorABC, LocalDate.of(2026,4,2), LocalDate.of(2026,4,15),
                List.of(pi(pSteelRods, 2000, 55.0, 0.0, 18.0), pi(pPlastic, 1000, 95.0, 0.0, 12.0)), "RECEIVED");
        PurchaseOrder po2 = savePurchaseOrder("PO-2026-002", vendorPrem, LocalDate.of(2026,4,8), LocalDate.of(2026,4,22),
                List.of(pi(pMachinePart, 100, 1200.0, 2.0, 18.0), pi(pElecComp, 500, 85.0, 1.0, 18.0)), "CONFIRMED");
        PurchaseOrder po3 = savePurchaseOrder("PO-2026-003", vendorXYZ, LocalDate.of(2026,4,14), LocalDate.of(2026,4,28),
                List.of(pi(pSteelRods, 1000, 54.0, 0.0, 18.0)), "DRAFT");

        // ── Goods Receipts ────────────────────────────────────────────────────
        GoodsReceipt grn1 = saveGRN("GRN-2026-001", po1, vendorABC, LocalDate.of(2026,4,14),
                List.of(gri(pSteelRods, 2000, 2000, 1980, 20), gri(pPlastic, 1000, 1000, 1000, 0)));
        GoodsReceipt grn2 = saveGRN("GRN-2026-002", po2, vendorPrem, LocalDate.of(2026,4,21),
                List.of(gri(pMachinePart, 100, 100, 100, 0), gri(pElecComp, 500, 480, 480, 0)));

        // ── Purchase Returns ──────────────────────────────────────────────────
        savePurchaseReturn("PR-2026-001", grn1.getId(), vendorABC.getName(), LocalDate.of(2026,4,17),
                "Quality rejected — steel rods below grade", 1180.0);

        // ── Supplier Advances ─────────────────────────────────────────────────
        saveSupplierAdvance(vendorABC,  LocalDate.of(2026,4,1), 100000.0, "BANK_TRANSFER", "NEFT20260401ABC");
        saveSupplierAdvance(vendorPrem, LocalDate.of(2026,4,7), 60000.0,  "CHEQUE",        "CHQ-0099213");

        // ── Service Categories & Definitions ─────────────────────────────────
        ServiceCategory scIT    = saveServiceCategory("IT Services",         "SVC-IT");
        ServiceCategory scMaint = saveServiceCategory("Maintenance Services", "SVC-MAINT");
        ServiceCategory scTrans = saveServiceCategory("Transport Services",   "SVC-TRANS");

        ServiceDefinition sdAMC       = saveServiceDef("Annual Maintenance Contract", "SVC001", scMaint, "MONTHS", 5000.0, tax18);
        ServiceDefinition sdSoftDev   = saveServiceDef("Software Development",        "SVC002", scIT,    "HOURS",  1800.0, tax18);
        ServiceDefinition sdTransport = saveServiceDef("Goods Transportation",        "SVC003", scTrans, "KM",     12.0,   tax18);

        // ── Service Orders ────────────────────────────────────────────────────
        ServiceOrder svo1 = saveServiceOrder("SVCO-2026-001", vendorTech, LocalDate.of(2026,4,5), LocalDate.of(2026,4,30),
                "Monthly server maintenance and support", List.of(svoi(sdAMC, 12, 5000.0, 18.0)));
        ServiceOrder svo2 = saveServiceOrder("SVCO-2026-002", vendorLog,  LocalDate.of(2026,4,10), LocalDate.of(2026,4,11),
                "Inter-city shipment — Hyderabad to Mumbai", List.of(svoi(sdTransport, 1500, 12.0, 18.0)));

        // ── Purchase Invoices ─────────────────────────────────────────────────
        savePurchaseInvoice("PI-2026-001", vendorABC,  "VINV-ABC-4521", po1, grn1,
                LocalDate.of(2026,4,15), LocalDate.of(2026,5,15), 244100.0, 0.0);
        savePurchaseInvoice("PI-2026-002", vendorPrem, "VINV-PREM-8812", po2, grn2,
                LocalDate.of(2026,4,22), LocalDate.of(2026,5,22), 157644.0, 80000.0);

        // ── Stock Transfers ───────────────────────────────────────────────────
        saveStockTransfer("ST-2026-001", whMain, whMumbai, LocalDate.of(2026,4,18),
                List.of(new Object[]{pWidgetA, 80.0}, new Object[]{pWidgetB, 40.0}));
        saveStockTransfer("ST-2026-002", whMain, whDelhi, LocalDate.of(2026,4,22),
                List.<Object[]>of(new Object[]{pMachinePart, 20.0}));

        // ── Physical Counts ───────────────────────────────────────────────────
        savePhysicalCount("PC-2026-001", whMain,   LocalDate.of(2026,4,30), "COMPLETED");
        savePhysicalCount("PC-2026-002", whMumbai, LocalDate.of(2026,5,2),  "DRAFT");

        // ── BOM (production module) ───────────────────────────────────────────
        BillOfMaterials bom1 = saveBOM("Widget A BOM",   pWidgetA, "1.0",
                List.of(new Object[]{pSteelRods, 2.5, "KG"}, new Object[]{pElecComp, 3.0, "PCS"}, new Object[]{pPacking, 1.0, "BOX"}));
        BillOfMaterials bom2 = saveBOM("Widget B BOM",   pWidgetB, "1.0",
                List.of(new Object[]{pSteelRods, 4.0, "KG"}, new Object[]{pPlastic, 1.5, "KG"},   new Object[]{pElecComp, 5.0, "PCS"}));
        BillOfMaterials bom3 = saveBOM("Machine Part BOM",pMachinePart,"2.0",
                List.of(new Object[]{pSteelRods,10.0, "KG"}, new Object[]{pLubricant, 0.5, "CAN"}));

        // ── Work Orders ───────────────────────────────────────────────────────
        WorkOrder wo1 = saveWorkOrder("WO-2026-001", pWidgetA, bom1, 500, LocalDate.of(2026,4,5), LocalDate.of(2026,4,25), "COMPLETED");
        WorkOrder wo2 = saveWorkOrder("WO-2026-002", pWidgetB, bom2, 200, LocalDate.of(2026,4,10),LocalDate.of(2026,5,5),  "IN_PROGRESS");
        WorkOrder wo3 = saveWorkOrder("WO-2026-003", pMachinePart,bom3,100,LocalDate.of(2026,4,20),LocalDate.of(2026,5,15),"PLANNED");

        // ── Operation Types ───────────────────────────────────────────────────
        OperationType opMachining = saveOpType("Machining",  "OT-MACH", "MACHINING");
        OperationType opAssembly  = saveOpType("Assembly",   "OT-ASSY", "ASSEMBLY");
        OperationType opTesting   = saveOpType("Testing",    "OT-TEST", "TESTING");
        OperationType opPacking   = saveOpType("Packaging",  "OT-PACK", "PACKAGING");

        // ── Process Steps ─────────────────────────────────────────────────────
        ProcessStep ps1 = saveProcessStep("Raw Material Inspection", "PS-001", opTesting,  1.0, 0.5);
        ProcessStep ps2 = saveProcessStep("Cutting & Shaping",       "PS-002", opMachining,2.0, 0.5);
        ProcessStep ps3 = saveProcessStep("Component Assembly",      "PS-003", opAssembly, 3.0, 1.0);
        ProcessStep ps4 = saveProcessStep("Final QC Test",           "PS-004", opTesting,  1.5, 0.0);
        ProcessStep ps5 = saveProcessStep("Labelling & Packing",     "PS-005", opPacking,  0.5, 0.0);

        // ── Process Templates ─────────────────────────────────────────────────
        saveProcessTemplate("Standard Widget Manufacturing", "PT-001", "Finished Goods",
                List.of(ps1, ps2, ps3, ps4, ps5));
        saveProcessTemplate("Machined Parts Process",        "PT-002", "Raw Materials",
                List.of(ps1, ps2, ps4));

        // ── Batch Tracking ────────────────────────────────────────────────────
        BatchHistory bat1 = saveBatch("BATCH-2026-001", pWidgetA, LocalDate.of(2026,4,26), LocalDate.of(2027,4,25), 480, wo1.getId(), "APPROVED");
        BatchHistory bat2 = saveBatch("BATCH-2026-002", pWidgetB, LocalDate.of(2026,5,6),  LocalDate.of(2027,5,5),  185, wo2.getId(), "IN_TESTING");
        BatchHistory bat3 = saveBatch("BATCH-2026-003", pMachinePart,LocalDate.of(2026,5,20),LocalDate.of(2028,5,19),90, wo3.getId(), "CREATED");

        // ── Quality Inspections ───────────────────────────────────────────────
        saveQualityInspection("QI-2026-001", "INCOMING",   pSteelRods,   grn1.getId(), "GRN-2026-001", LocalDate.of(2026,4,14), "PASS");
        saveQualityInspection("QI-2026-002", "IN_PROCESS", pWidgetA,     wo1.getId(),  "WO-2026-001",  LocalDate.of(2026,4,20), "PASS");
        saveQualityInspection("QI-2026-003", "INCOMING",   pMachinePart, grn2.getId(), "GRN-2026-002", LocalDate.of(2026,4,21), "CONDITIONAL");

        // ── Quality Rejections ────────────────────────────────────────────────
        saveQualityRejection("QR-2026-001", bat1.getId(), "BATCH-2026-001", pWidgetA,
                LocalDate.of(2026,4,28), 20, "Dimensional deviation beyond tolerance", "REWORK");
        saveQualityRejection("QR-2026-002", grn1.getId(), "GRN-2026-001",   pSteelRods,
                LocalDate.of(2026,4,15), 20, "Steel grade below specification",         "RETURN_TO_SUPPLIER");

        // ── Accounting Periods ────────────────────────────────────────────────
        int[][] periods = {{2026,4},{2026,5},{2026,6},{2026,7},{2026,8},{2026,9}};
        String[] monthNames = {"April","May","June","July","August","September"};
        for (int i = 0; i < periods.length; i++) {
            saveAccountingPeriod(periods[i][0], periods[i][1], monthNames[i] + " " + periods[i][0],
                    i < 2 ? "CLOSED" : "OPEN");
        }

        // ── Bank Accounts ─────────────────────────────────────────────────────
        BankAccount bankHDFC = saveBankAccount("HDFC Main Account",   "00112233445566", "HDFC Bank",   "HDFC0001234", "Banjara Hills, Hyderabad", 500000.0);
        BankAccount bankSBI  = saveBankAccount("SBI Operations Account","33445566778800","State Bank", "SBIN0007890", "Jubilee Hills, Hyderabad", 250000.0);

        // ── Bank Reconciliation ───────────────────────────────────────────────
        saveBankReconciliation(bankHDFC, LocalDate.of(2026,4,30), 540000.0, 536250.0, "RECONCILED");

        // ── Cost Centers ──────────────────────────────────────────────────────
        CostCenter ccSales = saveCostCenter("Sales Division",   "CC-SALES", "DEPARTMENT", 2000000.0, null);
        CostCenter ccMfg   = saveCostCenter("Manufacturing",    "CC-MFG",   "DEPARTMENT", 5000000.0, null);
        CostCenter ccAdmin = saveCostCenter("Administration",   "CC-ADMIN", "DIVISION",   800000.0,  null);
        saveCostCenter("North Sales",   "CC-SALES-N", "PROJECT", 800000.0, ccSales);
        saveCostCenter("South Sales",   "CC-SALES-S", "PROJECT", 1200000.0, ccSales);

        // ── Asset Groups ──────────────────────────────────────────────────────
        AssetGroup agIT    = saveAssetGroup("IT Equipment",       "AG-IT",    "STRAIGHT_LINE",     3, 33.33);
        AssetGroup agMach  = saveAssetGroup("Plant & Machinery",  "AG-MACH",  "WRITTEN_DOWN_VALUE",10, 15.00);
        AssetGroup agFurn  = saveAssetGroup("Furniture & Fixtures","AG-FURN", "STRAIGHT_LINE",     5, 20.00);

        // ── Fixed Assets ──────────────────────────────────────────────────────
        saveFixedAsset("FA-001", "Office Laptop - Finance",      agIT,   LocalDate.of(2025,6,1),  65000.0,  5000.0, 3,  "STRAIGHT_LINE",  "Finance Dept");
        saveFixedAsset("FA-002", "CNC Machine Unit 1",           agMach, LocalDate.of(2024,10,1),1250000.0,50000.0, 10, "WRITTEN_DOWN_VALUE","Shop Floor A");
        saveFixedAsset("FA-003", "Conference Room Furniture",    agFurn, LocalDate.of(2025,3,1),  85000.0,  8500.0, 5,  "STRAIGHT_LINE",  "Board Room");
        saveFixedAsset("FA-004", "Server Rack - IT",             agIT,   LocalDate.of(2025,1,1), 180000.0, 15000.0, 3,  "STRAIGHT_LINE",  "Server Room");

        // ── Payment Terms ─────────────────────────────────────────────────────
        savePaymentTerm("Net 30",          "NET30",  30, 0,  BigDecimal.ZERO,        10, BigDecimal.valueOf(2.0));
        savePaymentTerm("Net 45",          "NET45",  45, 0,  BigDecimal.ZERO,        15, BigDecimal.valueOf(1.5));
        savePaymentTerm("Net 60 / 2-10",   "NET60",  60, 10, BigDecimal.valueOf(2.0), 20, BigDecimal.valueOf(1.0));
        savePaymentTerm("Immediate",       "IMMED",   0, 0,  BigDecimal.ZERO,         5, BigDecimal.valueOf(0.5));

        // ── Early Payment Discounts ───────────────────────────────────────────
        saveEarlyPayDiscount(inv2.getId(), "INV-2026-002", custEnt.getId(), custEnt.getName(),
                LocalDate.of(2026,4,25), LocalDate.of(2026,5,5), 70576.0, 2.0);
        saveEarlyPayDiscount(inv3.getId(), "INV-2026-003", custQuick.getId(), custQuick.getName(),
                LocalDate.of(2026,4,28), LocalDate.of(2026,5,8), 18388.0, 1.5);

        // ── Journal Entries ───────────────────────────────────────────────────
        saveJournalEntry("JE-2026-001", LocalDate.of(2026,4,20), "Sales Invoice INV-2026-001 — Mega Retail",
                "1200", "Accounts Receivable", 68469.5, 0.0,
                "4100", "Sales Revenue",        0.0,      58025.0);
        saveJournalEntry("JE-2026-002", LocalDate.of(2026,4,15), "Purchase from ABC Suppliers — PO-2026-001",
                "5100", "Cost of Goods Sold",  244100.0, 0.0,
                "2100", "Accounts Payable",     0.0,     244100.0);
        saveJournalEntry("JE-2026-003", LocalDate.of(2026,4,25), "Salary disbursement — April 2026",
                "5200", "Salaries & Wages",    325000.0, 0.0,
                "1120", "Bank Account",          0.0,    325000.0);

        // ── HR: Leave Types ───────────────────────────────────────────────────
        LeaveType ltAnnual  = saveLeaveType("Annual Leave",    "AL",  24, true,  true);
        LeaveType ltPrivilege = saveLeaveType("Privilege Leave","PL", 18, true,  true);
        LeaveType ltSick    = saveLeaveType("Sick Leave",      "SL",  12, false, true);
        LeaveType ltCasual  = saveLeaveType("Casual Leave",    "CL",   8, false, true);
        LeaveType ltMaternity = saveLeaveType("Maternity Leave","ML", 90, false, true);
        LeaveType ltPaternity = saveLeaveType("Paternity Leave","PTL", 5, false, true);

        // ── Employees ─────────────────────────────────────────────────────────
        Employee emp1 = saveEmployee("EMP001","Arjun",  "Sharma",  "arjun.sharma@erp.com",  "9810101010","M",LocalDate.of(1990,5,15),"Sales Manager",  salesDept, hoBranch,  65000.0);
        Employee emp2 = saveEmployee("EMP002","Priya",  "Nayak",   "priya.nayak@erp.com",   "9810202020","F",LocalDate.of(1992,8,22),"Purchase Officer",purchDept,hoBranch,  48000.0);
        Employee emp3 = saveEmployee("EMP003","Suresh", "Iyer",    "suresh.iyer@erp.com",   "9810303030","M",LocalDate.of(1988,3,10),"Accountant",     finDept,  hoBranch,  55000.0);
        Employee emp4 = saveEmployee("EMP004","Meena",  "Pillai",  "meena.pillai@erp.com",  "9810404040","F",LocalDate.of(1995,11,5),"HR Executive",   hrDept,   hoBranch,  40000.0);
        Employee emp5 = saveEmployee("EMP005","Kiran",  "Reddy",   "kiran.reddy@erp.com",   "9810505050","M",LocalDate.of(1987,7,18),"Production Mgr", mfgDept,  hoBranch,  72000.0);
        Employee emp6 = saveEmployee("EMP006","Anita",  "Kulkarni","anita.kulkarni@erp.com","9810606060","F",LocalDate.of(1993,2,28),"Sales Executive",salesDept,mumBranch, 38000.0);

        // ── Leave Applications ────────────────────────────────────────────────
        saveLeaveApp(emp1.getId(), ltAnnual, LocalDate.of(2026,5,12), LocalDate.of(2026,5,16), 5, "Family vacation");
        saveLeaveApp(emp4.getId(), ltSick,   LocalDate.of(2026,5,8),  LocalDate.of(2026,5,9),  2, "Fever");
        saveLeaveApp(emp6.getId(), ltCasual, LocalDate.of(2026,5,19), LocalDate.of(2026,5,19), 1, "Personal work");

        // ── Attendance ────────────────────────────────────────────────────────
        LocalDate[] workDays = {
            LocalDate.of(2026,5,4), LocalDate.of(2026,5,5), LocalDate.of(2026,5,6),
            LocalDate.of(2026,5,7), LocalDate.of(2026,5,8)
        };
        for (Employee emp : List.of(emp1, emp2, emp3, emp4, emp5, emp6)) {
            for (LocalDate d : workDays) {
                saveAttendance(emp.getId(), d, LocalTime.of(9,0), LocalTime.of(18,0), "PRESENT");
            }
        }

        // ── Salary Components ─────────────────────────────────────────────────
        SalaryComponent scBasic   = saveSalaryComponent("Basic Salary",       "BASIC",  "EARNING",    false, 100.0, "BASIC",  false);
        SalaryComponent scHRA     = saveSalaryComponent("House Rent Allowance","HRA",   "EARNING",    true,  40.0,  "BASIC",  false);
        SalaryComponent scTA      = saveSalaryComponent("Travel Allowance",    "TA",    "EARNING",    false, 1500.0,"BASIC",  false);
        SalaryComponent scPF      = saveSalaryComponent("Provident Fund (EE)", "PF_EE", "DEDUCTION",  true,  12.0,  "BASIC",  false);
        SalaryComponent scPT      = saveSalaryComponent("Professional Tax",    "PT",    "DEDUCTION",  false, 200.0, "BASIC",  false);
        SalaryComponent scESIC    = saveSalaryComponent("ESIC (EE)",           "ESIC",  "DEDUCTION",  true,  0.75,  "GROSS",  false);

        // ── Payslips ──────────────────────────────────────────────────────────
        savePayslip("PS-2026-04-001", emp1.getId(), 4, 2026, LocalDate.of(2026,5,1), 65000.0, 26, 24,
                List.of(
                    pLine(scBasic, 65000.0, "EARNING"),
                    pLine(scHRA,   26000.0, "EARNING"),
                    pLine(scTA,     1500.0, "EARNING"),
                    pLine(scPF,     7800.0, "DEDUCTION"),
                    pLine(scPT,      200.0, "DEDUCTION")
                ), "PAID");
        savePayslip("PS-2026-04-002", emp3.getId(), 4, 2026, LocalDate.of(2026,5,1), 55000.0, 26, 26,
                List.of(
                    pLine(scBasic, 55000.0, "EARNING"),
                    pLine(scHRA,   22000.0, "EARNING"),
                    pLine(scTA,     1500.0, "EARNING"),
                    pLine(scPF,     6600.0, "DEDUCTION"),
                    pLine(scPT,      200.0, "DEDUCTION")
                ), "APPROVED");
        savePayslip("PS-2026-04-003", emp5.getId(), 4, 2026, LocalDate.of(2026,5,1), 72000.0, 26, 25,
                List.of(
                    pLine(scBasic, 72000.0, "EARNING"),
                    pLine(scHRA,   28800.0, "EARNING"),
                    pLine(scTA,     1500.0, "EARNING"),
                    pLine(scPF,     8640.0, "DEDUCTION"),
                    pLine(scPT,      200.0, "DEDUCTION")
                ), "DRAFT");

        // ── Price Lists ───────────────────────────────────────────────────────
        PriceList plRetail  = savePriceList("Retail Price List",     "PL-RETAIL",  true);
        PriceList plWhole   = savePriceList("Wholesale Price List",  "PL-WHOLE",   false);
        PriceList plExport  = savePriceList("Export Price List",     "PL-EXPORT",  false);

        savePriceListItem(plRetail,  pWidgetA, 250.0, 1.0,   999.0, 0.0,   LocalDate.of(2026,4,1), LocalDate.of(2027,3,31));
        savePriceListItem(plRetail,  pWidgetB, 520.0, 1.0,   999.0, 0.0,   LocalDate.of(2026,4,1), LocalDate.of(2027,3,31));
        savePriceListItem(plWhole,   pWidgetA, 215.0, 100.0, 9999.0, 10.0, LocalDate.of(2026,4,1), LocalDate.of(2027,3,31));
        savePriceListItem(plWhole,   pWidgetB, 450.0, 50.0,  9999.0, 10.0, LocalDate.of(2026,4,1), LocalDate.of(2027,3,31));
        savePriceListItem(plExport,  pWidgetA, 200.0, 500.0, 99999.0,15.0, LocalDate.of(2026,4,1), LocalDate.of(2027,3,31));

        // ── Discount Schemes ──────────────────────────────────────────────────
        DiscountScheme dsFlat = saveDiscountScheme("Festive 10% Off",   "DS-FEST",  "FLAT_PERCENTAGE", 10.0, "RETAIL",   LocalDate.of(2026,10,1), LocalDate.of(2026,10,31));
        DiscountScheme dsSlab = saveDiscountScheme("Volume Quantity Discount","DS-QTY","SLAB_QUANTITY",  0.0,  "WHOLESALE",LocalDate.of(2026,4,1),  LocalDate.of(2027,3,31));
        saveDiscountSlab(dsSlab, 100.0,  499.0, 5.0, 0.0);
        saveDiscountSlab(dsSlab, 500.0,  999.0, 8.0, 0.0);
        saveDiscountSlab(dsSlab,1000.0, 99999.0,12.0, 0.0);

        // ── Workflow Definitions ──────────────────────────────────────────────
        saveWorkflowDefinition("Purchase Order Approval",  "PURCHASE", 50000.0, 500000.0);
        saveWorkflowDefinition("Invoice Approval",         "SALES",    25000.0, 99999999.0);

        // ── Phase 2: Dispatch ─────────────────────────────────────────────────
        seedDispatches(custMega, custEnt, custQuick, pWidgetA, pWidgetB, pMachinePart, whMain);

        // ── Phase 2: Promotions ───────────────────────────────────────────────
        seedPromotions(pWidgetA, pWidgetB);

        // ── Phase 2: Supplier Contracts & Performance ─────────────────────────
        seedSupplierContracts(vendorABC, vendorXYZ, vendorTech, pSteelRods, pPlastic, pElecComp);
        seedSupplierPerformance(vendorABC, vendorXYZ, vendorTech);
        seedSupplierNonConformances(vendorABC, vendorXYZ);

        // ── Phase 2: Accounting Extensions ───────────────────────────────────
        seedDimensions();
        seedPeriodClose();
        seedBudget();

        // ── Phase 2: Storage Locations ────────────────────────────────────────
        seedStorageLocations(whMain, whMumbai, pWidgetA, pWidgetB, pSteelRods);

        // ── Phase 3: UoM ──────────────────────────────────────────────────────
        seedUoM();

        // ── Phase 3: Brand + ProductLine ──────────────────────────────────────
        seedBrandsAndProductLines();

        // ── Phase 3: Opening Balances ─────────────────────────────────────────
        seedOpeningBalances(custMega, custEnt, vendorABC, vendorXYZ, whMain, pWidgetA, pWidgetB);

        // ── Phase 3: Sales Planning ───────────────────────────────────────────
        seedSalesPlans(pWidgetA, pWidgetB, pMachinePart);
        seedDemandForecasts(pWidgetA, pWidgetB, pSteelRods, pElecComp, whMain);

        // ── Phase 3: Supplier Payments ────────────────────────────────────────
        seedSupplierPayments(vendorABC, vendorXYZ);

        // ── Phase 3: Scrap ────────────────────────────────────────────────────
        seedScrapEntries(whMain, pSteelRods, pWidgetA, pElecComp);

        // ── Phase 3: Approval Workflow Rules ──────────────────────────────────
        seedApprovalRules();

        // ── Phase 3: Report Definitions ───────────────────────────────────────
        seedReportDefinitions();

        log.info("Test data seeding complete — all modules populated");
    }

    // ══════════════════════════ Helper builders ═══════════════════════════════

    /**
     * Reuses the branch of that code if it is already there.
     *
     * <p>The seeder as a whole is skipped once vendors exist, but branches are created before
     * vendors: if anything in between failed, the next start found no vendors, ran again, and added
     * the branches a second time. Every restart added three more.
     */
    private Branch saveBranch(String name, String code, String city, String state, boolean hq) {
        var existing = branchRepo.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(T, code);
        if (existing.isPresent()) return existing.get();
        Branch b = new Branch(); b.setTenantId(T);
        b.setCompanyId(companyRepo.findAll().stream().findFirst().map(Company::getId).orElse(T));
        b.setName(name); b.setCode(code); b.setCity(city); b.setState(state);
        b.setHeadOffice(hq); b.setActive(true); return branchRepo.save(b);
    }

    /**
     * A department belongs to a company, and company_id is not nullable.
     *
     * <p>Leaving it unset stopped the very first department being written, and the seeder gives up
     * at the first failure, so on a brand-new database nothing after this point was created at all:
     * no vendors, no employees, no orders. The application still started, and every screen that
     * reads any of it was simply empty, with nothing to say why.
     */
    private Department saveDept(String name, String code) {
        Department d = new Department(); d.setTenantId(T);
        d.setCompanyId(companyRepo.findAll().stream().findFirst().map(Company::getId).orElse(T));
        d.setName(name); d.setCode(code); d.setActive(true); return deptRepo.save(d);
    }

    private Vendor saveVendor(String name, String code, String email, String phone, String cat) {
        Vendor v = new Vendor(); v.setTenantId(T);
        v.setName(name); v.setCode(code); v.setEmail(email); v.setPhone(phone);
        v.setCategory(Vendor.VendorCategory.valueOf(cat));
        v.setCity("Hyderabad"); v.setState("Telangana"); v.setCountry("India");
        v.setPaymentTermsDays(30); v.setActive(true); return vendorRepo.save(v);
    }

    private Customer saveCustomer(String name, String code, String email, String phone, String cat, double credit) {
        Customer c = new Customer(); c.setTenantId(T);
        c.setName(name); c.setCode(code); c.setEmail(email); c.setPhone(phone);
        c.setCategory(Customer.CustomerCategory.valueOf(cat));
        c.setCreditLimit(BigDecimal.valueOf(credit));
        c.setOutstandingBalance(BigDecimal.ZERO);
        c.setCity("Hyderabad"); c.setState("Telangana"); c.setCountry("India");
        c.setPaymentTermsDays(30); c.setActive(true); return customerRepo.save(c);
    }

    private Product saveProduct(String name, String code, ProductCategory cat, Tax tax,
                                String unit, double pp, double sp, double mrp, Product.ProductType type) {
        Product p = new Product(); p.setTenantId(T);
        p.setName(name); p.setCode(code); p.setSku(code); p.setCategory(cat); p.setTaxRate(tax);
        p.setUnit(unit); p.setProductType(type);
        p.setPurchasePrice(BigDecimal.valueOf(pp));
        p.setSalePrice(BigDecimal.valueOf(sp));
        p.setMrp(BigDecimal.valueOf(mrp));
        p.setCurrentStock(BigDecimal.ZERO); p.setReorderLevel(BigDecimal.valueOf(50));
        p.setTrackInventory(type != Product.ProductType.SERVICE); p.setActive(true);
        return productRepo.save(p);
    }

    private void saveProductSpec(Product prod, String specType, String attr, String unit, String val,
                                  BigDecimal min, BigDecimal max) {
        ProductSpecification s = new ProductSpecification(); s.setTenantId(T);
        s.setProduct(prod); s.setSpecType(ProductSpecification.SpecType.valueOf(specType));
        s.setAttributeName(attr); s.setAttributeValue(val); s.setUnit(unit);
        s.setMinValue(min); s.setMaxValue(max); s.setMandatory(false);
        productSpecRepo.save(s);
    }

    private Warehouse saveWarehouse(String name, String code, String city, boolean def) {
        Warehouse w = new Warehouse(); w.setTenantId(T);
        w.setName(name); w.setCode(code); w.setCity(city);
        w.setDefault(def); w.setActive(true); return warehouseRepo.save(w);
    }

    private void saveStockItem(Warehouse wh, Product prod, BigDecimal qty, BigDecimal avgCost) {
        StockItem s = new StockItem(); s.setTenantId(T);
        s.setWarehouseId(wh.getId()); s.setProductId(prod.getId());
        s.setProductName(prod.getName()); s.setQuantityOnHand(qty);
        s.setQuantityReserved(BigDecimal.ZERO); s.setAverageCost(avgCost);
        stockItemRepo.save(s);
    }

    private void saveLead(String company, String name, String email, String phone,
                           String source, String status, String priority, double value) {
        Lead l = new Lead(); l.setTenantId(T);
        l.setCompany(company); l.setName(name); l.setEmail(email); l.setPhone(phone);
        l.setSource(Lead.LeadSource.valueOf(source));
        l.setStatus(Lead.LeadStatus.valueOf(status));
        l.setPriority(Lead.LeadPriority.valueOf(priority));
        l.setEstimatedValue(BigDecimal.valueOf(value));
        l.setExpectedCloseDate(LocalDate.now().plusMonths(2));
        leadRepo.save(l);
    }

    private void saveContact(String first, String last, String email, String phone,
                              String desig, UUID custId) {
        Contact c = new Contact(); c.setTenantId(T);
        c.setFirstName(first); c.setLastName(last); c.setEmail(email);
        c.setPhone(phone); c.setDesignation(desig); c.setCustomerId(custId);
        contactRepo.save(c);
    }

    private SalesTerritory saveTerritory(String name, String code, String region, String desc) {
        SalesTerritory t = new SalesTerritory(); t.setTenantId(T);
        t.setName(name); t.setCode(code); t.setRegion(region);
        t.setDescription(desc); t.setCountry("India"); t.setActive(true);
        return salesTerritoryRepo.save(t);
    }

    private SalesRepresentative saveSalesRep(String name, String code, String email, String phone,
                                              SalesTerritory terr, double target, double comm) {
        SalesRepresentative r = new SalesRepresentative(); r.setTenantId(T);
        r.setName(name); r.setCode(code); r.setEmail(email); r.setPhone(phone);
        r.setTerritory(terr); r.setTargetAmount(BigDecimal.valueOf(target));
        r.setCommissionPercent(BigDecimal.valueOf(comm)); r.setActive(true);
        return salesRepRepo.save(r);
    }

    private void saveSalesTarget(String name, String period, SalesTerritory terr, SalesRepresentative rep,
                                  LocalDate start, LocalDate end, double target, double actual) {
        SalesTarget t = new SalesTarget(); t.setTenantId(T);
        t.setName(name); t.setPeriod(period); t.setTerritory(terr); t.setSalesRep(rep);
        t.setStartDate(start); t.setEndDate(end);
        t.setTargetAmount(BigDecimal.valueOf(target)); t.setActualAmount(BigDecimal.valueOf(actual));
        t.setStatus(SalesTarget.TargetStatus.ACTIVE);
        salesTargetRepo.save(t);
    }

    // ── Quotation helpers ──────────────────────────────────────────────────────
    record QI(Product prod, int qty, double price, double disc, double tax) {}
    private QI qi(Product p, int qty, double price, double disc, double tax) { return new QI(p,qty,price,disc,tax); }

    private Quotation saveQuotation(String num, Customer cust, LocalDate date, LocalDate valid, List<QI> items) {
        Quotation q = new Quotation(); q.setTenantId(T);
        q.setQuotationNumber(num); q.setCustomerId(cust.getId()); q.setCustomerName(cust.getName());
        q.setQuotationDate(date); q.setValidUntil(valid);
        q.setStatus(Quotation.QuotationStatus.SENT);
        List<QuotationItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO, taxTotal = BigDecimal.ZERO, total = BigDecimal.ZERO;
        for (QI it : items) {
            QuotationItem li = new QuotationItem();
            BigDecimal linePrice = BigDecimal.valueOf(it.price());
            BigDecimal lineQty   = BigDecimal.valueOf(it.qty());
            BigDecimal disc      = linePrice.multiply(lineQty).multiply(BigDecimal.valueOf(it.disc()/100));
            BigDecimal net       = linePrice.multiply(lineQty).subtract(disc);
            BigDecimal taxAmt    = net.multiply(BigDecimal.valueOf(it.tax()/100));
            BigDecimal lineTotal = net.add(taxAmt);
            li.setQuotation(q); li.setProductId(it.prod().getId()); li.setProductName(it.prod().getName());
            li.setQuantity(lineQty); li.setUnit(it.prod().getUnit()); li.setUnitPrice(linePrice);
            li.setDiscountPercent(BigDecimal.valueOf(it.disc())); li.setTaxPercent(BigDecimal.valueOf(it.tax()));
            li.setTaxAmount(taxAmt); li.setTotalAmount(lineTotal);
            lineItems.add(li); subtotal = subtotal.add(net); taxTotal = taxTotal.add(taxAmt); total = total.add(lineTotal);
        }
        q.setItems(lineItems); q.setSubtotal(subtotal); q.setTaxAmount(taxTotal);
        q.setDiscountAmount(BigDecimal.ZERO); q.setTotalAmount(total);
        return quotationRepo.save(q);
    }

    private SalesOrder saveSalesOrder(String num, Customer cust, LocalDate date, LocalDate delivery,
                                       List<QI> items, String status) {
        SalesOrder o = new SalesOrder(); o.setTenantId(T);
        o.setOrderNumber(num); o.setCustomerId(cust.getId()); o.setCustomerName(cust.getName());
        o.setOrderDate(date); o.setDeliveryDate(delivery);
        o.setStatus(SalesOrder.SalesOrderStatus.valueOf(status));
        List<SalesOrderItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO, taxTotal = BigDecimal.ZERO, total = BigDecimal.ZERO;
        for (QI it : items) {
            SalesOrderItem li = new SalesOrderItem();
            BigDecimal linePrice = BigDecimal.valueOf(it.price());
            BigDecimal lineQty   = BigDecimal.valueOf(it.qty());
            BigDecimal disc      = linePrice.multiply(lineQty).multiply(BigDecimal.valueOf(it.disc()/100));
            BigDecimal net       = linePrice.multiply(lineQty).subtract(disc);
            BigDecimal taxAmt    = net.multiply(BigDecimal.valueOf(it.tax()/100));
            BigDecimal lineTotal = net.add(taxAmt);
            li.setSalesOrder(o); li.setProductId(it.prod().getId()); li.setProductName(it.prod().getName());
            li.setQuantity(lineQty); li.setUnit(it.prod().getUnit()); li.setUnitPrice(linePrice);
            li.setDiscountPercent(BigDecimal.valueOf(it.disc())); li.setTaxPercent(BigDecimal.valueOf(it.tax()));
            li.setTaxAmount(taxAmt); li.setTotalAmount(lineTotal); li.setDeliveredQty(BigDecimal.ZERO);
            lineItems.add(li); subtotal = subtotal.add(net); taxTotal = taxTotal.add(taxAmt); total = total.add(lineTotal);
        }
        o.setItems(lineItems); o.setSubtotal(subtotal); o.setTaxAmount(taxTotal);
        o.setDiscountAmount(BigDecimal.ZERO); o.setTotalAmount(total);
        return salesOrderRepo.save(o);
    }

    private Invoice saveInvoice(String num, Customer cust, SalesOrder so, LocalDate date, LocalDate due,
                                 List<QI> items, String status, double total, double paid) {
        Invoice inv = new Invoice(); inv.setTenantId(T);
        inv.setInvoiceNumber(num); inv.setCustomerId(cust.getId()); inv.setCustomerName(cust.getName());
        inv.setSalesOrderId(so.getId()); inv.setInvoiceDate(date); inv.setDueDate(due);
        inv.setStatus(Invoice.InvoiceStatus.valueOf(status));
        List<InvoiceItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO, taxTotal = BigDecimal.ZERO;
        for (QI it : items) {
            InvoiceItem li = new InvoiceItem();
            BigDecimal linePrice = BigDecimal.valueOf(it.price());
            BigDecimal lineQty   = BigDecimal.valueOf(it.qty());
            BigDecimal disc      = linePrice.multiply(lineQty).multiply(BigDecimal.valueOf(it.disc()/100));
            BigDecimal net       = linePrice.multiply(lineQty).subtract(disc);
            BigDecimal taxAmt    = net.multiply(BigDecimal.valueOf(it.tax()/100));
            BigDecimal lineTotal = net.add(taxAmt);
            li.setInvoice(inv); li.setProductId(it.prod().getId()); li.setProductName(it.prod().getName());
            li.setQuantity(lineQty); li.setUnit(it.prod().getUnit()); li.setUnitPrice(linePrice);
            li.setDiscountPercent(BigDecimal.valueOf(it.disc())); li.setDiscountAmount(disc);
            li.setTaxPercent(BigDecimal.valueOf(it.tax())); li.setTaxAmount(taxAmt); li.setTotalAmount(lineTotal);
            lineItems.add(li); subtotal = subtotal.add(net); taxTotal = taxTotal.add(taxAmt);
        }
        BigDecimal totalAmt  = BigDecimal.valueOf(total);
        BigDecimal paidAmt   = BigDecimal.valueOf(paid);
        inv.setItems(lineItems); inv.setSubtotal(subtotal); inv.setTaxAmount(taxTotal);
        inv.setDiscountAmount(BigDecimal.ZERO); inv.setTotalAmount(totalAmt);
        inv.setPaidAmount(paidAmt); inv.setBalanceDue(totalAmt.subtract(paidAmt));
        inv.setFreightCharges(BigDecimal.ZERO); inv.setPackingForwarding(BigDecimal.ZERO);
        return invoiceRepo.save(inv);
    }

    private void saveDeliveryNote(String num, UUID soId, String custName, LocalDate date, String status, String addr) {
        DeliveryNote d = new DeliveryNote(); d.setTenantId(T);
        d.setDeliveryNumber(num); d.setSalesOrderId(soId); d.setCustomerName(custName);
        d.setDeliveryDate(date); d.setStatus(status); d.setDeliveryAddress(addr);
        deliveryNoteRepo.save(d);
    }

    private void saveSalesReturn(String num, UUID invId, String custName, LocalDate date, String reason, double amt) {
        SalesReturn r = new SalesReturn(); r.setTenantId(T);
        r.setReturnNumber(num); r.setInvoiceId(invId); r.setCustomerName(custName);
        r.setReturnDate(date); r.setReason(reason); r.setTotalAmount(BigDecimal.valueOf(amt));
        r.setStatus("APPROVED");
        salesReturnRepo.save(r);
    }

    private void saveCustomerAdvance(String num, Customer cust, LocalDate date, double amt, String mode, String ref) {
        CustomerAdvance a = new CustomerAdvance(); a.setTenantId(T);
        a.setAdvanceNumber(num); a.setCustomerId(cust.getId()); a.setCustomerName(cust.getName());
        a.setPaymentDate(date); a.setAmount(BigDecimal.valueOf(amt));
        a.setAmountApplied(BigDecimal.ZERO); a.setAmountAvailable(BigDecimal.valueOf(amt));
        a.setPaymentMode(mode); a.setReferenceNumber(ref);
        a.setStatus(CustomerAdvance.AdvanceStatus.AVAILABLE);
        customerAdvanceRepo.save(a);
    }

    // ── Purchase helpers ──────────────────────────────────────────────────────
    record PI(Product prod, int qty, double price, double disc, double tax) {}
    private PI pi(Product p, int qty, double price, double disc, double tax) { return new PI(p,qty,price,disc,tax); }

    private PurchaseOrder savePurchaseOrder(String num, Vendor vendor, LocalDate date, LocalDate delivery,
                                             List<PI> items, String status) {
        PurchaseOrder po = new PurchaseOrder(); po.setTenantId(T);
        po.setPoNumber(num); po.setVendorId(vendor.getId()); po.setVendorName(vendor.getName());
        po.setOrderDate(date); po.setExpectedDeliveryDate(delivery);
        po.setStatus(PurchaseOrder.POStatus.valueOf(status));
        List<PurchaseOrderItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO, taxTotal = BigDecimal.ZERO, total = BigDecimal.ZERO;
        for (PI it : items) {
            PurchaseOrderItem li = new PurchaseOrderItem();
            BigDecimal linePrice = BigDecimal.valueOf(it.price());
            BigDecimal lineQty   = BigDecimal.valueOf(it.qty());
            BigDecimal disc      = linePrice.multiply(lineQty).multiply(BigDecimal.valueOf(it.disc()/100));
            BigDecimal net       = linePrice.multiply(lineQty).subtract(disc);
            BigDecimal taxAmt    = net.multiply(BigDecimal.valueOf(it.tax()/100));
            BigDecimal lineTotal = net.add(taxAmt);
            li.setPurchaseOrder(po); li.setProductId(it.prod().getId()); li.setProductName(it.prod().getName());
            li.setQuantity(lineQty); li.setUnit(it.prod().getUnit()); li.setUnitPrice(linePrice);
            li.setDiscountPercent(BigDecimal.valueOf(it.disc())); li.setDiscountAmount(disc);
            li.setTaxPercent(BigDecimal.valueOf(it.tax())); li.setTaxAmount(taxAmt); li.setTotalAmount(lineTotal);
            li.setReceivedQty(BigDecimal.ZERO);
            lineItems.add(li); subtotal = subtotal.add(net); taxTotal = taxTotal.add(taxAmt); total = total.add(lineTotal);
        }
        po.setItems(lineItems); po.setSubtotal(subtotal); po.setTaxAmount(taxTotal);
        po.setDiscountAmount(BigDecimal.ZERO); po.setTotalAmount(total); po.setFreightCharges(BigDecimal.ZERO);
        return purchaseOrderRepo.save(po);
    }

    record GRI(Product prod, int ordered, int received, int accepted, int rejected) {}
    private GRI gri(Product p, int o, int r, int a, int rej) { return new GRI(p,o,r,a,rej); }

    private GoodsReceipt saveGRN(String num, PurchaseOrder po, Vendor vendor, LocalDate date, List<GRI> items) {
        GoodsReceipt g = new GoodsReceipt(); g.setTenantId(T);
        g.setGrnNumber(num); g.setPurchaseOrderId(po.getId());
        g.setVendorId(vendor.getId()); g.setVendorName(vendor.getName());
        g.setReceiptDate(date); g.setStatus("RECEIVED");
        List<GoodsReceiptItem> lineItems = new ArrayList<>();
        for (GRI it : items) {
            GoodsReceiptItem li = new GoodsReceiptItem();
            li.setGoodsReceipt(g); li.setProductId(it.prod().getId()); li.setProductName(it.prod().getName());
            li.setOrderedQty(BigDecimal.valueOf(it.ordered())); li.setReceivedQty(BigDecimal.valueOf(it.received()));
            li.setAcceptedQty(BigDecimal.valueOf(it.accepted())); li.setRejectedQty(BigDecimal.valueOf(it.rejected()));
            if (it.rejected() > 0) li.setRejectionReason("Quality below standard");
            lineItems.add(li);
        }
        g.setItems(lineItems); return goodsReceiptRepo.save(g);
    }

    private void savePurchaseReturn(String num, UUID grnId, String vendorName, LocalDate date, String reason, double amt) {
        PurchaseReturn r = new PurchaseReturn(); r.setTenantId(T);
        r.setReturnNumber(num); r.setGoodsReceiptId(grnId); r.setVendorName(vendorName);
        r.setReturnDate(date); r.setReason(reason); r.setTotalAmount(BigDecimal.valueOf(amt));
        r.setStatus("APPROVED");
        purchaseReturnRepo.save(r);
    }

    private void saveSupplierAdvance(Vendor vendor, LocalDate date, double amt, String type, String ref) {
        SupplierAdvance a = new SupplierAdvance(); a.setTenantId(T);
        a.setVendor(vendor); a.setAdvanceDate(date); a.setAmount(BigDecimal.valueOf(amt));
        a.setAdvanceType(SupplierAdvance.AdvanceType.valueOf(type.equals("BANK_TRANSFER") ? "BANK_TRANSFER" : "CHEQUE"));
        a.setBankReference(ref); a.setAdjustedAmount(BigDecimal.ZERO);
        a.setStatus(SupplierAdvance.AdvanceStatus.PENDING);
        supplierAdvanceRepo.save(a);
    }

    private ServiceCategory saveServiceCategory(String name, String code) {
        ServiceCategory c = new ServiceCategory(); c.setTenantId(T);
        c.setName(name); c.setCode(code); c.setActive(true); return serviceCategoryRepo.save(c);
    }

    private ServiceDefinition saveServiceDef(String name, String code, ServiceCategory cat,
                                              String unit, double rate, Tax tax) {
        ServiceDefinition d = new ServiceDefinition(); d.setTenantId(T);
        d.setName(name); d.setCode(code); d.setCategory(cat); d.setUnit(unit);
        d.setStandardRate(BigDecimal.valueOf(rate)); d.setTaxRate(tax); d.setActive(true);
        return serviceDefinitionRepo.save(d);
    }

    record SVOI(ServiceDefinition svc, int qty, double rate, double tax) {}
    private SVOI svoi(ServiceDefinition s, int q, double r, double t) { return new SVOI(s,q,r,t); }

    private ServiceOrder saveServiceOrder(String num, Vendor vendor, LocalDate date, LocalDate end,
                                           String desc, List<SVOI> items) {
        ServiceOrder so = new ServiceOrder(); so.setTenantId(T);
        so.setOrderNumber(num); so.setVendor(vendor); so.setOrderDate(date);
        so.setExpectedCompletionDate(end); so.setDescription(desc);
        so.setStatus(ServiceOrder.ServiceOrderStatus.APPROVED);
        BigDecimal total = BigDecimal.ZERO;
        List<ServiceOrderItem> lineItems = new ArrayList<>();
        for (SVOI it : items) {
            ServiceOrderItem li = new ServiceOrderItem(); li.setTenantId(T);
            BigDecimal lineTotal = BigDecimal.valueOf(it.qty()).multiply(BigDecimal.valueOf(it.rate()))
                    .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(it.tax()/100)));
            li.setServiceOrder(so); li.setService(it.svc());
            li.setQuantity(BigDecimal.valueOf(it.qty())); li.setUnitRate(BigDecimal.valueOf(it.rate()));
            li.setTaxPercent(BigDecimal.valueOf(it.tax())); li.setTotalAmount(lineTotal);
            lineItems.add(li); total = total.add(lineTotal);
        }
        so.setItems(lineItems); so.setTotalAmount(total); return serviceOrderRepo.save(so);
    }

    private void savePurchaseInvoice(String num, Vendor vendor, String vendorInv, PurchaseOrder po, GoodsReceipt grn,
                                      LocalDate date, LocalDate due, double total, double paid) {
        PurchaseInvoice pi = new PurchaseInvoice(); pi.setTenantId(T);
        pi.setPiNumber(num); pi.setVendorInvoiceNumber(vendorInv);
        pi.setVendorId(vendor.getId()); pi.setVendorName(vendor.getName());
        pi.setPurchaseOrderId(po.getId()); pi.setGoodsReceiptId(grn.getId());
        pi.setInvoiceDate(date); pi.setDueDate(due);
        pi.setStatus(paid >= total ? PurchaseInvoice.PIStatus.PAID
                    : paid > 0    ? PurchaseInvoice.PIStatus.PARTIALLY_PAID
                                  : PurchaseInvoice.PIStatus.APPROVED);
        BigDecimal totalAmt = BigDecimal.valueOf(total), paidAmt = BigDecimal.valueOf(paid);
        pi.setTotalAmount(totalAmt); pi.setNetPayable(totalAmt); pi.setPaidAmount(paidAmt);
        pi.setBalanceDue(totalAmt.subtract(paidAmt));
        pi.setSubtotal(totalAmt.multiply(BigDecimal.valueOf(0.85)));
        pi.setTaxAmount(totalAmt.multiply(BigDecimal.valueOf(0.15)));
        pi.setDiscountAmount(BigDecimal.ZERO); pi.setFreightCharges(BigDecimal.ZERO);
        pi.setTdsApplicable(false); pi.setTdsAmount(BigDecimal.ZERO);
        purchaseInvoiceRepo.save(pi);
    }

    private void saveStockTransfer(String num, Warehouse from, Warehouse to, LocalDate date, List<Object[]> items) {
        StockTransfer st = new StockTransfer(); st.setTenantId(T);
        st.setTransferNumber(num); st.setFromWarehouseId(from.getId()); st.setFromWarehouseName(from.getName());
        st.setToWarehouseId(to.getId()); st.setToWarehouseName(to.getName());
        st.setTransferDate(date); st.setStatus("COMPLETED");
        List<StockTransferItem> lineItems = new ArrayList<>();
        for (Object[] it : items) {
            StockTransferItem li = new StockTransferItem();
            Product p = (Product) it[0]; double qty = (Double) it[1];
            li.setStockTransfer(st); li.setProductId(p.getId()); li.setProductName(p.getName());
            li.setQuantity(BigDecimal.valueOf(qty)); lineItems.add(li);
        }
        st.setItems(lineItems); stockTransferRepo.save(st);
    }

    private void savePhysicalCount(String num, Warehouse wh, LocalDate date, String status) {
        PhysicalCount pc = new PhysicalCount(); pc.setTenantId(T);
        pc.setCountNumber(num); pc.setWarehouseId(wh.getId()); pc.setWarehouseName(wh.getName());
        pc.setCountDate(date); pc.setStatus(status);
        physicalCountRepo.save(pc);
    }

    private BillOfMaterials saveBOM(String name, Product prod, String version, List<Object[]> materials) {
        BillOfMaterials bom = new BillOfMaterials(); bom.setTenantId(T);
        bom.setName(name); bom.setProductId(prod.getId()); bom.setProductName(prod.getName());
        bom.setVersion(version); bom.setActive(true);
        List<BOMItem> items = new ArrayList<>();
        for (Object[] m : materials) {
            BOMItem bi = new BOMItem(); Product mp = (Product) m[0];
            bi.setBom(bom); bi.setMaterialId(mp.getId()); bi.setMaterialName(mp.getName());
            bi.setQuantity(BigDecimal.valueOf((Double) m[1])); bi.setUnit((String) m[2]);
            items.add(bi);
        }
        bom.setItems(items); return bomRepo.save(bom);
    }

    private WorkOrder saveWorkOrder(String num, Product prod, BillOfMaterials bom, int qty,
                                     LocalDate start, LocalDate end, String status) {
        WorkOrder wo = new WorkOrder(); wo.setTenantId(T);
        wo.setOrderNumber(num); wo.setProductId(prod.getId()); wo.setProductName(prod.getName());
        wo.setBomId(bom.getId()); wo.setPlannedQuantity(BigDecimal.valueOf(qty));
        wo.setProducedQuantity(status.equals("COMPLETED") ? BigDecimal.valueOf(qty) : BigDecimal.ZERO);
        wo.setPlannedStartDate(start); wo.setPlannedEndDate(end);
        wo.setStatus(WorkOrder.WorkOrderStatus.valueOf(status));
        wo.setPriority("HIGH");
        return workOrderRepo.save(wo);
    }

    private OperationType saveOpType(String name, String code, String cat) {
        OperationType o = new OperationType(); o.setTenantId(T);
        o.setName(name); o.setCode(code);
        o.setCategory(OperationType.OperationCategory.valueOf(cat));
        o.setActive(true); return operationTypeRepo.save(o);
    }

    private ProcessStep saveProcessStep(String name, String code, OperationType opType, double duration, double setup) {
        ProcessStep ps = new ProcessStep(); ps.setTenantId(T);
        ps.setName(name); ps.setCode(code); ps.setOperationType(opType);
        ps.setStandardDurationHours(BigDecimal.valueOf(duration));
        ps.setSetupTimeHours(BigDecimal.valueOf(setup));
        ps.setQualityCheckRequired(opType.getCategory() == OperationType.OperationCategory.TESTING);
        ps.setActive(true); return processStepRepo.save(ps);
    }

    private void saveProcessTemplate(String name, String code, String category, List<ProcessStep> steps) {
        ProcessTemplate pt = new ProcessTemplate(); pt.setTenantId(T);
        pt.setName(name); pt.setCode(code); pt.setProductCategory(category); pt.setActive(true);
        pt = processTemplateRepo.save(pt);
        int seq = 1;
        for (ProcessStep step : steps) {
            ProcessTemplateStep pts = new ProcessTemplateStep(); pts.setTenantId(T);
            pts.setTemplate(pt); pts.setStep(step); pts.setSequenceNumber(seq++); pts.setOptional(false);
            processTemplateStepRepo.save(pts);
        }
    }

    private BatchHistory saveBatch(String batchNum, Product prod, LocalDate prod_date, LocalDate expiry,
                                    int qty, UUID woId, String status) {
        BatchHistory b = new BatchHistory(); b.setTenantId(T);
        b.setBatchNumber(batchNum); b.setProduct(prod);
        b.setProductionDate(prod_date); b.setExpiryDate(expiry);
        b.setQuantity(BigDecimal.valueOf(qty)); b.setRemainingQuantity(BigDecimal.valueOf(qty));
        b.setUnit("PCS"); b.setWorkOrderId(woId);
        b.setStatus(BatchHistory.BatchStatus.valueOf(status));
        return batchHistoryRepo.save(b);
    }

    private void saveQualityInspection(String num, String type, Product prod, UUID refId, String refNum,
                                        LocalDate date, String result) {
        QualityInspection qi = new QualityInspection(); qi.setTenantId(T);
        qi.setInspectionNumber(num); qi.setInspectionType(type);
        qi.setProductId(prod.getId()); qi.setProductName(prod.getName());
        qi.setReferenceId(refId); qi.setReferenceNumber(refNum);
        qi.setInspectionDate(date); qi.setInspectorName("QA Team");
        qi.setResult(result); qi.setStatus("COMPLETED"); qi.setSampleSize(10);
        qi.setDefectsFound(result.equals("PASS") ? 0 : 2);
        qi.setAcceptanceCriteria("AQL 2.5 — defects < 5%");
        qualityInspectionRepo.save(qi);
    }

    private void saveQualityRejection(String num, UUID batchId, String batchNum, Product prod,
                                       LocalDate date, int qty, String reason, String disposition) {
        QualityRejection qr = new QualityRejection(); qr.setTenantId(T);
        qr.setRejectionNumber(num); qr.setBatchId(batchId); qr.setBatchNumber(batchNum);
        qr.setProduct(prod); qr.setRejectionDate(date);
        qr.setQuantity(BigDecimal.valueOf(qty)); qr.setUnit("PCS");
        qr.setRejectionReason(reason);
        qr.setDisposition(QualityRejection.Disposition.valueOf(disposition));
        qr.setStatus(QualityRejection.RejectionStatus.OPEN);
        qr.setCorrectiveAction("Under investigation");
        qualityRejectionRepo.save(qr);
    }

    private void saveAccountingPeriod(int year, int month, String name, String status) {
        AccountingPeriod ap = new AccountingPeriod(); ap.setTenantId(T);
        ap.setPeriodYear(year); ap.setPeriodMonth(month); ap.setName(name);
        ap.setStatus(AccountingPeriod.PeriodStatus.valueOf(status));
        accountingPeriodRepo.save(ap);
    }

    private BankAccount saveBankAccount(String name, String accNum, String bank, String ifsc, String branch, double balance) {
        BankAccount ba = new BankAccount(); ba.setTenantId(T);
        ba.setAccountName(name); ba.setAccountNumber(accNum); ba.setBankName(bank);
        ba.setIfscCode(ifsc); ba.setBranch(branch);
        ba.setOpeningBalance(BigDecimal.valueOf(balance)); ba.setCurrentBalance(BigDecimal.valueOf(balance));
        ba.setCurrency("INR"); ba.setActive(true); return bankAccountRepo.save(ba);
    }

    private void saveBankReconciliation(BankAccount bank, LocalDate date, double stmtBal, double sysBal, String status) {
        BankReconciliation br = new BankReconciliation(); br.setTenantId(T);
        br.setBankAccount(bank); br.setStatementDate(date);
        br.setStatementBalance(BigDecimal.valueOf(stmtBal));
        br.setSystemBalance(BigDecimal.valueOf(sysBal));
        br.setDifference(BigDecimal.valueOf(stmtBal - sysBal));
        br.setStatus(BankReconciliation.ReconciliationStatus.valueOf(status));
        bankReconRepo.save(br);
    }

    private CostCenter saveCostCenter(String name, String code, String type, double budget, CostCenter parent) {
        CostCenter cc = new CostCenter(); cc.setTenantId(T);
        cc.setName(name); cc.setCode(code); cc.setType(CostCenter.CostCenterType.valueOf(type));
        cc.setBudgetAmount(BigDecimal.valueOf(budget)); cc.setActualAmount(BigDecimal.ZERO);
        cc.setParent(parent); cc.setActive(true); return costCenterRepo.save(cc);
    }

    private AssetGroup saveAssetGroup(String name, String code, String method, int life, double rate) {
        AssetGroup ag = new AssetGroup(); ag.setTenantId(T);
        ag.setName(name); ag.setCode(code);
        ag.setDepreciationMethod(AssetGroup.DepreciationMethod.valueOf(method));
        ag.setUsefulLifeYears(life); ag.setDepreciationRate(BigDecimal.valueOf(rate)); ag.setActive(true);
        return assetGroupRepo.save(ag);
    }

    private void saveFixedAsset(String code, String name, AssetGroup group, LocalDate purchDate,
                                 double cost, double salvage, int life, String method, String location) {
        FixedAsset fa = new FixedAsset(); fa.setTenantId(T);
        fa.setAssetCode(code); fa.setName(name); fa.setAssetGroup(group);
        fa.setPurchaseDate(purchDate); fa.setPurchaseCost(BigDecimal.valueOf(cost));
        fa.setSalvageValue(BigDecimal.valueOf(salvage)); fa.setUsefulLifeYears(life);
        fa.setDepreciationMethod(AssetGroup.DepreciationMethod.valueOf(method));
        fa.setDepreciationRate(BigDecimal.valueOf(100.0 / life));
        fa.setCurrentBookValue(BigDecimal.valueOf(cost));
        fa.setAccumulatedDepreciation(BigDecimal.ZERO);
        fa.setLocation(location); fa.setStatus(FixedAsset.AssetStatus.ACTIVE); fa.setActive(true);
        fixedAssetRepo.save(fa);
    }

    private void savePaymentTerm(String name, String code, int due, int discDays, BigDecimal discPct,
                                  int penaltyDays, BigDecimal penaltyPct) {
        PaymentTerm pt = new PaymentTerm(); pt.setTenantId(T);
        pt.setName(name); pt.setCode(code); pt.setDueDays(due);
        pt.setDiscountDays(discDays); pt.setDiscountPercent(discPct);
        pt.setHasEarlyPaymentDiscount(discDays > 0);
        pt.setPenaltyAfterDays(penaltyDays); pt.setPenaltyPercent(penaltyPct); pt.setActive(true);
        paymentTermRepo.save(pt);
    }

    private void saveEarlyPayDiscount(UUID invId, String invNum, UUID custId, String custName,
                                       LocalDate invDate, LocalDate eligibleUntil, double invAmt, double discPct) {
        EarlyPaymentDiscount epd = new EarlyPaymentDiscount(); epd.setTenantId(T);
        epd.setInvoiceId(invId); epd.setInvoiceNumber(invNum);
        epd.setCustomerId(custId); epd.setCustomerName(custName);
        epd.setInvoiceDate(invDate); epd.setDiscountEligibleUntil(eligibleUntil);
        epd.setInvoiceAmount(BigDecimal.valueOf(invAmt));
        epd.setDiscountPercent(BigDecimal.valueOf(discPct));
        epd.setDiscountAmount(BigDecimal.valueOf(invAmt * discPct / 100));
        epd.setApplied(false);
        earlyPayDiscountRepo.save(epd);
    }

    private void saveJournalEntry(String num, LocalDate date, String desc,
                                   String drCode, String drName, double dr1, double cr1,
                                   String crCode, String crName, double dr2, double cr2) {
        JournalEntry je = new JournalEntry(); je.setTenantId(T);
        je.setEntryNumber(num); je.setEntryDate(date); je.setDescription(desc);
        je.setStatus(JournalEntry.JEStatus.POSTED);
        JournalEntryLine line1 = new JournalEntryLine(); line1.setJournalEntry(je);
        line1.setAccountCode(drCode); line1.setAccountName(drName);
        line1.setDebitAmount(BigDecimal.valueOf(dr1)); line1.setCreditAmount(BigDecimal.valueOf(cr1));
        JournalEntryLine line2 = new JournalEntryLine(); line2.setJournalEntry(je);
        line2.setAccountCode(crCode); line2.setAccountName(crName);
        line2.setDebitAmount(BigDecimal.valueOf(dr2)); line2.setCreditAmount(BigDecimal.valueOf(cr2));
        je.setLines(new java.util.ArrayList<>(java.util.List.of(line1, line2)));
        je.setTotalDebit(BigDecimal.valueOf(Math.max(dr1, dr2)));
        je.setTotalCredit(BigDecimal.valueOf(Math.max(cr1, cr2)));
        journalEntryRepo.save(je);
    }

    private LeaveType saveLeaveType(String name, String code, int days, boolean carry, boolean paid) {
        LeaveType lt = new LeaveType(); lt.setTenantId(T);
        lt.setName(name); lt.setCode(code); lt.setDaysAllowed(days);
        lt.setCarryForward(carry); lt.setPaid(paid); lt.setActive(true);
        return leaveTypeRepo.save(lt);
    }

    private Employee saveEmployee(String code, String first, String last, String email, String phone,
                                   String gender, LocalDate dob, String desig, Department dept,
                                   Branch branch, double basic) {
        Employee e = new Employee(); e.setTenantId(T);
        e.setEmployeeCode(code); e.setFirstName(first); e.setLastName(last);
        e.setEmail(email); e.setPhone(phone); e.setGender(gender); e.setDateOfBirth(dob);
        e.setDesignation(desig); e.setDepartmentId(dept.getId()); e.setBranchId(branch.getId());
        e.setJoiningDate(LocalDate.of(2023,1,1)); e.setStatus(Employee.EmployeeStatus.ACTIVE);
        e.setBasicSalary(BigDecimal.valueOf(basic)); e.setEmploymentType("PERMANENT");
        e.setCity("Hyderabad"); e.setState("Telangana"); e.setCountry("India");
        return employeeRepo.save(e);
    }

    private void saveLeaveApp(UUID empId, LeaveType lt, LocalDate from, LocalDate to, int days, String reason) {
        LeaveApplication la = new LeaveApplication(); la.setTenantId(T);
        la.setEmployeeId(empId); la.setLeaveTypeId(lt.getId()); la.setLeaveType(lt.getName());
        la.setFromDate(from); la.setToDate(to); la.setDays(days); la.setReason(reason);
        la.setStatus(LeaveApplication.LeaveStatus.PENDING);
        leaveAppRepo.save(la);
    }

    private void saveAttendance(UUID empId, LocalDate date, LocalTime in, LocalTime out, String status) {
        Attendance a = new Attendance(); a.setTenantId(T);
        a.setEmployeeId(empId); a.setDate(date); a.setCheckIn(in); a.setCheckOut(out);
        a.setStatus(status); a.setWorkingHours(9.0);
        attendanceRepo.save(a);
    }

    record PL(SalaryComponent comp, double amount, String type) {}
    private PL pLine(SalaryComponent c, double amt, String type) { return new PL(c, amt, type); }

    private SalaryComponent saveSalaryComponent(String name, String code, String type, boolean isPct,
                                                  double val, String base, boolean taxable) {
        SalaryComponent sc = new SalaryComponent(); sc.setTenantId(T);
        sc.setName(name); sc.setCode(code); sc.setType(type); sc.setCalculationType(isPct ? "PERCENTAGE" : "FIXED");
        sc.setValue(val); sc.setCalculationBase(base); sc.setTaxable(taxable); sc.setActive(true);
        return salaryComponentRepo.save(sc);
    }

    private void savePayslip(String num, UUID empId, int month, int year, LocalDate payDate,
                               double basic, int workDays, int presentDays, List<PL> lines, String status) {
        Payslip ps = new Payslip(); ps.setTenantId(T);
        ps.setPayslipNumber(num); ps.setEmployeeId(empId);
        ps.setPayMonth(month); ps.setPayYear(year); ps.setPayDate(payDate);
        ps.setBasicSalary(BigDecimal.valueOf(basic)); ps.setWorkingDays(workDays); ps.setPresentDays(presentDays);
        ps.setLeaveDays(workDays - presentDays); ps.setStatus(Payslip.PayslipStatus.valueOf(status));
        ps.setPaymentMethod("BANK_TRANSFER");
        BigDecimal gross = BigDecimal.ZERO, deductions = BigDecimal.ZERO;
        List<PayslipLine> psLines = new ArrayList<>();
        for (PL l : lines) {
            PayslipLine pl = new PayslipLine(); pl.setPayslip(ps);
            pl.setComponentId(l.comp().getId()); pl.setComponentName(l.comp().getName());
            pl.setComponentType(l.type()); pl.setAmount(BigDecimal.valueOf(l.amount()));
            psLines.add(pl);
            if (l.type().equals("EARNING"))   gross      = gross.add(BigDecimal.valueOf(l.amount()));
            else                              deductions = deductions.add(BigDecimal.valueOf(l.amount()));
        }
        ps.setGrossEarnings(gross); ps.setTotalDeductions(deductions);
        ps.setNetSalary(gross.subtract(deductions)); ps.setLines(psLines);
        payslipRepo.save(ps);
    }

    private PriceList savePriceList(String name, String code, boolean isDefault) {
        PriceList pl = new PriceList(); pl.setTenantId(T);
        pl.setName(name); pl.setCode(code); pl.setCurrency("INR");
        pl.setDefault(isDefault); pl.setActive(true); return priceListRepo.save(pl);
    }

    private void savePriceListItem(PriceList pl, Product prod, double price, double minQty,
                                    double maxQty, double disc, LocalDate from, LocalDate to) {
        PriceListItem pli = new PriceListItem(); pli.setTenantId(T);
        pli.setPriceList(pl); pli.setProduct(prod); pli.setUnitPrice(BigDecimal.valueOf(price));
        pli.setMinQuantity(BigDecimal.valueOf(minQty)); pli.setMaxQuantity(BigDecimal.valueOf(maxQty));
        pli.setDiscountPercent(BigDecimal.valueOf(disc));
        pli.setEffectiveFrom(from); pli.setEffectiveTo(to); priceListItemRepo.save(pli);
    }

    private DiscountScheme saveDiscountScheme(String name, String code, String type, double discPct,
                                               String custType, LocalDate start, LocalDate end) {
        DiscountScheme ds = new DiscountScheme(); ds.setTenantId(T);
        ds.setName(name); ds.setCode(code);
        ds.setSchemeType(DiscountScheme.SchemeType.valueOf(type));
        ds.setDiscountPercent(BigDecimal.valueOf(discPct));
        ds.setCustomerType(custType); ds.setStartDate(start); ds.setEndDate(end); ds.setActive(true);
        return discountSchemeRepo.save(ds);
    }

    private void saveDiscountSlab(DiscountScheme scheme, double min, double max, double disc, double free) {
        DiscountSlab sl = new DiscountSlab(); sl.setTenantId(T);
        sl.setScheme(scheme); sl.setMinValue(BigDecimal.valueOf(min)); sl.setMaxValue(BigDecimal.valueOf(max));
        sl.setDiscountPercent(BigDecimal.valueOf(disc)); sl.setFreeQuantity(BigDecimal.valueOf(free));
        discountSlabRepo.save(sl);
    }

    private void saveWorkflowDefinition(String name, String module, double minAmt, double maxAmt) {
        WorkflowDefinition wd = new WorkflowDefinition(); wd.setTenantId(T);
        wd.setName(name); wd.setModule(module);
        wd.setMinAmount(BigDecimal.valueOf(minAmt)); wd.setMaxAmount(BigDecimal.valueOf(maxAmt));
        wd.setActive(true);
        workflowDefRepo.save(wd);
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
        if (status == Dispatch.DispatchStatus.DELIVERED) { d.setPodReceived(true); d.setPodDate(actDelivery); d.setPodReference("POD-" + num); }
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
        saveSupplierPerf(v1, "Q1-2026", LocalDate.of(2026,4,5), 92.0, 95.0, 88.0, 90.0, 45, 42, 3, 1, 44, "Excellent overall performance");
        saveSupplierPerf(v2, "Q1-2026", LocalDate.of(2026,4,6), 78.0, 82.0, 75.0, 85.0, 30, 23, 7, 3, 27, "Delivery delays in March — needs improvement");
        saveSupplierPerf(v3, "Q1-2026", LocalDate.of(2026,4,7), 98.0, 99.0, 97.0, 88.0, 12, 12, 0, 0, 12, "Outstanding service quality");
        saveSupplierPerf(v1, "Q4-2025", LocalDate.of(2026,1,8), 88.0, 90.0, 85.0, 87.0, 40, 35, 5, 2, 38, "Good performance last quarter");
        saveSupplierPerf(v2, "Q4-2025", LocalDate.of(2026,1,9), 55.0, 60.0, 65.0, 70.0, 25, 14, 11, 6, 19, "Significant quality issues in December");
    }

    private void saveSupplierPerf(Vendor v, String period, LocalDate evalDate,
                                   double otd, double qual, double resp, double price,
                                   int total, int onTime, int late, int rejected, int accepted, String remarks) {
        SupplierPerformance sp = new SupplierPerformance(); sp.setTenantId(T);
        sp.setVendorId(v.getId()); sp.setVendorName(v.getName());
        sp.setEvaluationPeriod(period); sp.setEvaluationDate(evalDate);
        sp.setOnTimeDeliveryScore(BigDecimal.valueOf(otd)); sp.setQualityScore(BigDecimal.valueOf(qual));
        sp.setResponseScore(BigDecimal.valueOf(resp)); sp.setPricingScore(BigDecimal.valueOf(price));
        BigDecimal overall = BigDecimal.valueOf((otd + qual + resp + price) / 4.0).setScale(2, java.math.RoundingMode.HALF_UP);
        sp.setOverallScore(overall);
        int score = overall.intValue();
        if (score >= 80) sp.setStatus(SupplierPerformance.PerfStatus.PREFERRED);
        else if (score >= 60) sp.setStatus(SupplierPerformance.PerfStatus.APPROVED);
        else if (score >= 40) sp.setStatus(SupplierPerformance.PerfStatus.CONDITIONAL);
        else sp.setStatus(SupplierPerformance.PerfStatus.BLACKLISTED);
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
        if (status == SupplierNonConformance.NCStatus.VENDOR_RESPONDED || status == SupplierNonConformance.NCStatus.CLOSED)
            nc.setVendorResponse("Acknowledged — corrective actions in progress");
        supplierNcRepo.save(nc);
    }

    private void seedDimensions() {
        Dimension dimBU = saveDimension("Business Unit", "BU", "Segment by business unit", false);
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
                pc.setClosedAt(LocalDateTime.of(Integer.parseInt(p[0]), Integer.parseInt(p[1])+1 > 12 ? 1 : Integer.parseInt(p[1])+1, 5, 18, 0));
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
        StorageLocation locA01 = saveStorageLocation(wh1, "A-01-01", "Aisle A Rack 1 Bin 1", StorageLocation.LocationType.BIN, "A", "01", "01", 500.0);
        StorageLocation locA01b= saveStorageLocation(wh1, "A-01-02", "Aisle A Rack 1 Bin 2", StorageLocation.LocationType.BIN, "A", "01", "02", 500.0);
        StorageLocation locB01 = saveStorageLocation(wh1, "B-01-01", "Aisle B Rack 1 Bin 1", StorageLocation.LocationType.BIN, "B", "01", "01", 1000.0);
        StorageLocation locCold = saveStorageLocation(wh1, "COLD-01", "Cold Storage Zone 1",  StorageLocation.LocationType.COLD_STORAGE, null, null, "01", 200.0);
        StorageLocation locDsp  = saveStorageLocation(wh1, "DISP-01", "Dispatch Staging Area", StorageLocation.LocationType.DISPATCH_AREA, null, null, null, 2000.0);
        saveStorageLocation(wh2, "M-A-01", "Mumbai Rack A Bin 1",  StorageLocation.LocationType.BIN, "A", "01", null, 300.0);
        saveStorageLocation(wh2, "M-B-01", "Mumbai Rack B Bin 1",  StorageLocation.LocationType.BIN, "B", "01", null, 300.0);

        // Seed some location stock
        saveLocationStock(locA01, wh1, p1, "LOT-20260401-001", 250.0, 45.0);
        saveLocationStock(locA01b, wh1, p2, "LOT-20260401-002", 120.0, 350.0);
        saveLocationStock(locB01,  wh1, p3, "LOT-20260501-003", 800.0, 55.0);
        saveLocationStock(locCold, wh1, p1, "LOT-20260501-001", 100.0, 45.0);
    }

    private StorageLocation saveStorageLocation(Warehouse wh, String code, String name,
                                                 StorageLocation.LocationType type,
                                                 String aisle, String rack, String bin, double cap) {
        StorageLocation loc = new StorageLocation(); loc.setTenantId(T);
        loc.setWarehouseId(wh.getId()); loc.setWarehouseName(wh.getName());
        loc.setCode(code); loc.setName(name); loc.setLocationType(type);
        loc.setAisle(aisle); loc.setRack(rack); loc.setBin(bin);
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
        storageLocationRepo.save(loc);
    }

    // ══════════════════════════ Phase 3 Seed Methods ══════════════════════════

    private void seedUoM() {
        UnitOfMeasure uomKg  = saveUoM("KG",  "Kilogram",         "kg",  UnitOfMeasure.UoMType.WEIGHT,   true,  3);
        UnitOfMeasure uomGm  = saveUoM("GM",  "Gram",             "g",   UnitOfMeasure.UoMType.WEIGHT,   false, 3);
        UnitOfMeasure uomMt  = saveUoM("MT",  "Metric Tonne",     "MT",  UnitOfMeasure.UoMType.WEIGHT,   false, 3);
        UnitOfMeasure uomPcs = saveUoM("PCS", "Pieces",           "pcs", UnitOfMeasure.UoMType.QUANTITY,  true,  0);
        UnitOfMeasure uomBox = saveUoM("BOX", "Box",              "box", UnitOfMeasure.UoMType.QUANTITY,  false, 0);
        UnitOfMeasure uomDzn = saveUoM("DZN", "Dozen",            "dz",  UnitOfMeasure.UoMType.QUANTITY,  false, 0);
        UnitOfMeasure uomLtr = saveUoM("LTR", "Litre",            "L",   UnitOfMeasure.UoMType.VOLUME,    true,  3);
        UnitOfMeasure uomMl  = saveUoM("ML",  "Millilitre",       "mL",  UnitOfMeasure.UoMType.VOLUME,    false, 3);
        UnitOfMeasure uomMtr = saveUoM("MTR", "Metre",            "m",   UnitOfMeasure.UoMType.LENGTH,    true,  3);
        UnitOfMeasure uomCm  = saveUoM("CM",  "Centimetre",       "cm",  UnitOfMeasure.UoMType.LENGTH,    false, 2);
        UnitOfMeasure uomHr  = saveUoM("HRS", "Hours",            "hr",  UnitOfMeasure.UoMType.TIME,      true,  2);
        UnitOfMeasure uomDay = saveUoM("DAY", "Day",              "day", UnitOfMeasure.UoMType.TIME,      false, 0);

        // Conversions
        saveUoMConversion(uomKg,  uomGm,  1000.0);
        saveUoMConversion(uomMt,  uomKg,  1000.0);
        saveUoMConversion(uomDzn, uomPcs, 12.0);
        saveUoMConversion(uomBox, uomPcs, 24.0);
        saveUoMConversion(uomLtr, uomMl,  1000.0);
        saveUoMConversion(uomMtr, uomCm,  100.0);
        saveUoMConversion(uomDay, uomHr,  8.0);
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
        // Reverse
        UoMConversion rev = new UoMConversion(); rev.setTenantId(T);
        rev.setFromUomId(to.getId()); rev.setFromUomCode(to.getCode());
        rev.setToUomId(from.getId()); rev.setToUomCode(from.getCode());
        rev.setConversionFactor(BigDecimal.ONE.divide(BigDecimal.valueOf(factor), 8, java.math.RoundingMode.HALF_UP));
        rev.setBidirectional(true); rev.setActive(true);
        uomConversionRepo.save(rev);
    }

    private void seedBrandsAndProductLines() {
        Brand bGlobal = saveBrand("GlobalMake", "GLB", "India", "Domestic manufacturing brand");
        Brand bPremium= saveBrand("PremiumTech","PMT", "Germany", "Premium imported components");
        Brand bEco    = saveBrand("EcoLine",    "ECO", "India", "Eco-friendly product range");

        saveProductLine("WD-SERIES", "Widget Series",       bGlobal,  "Industrial Equipment", "B2B Manufacturing");
        saveProductLine("PC-SERIES", "Precision Components",bPremium, "Electronic Components","OEM Manufacturers");
        saveProductLine("ECO-PKG",   "Eco Packaging",       bEco,     "Packaging",            "FMCG Companies");
        saveProductLine("IT-INFRA",  "IT Infrastructure",   bGlobal,  "IT Equipment",         "Corporate Offices");
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
        if (qty != null) ob.setQuantity(BigDecimal.valueOf(qty));
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
        t1.add(makeSalesPlanTarget(sp1, p1, 10000.0, 2500000.0, 4200.0, 1050000.0, "PCS"));
        t1.add(makeSalesPlanTarget(sp1, p2,  5000.0, 2600000.0, 1950.0,  975000.0, "PCS"));
        t1.add(makeSalesPlanTarget(sp1, p3,   500.0,  875000.0,  210.0,  367500.0, "PCS"));
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
        t2.add(makeSalesPlanTarget(sp2, p1, 2000.0, 500000.0, 0.0, 0.0, "PCS"));
        t2.add(makeSalesPlanTarget(sp2, p2,  800.0, 416000.0, 0.0, 0.0, "PCS"));
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
        saveDemandForecast("DF-2026-001", p1, "2026-Q2", 2026, null, 2, DemandForecast.ForecastMethod.HISTORICAL, 3500.0, 3420.0, "PCS", 85, wh, DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-002", p2, "2026-Q2", 2026, null, 2, DemandForecast.ForecastMethod.HISTORICAL, 1400.0, 1380.0, "PCS", 82, wh, DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-003", p3, "2026-07",  2026, 7,  null, DemandForecast.ForecastMethod.MOVING_AVERAGE, 800.0, 0.0, "KG", 70, wh, DemandForecast.ForecastStatus.PUBLISHED);
        saveDemandForecast("DF-2026-004", p4, "2026-07",  2026, 7,  null, DemandForecast.ForecastMethod.MANUAL, 600.0, 0.0, "PCS", 60, wh, DemandForecast.ForecastStatus.DRAFT);
        saveDemandForecast("DF-2026-005", p1, "2026-Q3", 2026, null, 3, DemandForecast.ForecastMethod.EXPONENTIAL_SMOOTHING, 3800.0, 0.0, "PCS", 75, wh, DemandForecast.ForecastStatus.DRAFT);
    }

    private void saveDemandForecast(String num, Product prod, String period, int year, Integer month, Integer quarter,
                                     DemandForecast.ForecastMethod method, double fQty, double aQty,
                                     String unit, int confidence, Warehouse wh, DemandForecast.ForecastStatus status) {
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
                "HDFC Current Account", "UTR2026041500123", SupplierPayment.PayStatus.RECONCILED,
                "Rajesh Kumar", LocalDateTime.of(2026,4,15,10,30));
        saveSupplierPayment("SPAY-2026-002", v2, "PINV-2026-002", LocalDate.of(2026,5,10),
                SupplierPayment.PaymentMethod.RTGS, 195000.0, 1950.0, 1.0,
                "HDFC Current Account", "RTGS20260510ABC45", SupplierPayment.PayStatus.PROCESSED,
                "Finance Manager", LocalDateTime.of(2026,5,10,14,0));
        saveSupplierPayment("SPAY-2026-003", v1, "PINV-2026-003", LocalDate.of(2026,6,5),
                SupplierPayment.PaymentMethod.CHEQUE, 280000.0, 2800.0, 1.0,
                "HDFC Current Account", "CHQ-004521", SupplierPayment.PayStatus.APPROVED,
                "Finance Manager", LocalDateTime.of(2026,6,5,11,0));
        saveSupplierPayment("SPAY-2026-004", v2, null, LocalDate.of(2026,6,20),
                SupplierPayment.PaymentMethod.BANK_TRANSFER, 150000.0, 0.0, 0.0,
                "HDFC Current Account", null, SupplierPayment.PayStatus.DRAFT,
                null, null);
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
        }
    }

    private void seedApprovalRules() {
        saveApprovalRule("PO Approval — High Value", "PURCHASE_ORDER", "amount",
                ApprovalRule.Operator.GREATER_THAN, "100000", null,
                ApprovalRule.ApproverType.DEPARTMENT_HEAD, "Purchase Head", 1, 3, "CEO");
        saveApprovalRule("PO Approval — Very High Value", "PURCHASE_ORDER", "amount",
                ApprovalRule.Operator.GREATER_THAN, "500000", null,
                ApprovalRule.ApproverType.CEO, "CEO", 2, 2, "Board");
        saveApprovalRule("Invoice Approval", "INVOICE", "amount",
                ApprovalRule.Operator.GREATER_THAN, "50000", null,
                ApprovalRule.ApproverType.MANAGER, "Finance Manager", 1, 5, "CFO");
        saveApprovalRule("Leave Approval", "LEAVE", "days",
                ApprovalRule.Operator.GREATER_THAN, "3", null,
                ApprovalRule.ApproverType.DEPARTMENT_HEAD, "HR Head", 1, 1, "HR Director");
        saveApprovalRule("Payment Approval", "PAYMENT", "amount",
                ApprovalRule.Operator.GREATER_THAN, "200000", null,
                ApprovalRule.ApproverType.CEO, "CEO", 1, 2, "Board");
        saveApprovalRule("Expense Approval — Mid Range", "EXPENSE", "amount",
                ApprovalRule.Operator.BETWEEN, "5000", "50000",
                ApprovalRule.ApproverType.MANAGER, "Department Manager", 1, 3, "Finance Head");
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
        saveReportDefinition("RPT-2026-001", "Sales Summary by Customer",
                ReportDefinition.ReportCategory.SALES, "sales_orders",
                "[{\"field\":\"customerName\",\"label\":\"Customer\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"totalAmount\",\"label\":\"Total\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"status\",\"label\":\"Status\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[{\"field\":\"orderDate\",\"operator\":\"BETWEEN\",\"value\":\"2026-04-01\"}]",
                "customerName", "ASC", "customerName", true, "admin");
        saveReportDefinition("RPT-2026-002", "Purchase Order Aging Report",
                ReportDefinition.ReportCategory.PURCHASE, "purchase_orders",
                "[{\"field\":\"vendorName\",\"label\":\"Vendor\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"poNumber\",\"label\":\"PO Number\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"totalAmount\",\"label\":\"Amount\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"status\",\"label\":\"Status\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[{\"field\":\"status\",\"operator\":\"NOT_EQUALS\",\"value\":\"RECEIVED\"}]",
                "createdAt", "ASC", null, true, "admin");
        saveReportDefinition("RPT-2026-003", "Inventory Stock Valuation",
                ReportDefinition.ReportCategory.INVENTORY, "stock_items",
                "[{\"field\":\"productName\",\"label\":\"Product\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"warehouseId\",\"label\":\"Warehouse\",\"dataType\":\"UUID\",\"visible\":true},{\"field\":\"quantityOnHand\",\"label\":\"Qty\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"averageCost\",\"label\":\"Avg Cost\",\"dataType\":\"DECIMAL\",\"visible\":true}]",
                "[]", "productName", "ASC", null, true, "admin");
        saveReportDefinition("RPT-2026-004", "Employee Leave Balance Summary",
                ReportDefinition.ReportCategory.HR, "employees",
                "[{\"field\":\"fullName\",\"label\":\"Employee\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"department\",\"label\":\"Dept\",\"dataType\":\"STRING\",\"visible\":true}]",
                "[]", "fullName", "ASC", "department", false, "hr.manager");
        saveReportDefinition("RPT-2026-005", "Monthly P&L Summary",
                ReportDefinition.ReportCategory.FINANCIAL, "journal_entries",
                "[{\"field\":\"accountCode\",\"label\":\"Account\",\"dataType\":\"STRING\",\"visible\":true},{\"field\":\"debitAmount\",\"label\":\"Debit\",\"dataType\":\"DECIMAL\",\"visible\":true},{\"field\":\"creditAmount\",\"label\":\"Credit\",\"dataType\":\"DECIMAL\",\"visible\":true}]",
                "[{\"field\":\"entryDate\",\"operator\":\"BETWEEN\",\"value\":\"2026-04-01\"}]",
                "accountCode", "ASC", "accountCode", true, "admin");
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
