package com.erp.platform.common;

import com.erp.platform.modules.auth.dto.LoginRequest;
import com.erp.platform.modules.auth.dto.RegisterRequest;
import com.erp.platform.modules.auth.entity.Permission;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.entity.Customer.CustomerCategory;
import com.erp.platform.modules.master.entity.Tax;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.entity.Vendor.VendorCategory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fluent test data builders to eliminate boilerplate in unit tests.
 */
public final class TestDataBuilder {

    public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID DEFAULT_USER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private TestDataBuilder() {}

    // ─── Customer ────────────────────────────────────────────────────────────

    public static CustomerBuilder customer() { return new CustomerBuilder(); }

    public static class CustomerBuilder {
        private UUID id         = UUID.randomUUID();
        private UUID tenantId   = DEFAULT_TENANT_ID;
        private String name     = "Test Customer";
        private String code     = "CUST-001";
        private String email    = "test@customer.com";
        private String phone    = "9000000000";
        private CustomerCategory category = CustomerCategory.RETAIL;
        private boolean active  = true;

        public CustomerBuilder id(UUID id)                     { this.id = id; return this; }
        public CustomerBuilder tenantId(UUID t)                { this.tenantId = t; return this; }
        public CustomerBuilder name(String n)                  { this.name = n; return this; }
        public CustomerBuilder email(String e)                 { this.email = e; return this; }
        public CustomerBuilder category(CustomerCategory c)    { this.category = c; return this; }
        public CustomerBuilder active(boolean a)               { this.active = a; return this; }

        public Customer build() {
            Customer c = new Customer();
            c.setId(id);
            c.setTenantId(tenantId);
            c.setName(name);
            c.setCode(code);
            c.setEmail(email);
            c.setPhone(phone);
            c.setCategory(category);
            c.setActive(active);
            c.setCreditLimit(BigDecimal.valueOf(10000));
            c.setOutstandingBalance(BigDecimal.ZERO);
            c.setPaymentTermsDays(30);
            return c;
        }
    }

    public static CreateCustomerRequest createCustomerRequest() {
        CreateCustomerRequest r = new CreateCustomerRequest();
        r.setName("New Customer");
        r.setEmail("new@customer.com");
        r.setPhone("9111111111");
        r.setCategory(CustomerCategory.RETAIL);
        r.setCreditLimit(BigDecimal.ZERO);
        return r;
    }

    // ─── Vendor ──────────────────────────────────────────────────────────────

    public static VendorBuilder vendor() { return new VendorBuilder(); }

    public static class VendorBuilder {
        private UUID id       = UUID.randomUUID();
        private UUID tenantId = DEFAULT_TENANT_ID;
        private String name   = "Test Vendor";
        private String email  = "test@vendor.com";
        private VendorCategory category = VendorCategory.SUPPLIER;

        public VendorBuilder id(UUID id)                   { this.id = id; return this; }
        public VendorBuilder tenantId(UUID t)              { this.tenantId = t; return this; }
        public VendorBuilder name(String n)                { this.name = n; return this; }
        public VendorBuilder email(String e)               { this.email = e; return this; }

        public Vendor build() {
            Vendor v = new Vendor();
            v.setId(id);
            v.setTenantId(tenantId);
            v.setName(name);
            v.setCode("VEND-001");
            v.setEmail(email);
            v.setCategory(category);
            v.setActive(true);
            v.setOutstandingBalance(BigDecimal.ZERO);
            v.setPaymentTermsDays(30);
            return v;
        }
    }

    public static CreateVendorRequest createVendorRequest() {
        CreateVendorRequest r = new CreateVendorRequest();
        r.setName("New Vendor");
        r.setEmail("new@vendor.com");
        r.setPhone("9222222222");
        return r;
    }

    // ─── Tax ─────────────────────────────────────────────────────────────────

    public static Tax tax(String name, BigDecimal rate) {
        Tax t = new Tax();
        t.setId(UUID.randomUUID());
        t.setTenantId(DEFAULT_TENANT_ID);
        t.setName(name);
        t.setRate(rate);
        t.setTaxType(Tax.TaxType.PERCENTAGE);
        t.setActive(true);
        return t;
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    public static Role role(String name) {
        Role r = new Role();
        r.setId(UUID.randomUUID());
        r.setName(name);
        r.setSystem(true);
        r.setPermissions(new HashSet<>());
        return r;
    }

    public static User user() {
        User u = new User();
        u.setId(DEFAULT_USER_ID);
        u.setTenantId(DEFAULT_TENANT_ID);
        u.setEmail("admin@erp.com");
        u.setPassword("$2a$12$encodedpassword");
        u.setFullName("Test Admin");
        u.setStatus(User.UserStatus.ACTIVE);
        u.setEmailVerified(true);
        Role adminRole = role("ADMIN");
        u.setRoles(Set.of(adminRole));
        return u;
    }

    public static LoginRequest loginRequest(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    public static RegisterRequest registerRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setFullName("New User");
        r.setEmail("newuser@erp.com");
        r.setPassword("password123");
        r.setTenantId(DEFAULT_TENANT_ID);
        return r;
    }
}
