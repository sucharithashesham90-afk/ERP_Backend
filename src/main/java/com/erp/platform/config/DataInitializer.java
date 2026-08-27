package com.erp.platform.config;

import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.auth.entity.Permission;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.repository.PermissionRepository;
import com.erp.platform.modules.auth.repository.RoleRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.modules.master.entity.ProductCategory;
import com.erp.platform.modules.master.entity.Tax;
import com.erp.platform.modules.master.repository.ProductCategoryRepository;
import com.erp.platform.modules.master.repository.TaxRepository;
import com.erp.platform.modules.organization.entity.Company;
import com.erp.platform.modules.organization.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
// Must run before TestDataSeeder (@Order(2)), which reads the taxes and categories created here.
// Without an explicit order this runner defaults to lowest precedence, so on an empty database the
// seeder ran first, found no taxes, and killed the boot on taxes.get(0). Existing databases never
// showed it because the rows were already there from a previous run.
@Order(1)
public class DataInitializer implements CommandLineRunner {

    // Default tenant UUID for the seeded data
    public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // Set RESET_ADMIN_EMAIL / RESET_ADMIN_PASSWORD env vars on Railway to force-reset admin credentials
    @Value("${RESET_ADMIN_EMAIL:}")
    private String resetAdminEmail;

    @Value("${RESET_ADMIN_PASSWORD:}")
    private String resetAdminPassword;

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;
    private final CompanyRepository companyRepo;
    private final TaxRepository taxRepo;
    private final ProductCategoryRepository categoryRepo;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedPermissions();
        seedRoles();
        seedUsers();
        resetAdminIfRequested();
        seedCompany();
        seedTaxes();
        seedCategories();
        seedChartOfAccounts();
        log.info("ERP Platform data initialization complete");
    }

    private void resetAdminIfRequested() {
        if (!StringUtils.hasText(resetAdminEmail) || !StringUtils.hasText(resetAdminPassword)) return;
        userRepo.findByEmail(resetAdminEmail).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(resetAdminPassword));
            user.setStatus(User.UserStatus.ACTIVE);
            userRepo.save(user);
            log.info("Admin password reset for: {}", resetAdminEmail);
        });
    }

    private void seedPermissions() {
        if (permRepo.count() > 0) return;
        String[] modules = {"DASHBOARD", "CUSTOMERS", "VENDORS", "PRODUCTS", "LEADS", "SALES",
                "INVOICES", "PURCHASE", "INVENTORY", "ACCOUNTING", "HR", "PAYROLL",
                "WORKFLOW", "MANUFACTURING", "SETTINGS"};
        String[] actions = {"READ", "WRITE", "DELETE", "APPROVE"};
        List<Permission> perms = new ArrayList<>();
        for (String module : modules) {
            for (String action : actions) {
                Permission p = new Permission();
                p.setName(module + "_" + action);
                p.setModuleKey(module);
                p.setAction(action);
                p.setDescription(action + " access for " + module);
                perms.add(p);
            }
        }
        permRepo.saveAll(perms);
        log.info("Seeded {} permissions", perms.size());
    }

    private void seedRoles() {
        if (roleRepo.count() > 0) return;
        List<Permission> allPerms = permRepo.findAll();
        Set<Permission> allPermsSet = new HashSet<>(allPerms);

        Role admin = new Role();
        admin.setName("ADMIN");
        admin.setDescription("System Administrator");
        admin.setPermissions(allPermsSet);
        roleRepo.save(admin);

        Set<Permission> managerPerms = new HashSet<>(allPerms.stream()
                .filter(p -> !p.getModuleKey().equals("SETTINGS")).toList());
        Role manager = new Role();
        manager.setName("MANAGER");
        manager.setDescription("Department Manager");
        manager.setPermissions(managerPerms);
        roleRepo.save(manager);

        Set<Permission> accPerms = new HashSet<>(allPerms.stream()
                .filter(p -> Set.of("ACCOUNTING", "INVOICES", "PURCHASE", "DASHBOARD").contains(p.getModuleKey()))
                .toList());
        Role accountant = new Role();
        accountant.setName("ACCOUNTANT");
        accountant.setDescription("Accounting Staff");
        accountant.setPermissions(accPerms);
        roleRepo.save(accountant);

        Set<Permission> salesPerms = new HashSet<>(allPerms.stream()
                .filter(p -> Set.of("CUSTOMERS", "LEADS", "SALES", "INVOICES", "PRODUCTS", "DASHBOARD")
                        .contains(p.getModuleKey()))
                .toList());
        Role sales = new Role();
        sales.setName("SALES");
        sales.setDescription("Sales Team");
        sales.setPermissions(salesPerms);
        roleRepo.save(sales);

        Set<Permission> purchPerms = new HashSet<>(allPerms.stream()
                .filter(p -> Set.of("VENDORS", "PURCHASE", "PRODUCTS", "INVENTORY", "DASHBOARD")
                        .contains(p.getModuleKey()))
                .toList());
        Role purchase = new Role();
        purchase.setName("PURCHASE");
        purchase.setDescription("Purchase Team");
        purchase.setPermissions(purchPerms);
        roleRepo.save(purchase);

        Set<Permission> hrPerms = new HashSet<>(allPerms.stream()
                .filter(p -> Set.of("HR", "PAYROLL", "DASHBOARD").contains(p.getModuleKey()))
                .toList());
        Role hr = new Role();
        hr.setName("HR");
        hr.setDescription("HR Staff");
        hr.setPermissions(hrPerms);
        roleRepo.save(hr);

        Set<Permission> staffPerms = new HashSet<>(allPerms.stream()
                .filter(p -> p.getAction().equals("READ")).toList());
        Role staff = new Role();
        staff.setName("STAFF");
        staff.setDescription("General Staff");
        staff.setPermissions(staffPerms);
        roleRepo.save(staff);

        log.info("Seeded 7 roles");
    }

    private void seedUsers() {
        Role adminRole = roleRepo.findByName("ADMIN").orElseThrow();
        String defaultPassword = "admin@123";

        userRepo.findByEmail("admin@erp.com").ifPresentOrElse(admin -> {
            // Always reset to default password and ACTIVE on every startup
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            if (!admin.getRoles().contains(adminRole)) {
                admin.setRoles(new java.util.HashSet<>(java.util.Set.of(adminRole)));
            }
            userRepo.save(admin);
            log.info("Admin credentials reset on startup: admin@erp.com / {}", defaultPassword);
        }, () -> {
            User admin = new User();
            admin.setTenantId(DEFAULT_TENANT_ID);
            admin.setFullName("System Admin");
            admin.setEmail("admin@erp.com");
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setPhone("9999999999");
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setRoles(new java.util.HashSet<>(java.util.Set.of(adminRole)));
            userRepo.save(admin);
            log.info("Created default admin: admin@erp.com / {}", defaultPassword);
        });
    }

    private void seedCompany() {
        if (companyRepo.count() > 0) return;
        Company c = new Company();
        c.setTenantId(DEFAULT_TENANT_ID);
        c.setName("Demo Company Pvt Ltd");
        c.setLegalName("Demo Company Private Limited");
        c.setEmail("info@democompany.com");
        c.setPhone("9000000000");
        c.setAddress("123 Business Park");
        c.setCity("Hyderabad");
        c.setState("Telangana");
        c.setCountry("India");
        c.setPostalCode("500001");
        c.setCurrency("INR");
        c.setIndustry("SERVICES");
        companyRepo.save(c);
        log.info("Seeded demo company");
    }

    private void seedTaxes() {
        if (taxRepo.count() > 0) return;
        List<Tax> taxes = List.of(
            tax("No Tax",   BigDecimal.ZERO),
            tax("GST 5%",   BigDecimal.valueOf(5)),
            tax("GST 12%",  BigDecimal.valueOf(12)),
            tax("GST 18%",  BigDecimal.valueOf(18)),
            tax("GST 28%",  BigDecimal.valueOf(28)),
            tax("IGST 18%", BigDecimal.valueOf(18))
        );
        taxRepo.saveAll(taxes);
        log.info("Seeded {} tax rates", taxes.size());
    }

    private void seedCategories() {
        if (categoryRepo.count() > 0) return;
        List<String> cats = List.of("Raw Materials", "Finished Goods", "Semi-Finished",
                "Consumables", "Spare Parts", "Services", "Electronics",
                "Furniture", "Stationery", "IT Equipment");
        cats.forEach(name -> {
            ProductCategory c = new ProductCategory();
            c.setTenantId(DEFAULT_TENANT_ID);
            c.setName(name);
            c.setCode(name.toUpperCase().replace(" ", "_").substring(0, Math.min(name.replace(" ", "_").length(), 20)));
            c.setActive(true);
            categoryRepo.save(c);
        });
        log.info("Seeded {} product categories", cats.size());
    }

    private void seedChartOfAccounts() {
        if (accountRepo.count() > 0) return;
        List<Object[]> accounts = List.of(
            new Object[]{"1000", "Assets",                  "ASSET",     "PARENT"},
            new Object[]{"1100", "Current Assets",          "ASSET",     "CURRENT_ASSET"},
            new Object[]{"1110", "Cash",                    "ASSET",     "CASH"},
            new Object[]{"1120", "Bank Account",            "ASSET",     "BANK"},
            new Object[]{"1200", "Accounts Receivable",     "ASSET",     "ACCOUNTS_RECEIVABLE"},
            new Object[]{"1300", "Inventory",               "ASSET",     "INVENTORY_ASSET"},
            new Object[]{"1400", "Fixed Assets",            "ASSET",     "FIXED_ASSET"},
            new Object[]{"2000", "Liabilities",             "LIABILITY", "PARENT"},
            new Object[]{"2100", "Accounts Payable",        "LIABILITY", "ACCOUNTS_PAYABLE"},
            new Object[]{"2200", "Tax Payable",             "LIABILITY", "TAX_PAYABLE"},
            new Object[]{"2300", "Loans & Borrowings",      "LIABILITY", "LOAN"},
            new Object[]{"3000", "Equity",                  "EQUITY",    "PARENT"},
            new Object[]{"3100", "Share Capital",           "EQUITY",    "CAPITAL"},
            new Object[]{"3200", "Retained Earnings",       "EQUITY",    "RETAINED"},
            new Object[]{"4000", "Income",                  "INCOME",    "PARENT"},
            new Object[]{"4100", "Sales Revenue",           "INCOME",    "SALES_REVENUE"},
            new Object[]{"4200", "Service Revenue",         "INCOME",    "SERVICE_REVENUE"},
            new Object[]{"4300", "Other Income",            "INCOME",    "OTHER_INCOME"},
            new Object[]{"5000", "Expenses",                "EXPENSE",   "PARENT"},
            new Object[]{"5100", "Cost of Goods Sold",      "EXPENSE",   "COGS"},
            new Object[]{"5200", "Salaries & Wages",        "EXPENSE",   "SALARY_EXPENSE"},
            new Object[]{"5300", "Rent & Utilities",        "EXPENSE",   "OVERHEAD"},
            new Object[]{"5400", "Marketing & Advertising", "EXPENSE",   "MARKETING"},
            new Object[]{"5500", "Depreciation",            "EXPENSE",   "DEPRECIATION"},
            new Object[]{"5600", "Miscellaneous Expenses",  "EXPENSE",   "MISC"}
        );
        accounts.forEach(a -> {
            Account acc = new Account();
            acc.setTenantId(DEFAULT_TENANT_ID);
            acc.setCode((String) a[0]);
            acc.setName((String) a[1]);
            acc.setType((String) a[2]);
            acc.setSubType((String) a[3]);
            acc.setSystem(true);
            acc.setBalance(BigDecimal.ZERO);
            accountRepo.save(acc);
        });
        log.info("Seeded {} chart of accounts", accounts.size());
    }

    private Tax tax(String name, BigDecimal rate) {
        Tax t = new Tax();
        t.setTenantId(DEFAULT_TENANT_ID);
        t.setName(name);
        t.setTaxType(Tax.TaxType.PERCENTAGE);
        t.setRate(rate);
        t.setActive(true);
        return t;
    }
}
