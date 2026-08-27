package com.erp.platform.config;

import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.AssetGroup;
import com.erp.platform.modules.accounting.entity.AssetGroup.DepreciationMethod;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.AssetGroupRepository;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
import com.erp.platform.modules.payroll.entity.SalaryComponent;
import com.erp.platform.modules.payroll.repository.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static com.erp.platform.config.DataInitializer.DEFAULT_TENANT_ID;

@Component
@Order(4)
@RequiredArgsConstructor
@Slf4j
public class MasterDataSeeder implements CommandLineRunner {

    private static final UUID T = DEFAULT_TENANT_ID;

    private final AccountRepository accountRepo;
    private final WarehouseRepository warehouseRepo;
    private final SalaryComponentRepository salaryComponentRepo;
    private final AssetGroupRepository assetGroupRepo;

    @Override
    public void run(String... args) {
        seedMissingGlAccounts();
        seedDefaultWarehouse();
        seedSalaryComponents();
        seedAssetGroups();
        log.info("MasterDataSeeder completed");
    }

    // ── GL Accounts ──────────────────────────────────────────────────────────

    private void seedMissingGlAccounts() {
        // Checked per-subType so it is safe to run on both fresh and existing databases.
        // Many services look up accounts by exact subType string; if the subType is absent the
        // journal entry is silently skipped. This seeder ensures every required subType exists.
        Object[][] accounts = {
            // code,   name,                               type,        subType
            {"1201", "Accounts Receivable (AR)",       "ASSET",     "ACCOUNTS_RECEIVABLE"},
            {"1122", "Bank / Cash for Expenses",       "ASSET",     "BANK_ACCOUNT"},
            {"1301", "Inventory Asset",                "ASSET",     "INVENTORY_ASSET"},
            {"1501", "Advance to Vendors",             "ASSET",     "ADVANCE_TO_VENDOR"},
            {"2101", "Accounts Payable (AP)",          "LIABILITY", "ACCOUNTS_PAYABLE"},
            {"2401", "Customer Advance Liability",     "LIABILITY", "CUSTOMER_ADVANCE_LIABILITY"},
            {"2211", "Provident Fund Payable",         "LIABILITY", "PF_PAYABLE"},
            {"2221", "ESI / ESIC Payable",             "LIABILITY", "ESI_PAYABLE"},
            {"4101", "Sales Revenue (AR)",             "INCOME",    "SALES_REVENUE"},
            {"5201", "Salary & Wages Expense",         "EXPENSE",   "SALARY_EXPENSE"},
            {"5221", "Purchase & Materials Expense",   "EXPENSE",   "PURCHASE_EXPENSE"},
            {"5231", "Provident Fund Expense",         "EXPENSE",   "PF_EXPENSE"},
            {"5241", "ESI / ESIC Expense",             "EXPENSE",   "ESI_EXPENSE"},
            {"5611", "Stock Variance",                 "EXPENSE",   "STOCK_VARIANCE"},
            {"4311", "Stock Gain",                     "INCOME",    "STOCK_GAIN"},
            {"5621", "Stock Loss",                     "EXPENSE",   "STOCK_LOSS"},
            {"5631", "Scrap / Write-off Expense",      "EXPENSE",   "SCRAP_EXPENSE"},
            {"5641", "Employee Expense Reimbursement", "EXPENSE",   "EMPLOYEE_EXPENSE"},
        };

        int created = 0;
        for (Object[] a : accounts) {
            String subType = (String) a[3];
            if (accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(T, subType).isEmpty()) {
                Account acc = new Account();
                acc.setTenantId(T);
                acc.setCode((String) a[0]);
                acc.setName((String) a[1]);
                acc.setType((String) a[2]);
                acc.setSubType(subType);
                acc.setSystem(true);
                acc.setBalance(BigDecimal.ZERO);
                accountRepo.save(acc);
                created++;
            }
        }
        if (created > 0) {
            log.info("MasterDataSeeder: created {} missing GL accounts", created);
        }
    }

    // ── Default Warehouse ────────────────────────────────────────────────────

    private void seedDefaultWarehouse() {
        if (warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T).isPresent()) return;
        Warehouse wh = new Warehouse();
        wh.setTenantId(T);
        wh.setName("Main Warehouse");
        wh.setCode("WH001");
        wh.setCity("Hyderabad");
        wh.setDefault(true);
        wh.setActive(true);
        warehouseRepo.save(wh);
        log.info("MasterDataSeeder: created default warehouse WH001");
    }

    // ── Salary Components ────────────────────────────────────────────────────

    private void seedSalaryComponents() {
        if (!salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T).isEmpty()) return;

        // name, code, type, isPercentage, value, calculationBase
        Object[][] components = {
            {"Basic Salary",           "BASIC",   "EARNING",   false, 100.0,  "BASIC"},
            {"House Rent Allowance",   "HRA",     "EARNING",   true,  40.0,   "BASIC"},
            {"Travel Allowance",       "TA",      "EARNING",   false, 1500.0, "BASIC"},
            {"Provident Fund (EE)",    "PF_EE",   "DEDUCTION", true,  12.0,   "BASIC"},
            {"Professional Tax",       "PT",      "DEDUCTION", false, 200.0,  "BASIC"},
            {"ESIC (EE)",              "ESIC_EE", "DEDUCTION", true,  0.75,   "GROSS"},
            {"PF Employer (ER)",       "PF_ER",   "EARNING",   true,  12.0,   "BASIC"},
            {"ESIC Employer (ER)",     "ESIC_ER", "EARNING",   true,  3.25,   "GROSS"},
        };

        for (Object[] c : components) {
            SalaryComponent sc = new SalaryComponent();
            sc.setTenantId(T);
            sc.setName((String) c[0]);
            sc.setCode((String) c[1]);
            sc.setType((String) c[2]);
            sc.setCalculationType((boolean) c[3] ? "PERCENTAGE" : "FIXED");
            sc.setValue((double) c[4]);
            sc.setCalculationBase((String) c[5]);
            sc.setActive(true);
            salaryComponentRepo.save(sc);
        }
        log.info("MasterDataSeeder: created {} salary components", components.length);
    }

    // ── Asset Groups ─────────────────────────────────────────────────────────

    private void seedAssetGroups() {
        if (!assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true).isEmpty()) return;

        // name, code, depreciationMethod, usefulLifeYears, depreciationRate (%)
        Object[][] groups = {
            {"Plant & Machinery", "PM",   DepreciationMethod.STRAIGHT_LINE,     10, 10.0 },
            {"Computers & IT",    "IT",   DepreciationMethod.WRITTEN_DOWN_VALUE, 3,  33.33},
            {"Office Furniture",  "FURN", DepreciationMethod.STRAIGHT_LINE,      5,  20.0 },
            {"Vehicles",          "VEH",  DepreciationMethod.WRITTEN_DOWN_VALUE, 8,  15.0 },
            {"Buildings",         "BLDG", DepreciationMethod.STRAIGHT_LINE,      30, 3.33 },
        };

        for (Object[] g : groups) {
            AssetGroup ag = new AssetGroup();
            ag.setTenantId(T);
            ag.setName((String) g[0]);
            ag.setCode((String) g[1]);
            ag.setDepreciationMethod((DepreciationMethod) g[2]);
            ag.setUsefulLifeYears((int) g[3]);
            ag.setDepreciationRate(BigDecimal.valueOf((double) g[4]));
            ag.setActive(true);
            assetGroupRepo.save(ag);
        }
        log.info("MasterDataSeeder: created {} asset groups", groups.length);
    }
}
