package com.erp.platform.config;

import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates the roles the application offers but never made.
 *
 * <p>The screens offer nineteen roles — they are listed for a user group's allowed roles, on the
 * role hierarchy, and in the role picker — and only five of them existed. Choosing any of the other
 * fourteen was accepted everywhere it was offered and then failed at the one point it mattered, when
 * the role was actually assigned to somebody: "Role not found: PURCHASE_MANAGER". A group could be
 * set up entirely out of roles that could never be given to anyone, and nothing said so until a user
 * was saved against it.
 *
 * <p>What each of these roles can reach is already decided: every one of them has an entry in the
 * front end's module map, which is what a role with no explicit module list falls back to. So they
 * only needed creating; there is no access to define here, and defining any would override what is
 * already there.
 *
 * <p>Roles are not tenant-scoped — they are looked up by name alone — so these are created once and
 * shared, like the ones the initialiser already makes.
 *
 * <p>Runs on every start and creates only what is absent, so it repairs databases that already
 * exist rather than only fresh ones.
 */
@Component
@Order(11)
@RequiredArgsConstructor
@Slf4j
public class MissingRoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    /** Name to description, worded as the screens already describe them. */
    private static final Map<String, String> EXPECTED = new LinkedHashMap<>();
    static {
        EXPECTED.put("TENANT_ADMIN",          "Tenant administrator");
        EXPECTED.put("PRODUCTION_MANAGER",    "Processing, Quality, Inventory, Reports");
        EXPECTED.put("PRODUCTION_SUPERVISOR", "Processing, Inventory");
        EXPECTED.put("SALES_MANAGER",         "CRM, Sales, Reports");
        EXPECTED.put("SALES_EXECUTIVE",       "CRM, Sales");
        EXPECTED.put("PURCHASE_MANAGER",      "Purchase, Inventory, Reports");
        EXPECTED.put("PURCHASE_EXECUTIVE",    "Purchase, Inventory");
        EXPECTED.put("ACCOUNT_MANAGER",       "Accounting, HR, Payroll, Reports");
        EXPECTED.put("AR_ACCOUNTANT",         "Sales, Accounting, Reports");
        EXPECTED.put("AP_ACCOUNTANT",         "Purchase, Accounting, Reports");
        EXPECTED.put("WAREHOUSE_MANAGER",     "Inventory, Reports");
        EXPECTED.put("WAREHOUSE_STAFF",       "Inventory");
        EXPECTED.put("HR_MANAGER",            "Dashboard, HR, Payroll");
        EXPECTED.put("READ_ONLY",             "Read-only access to all modules");
    }

    /**
     * Roles the initialiser does make, but leaves unmarked.
     *
     * <p>Roles are listed as "system, or belonging to this tenant". These are neither: they were
     * saved without the system flag and without a tenant, so no tenant has ever been able to see
     * them on the roles screen. They could still be assigned, because assignment looks a role up by
     * name, which is why nobody noticed — the list of roles a tenant could see simply had seven of
     * them missing.
     */
    private static final Map<String, String> ALREADY_MADE = new LinkedHashMap<>();
    static {
        ALREADY_MADE.put("ADMIN",      "Full system access");
        ALREADY_MADE.put("MANAGER",    "All modules except Settings");
        ALREADY_MADE.put("ACCOUNTANT", "Accounting, Payroll");
        ALREADY_MADE.put("SALES",      "CRM, Sales");
        ALREADY_MADE.put("PURCHASE",   "Purchase, Inventory");
        ALREADY_MADE.put("HR",         "Dashboard, HR, Payroll");
        ALREADY_MADE.put("STAFF",      "Dashboard, Master Data");
    }

    @Override
    public void run(String... args) {
        int created = 0;
        for (Map.Entry<String, String> e : EXPECTED.entrySet()) {
            if (roleRepository.findByName(e.getKey()).isPresent()) continue;
            Role role = new Role();
            role.setName(e.getKey());
            role.setDescription(e.getValue());
            // Left without an explicit module list on purpose: that is what marks it a system role,
            // and a system role takes its access from the map the front end already defines. Setting
            // one here would silently override that.
            role.setSystem(true);
            roleRepository.save(role);
            created++;
            log.info("Created missing role {}", e.getKey());
        }

        int marked = 0;
        for (Map.Entry<String, String> e : ALREADY_MADE.entrySet()) {
            var existing = roleRepository.findByName(e.getKey()).orElse(null);
            if (existing == null || existing.isSystem()) continue;
            existing.setSystem(true);
            if (existing.getDescription() == null || existing.getDescription().isBlank()) {
                existing.setDescription(e.getValue());
            }
            roleRepository.save(existing);
            marked++;
            log.info("Role {} is now visible on the roles screen", e.getKey());
        }

        if (created > 0) {
            log.info("Role check: created {} role(s) the application offers but had never made", created);
        }
        if (marked > 0) {
            log.info("Role check: {} existing role(s) were invisible to every tenant and now are not", marked);
        }
    }
}
