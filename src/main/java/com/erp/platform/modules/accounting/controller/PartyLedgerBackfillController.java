package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType;
import com.erp.platform.modules.agri.entity.Farmer;
import com.erp.platform.modules.agri.entity.Organizer;
import com.erp.platform.modules.agri.repository.FarmerRepository;
import com.erp.platform.modules.agri.repository.OrganizerRepository;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.master.repository.VendorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One-time backfill: ensures a ledger exists for every existing grower, organizer, customer and
 * employee (parties created before auto-ledger creation was added). Idempotent — safe to re-run.
 */
@RestController
@RequestMapping("/api/v1/accounting/party-ledgers")
@RequiredArgsConstructor
@Tag(name = "Accounting - Party Ledgers")
public class PartyLedgerBackfillController {

    private final PartyLedgerService partyLedgerService;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final FarmerRepository farmerRepository;
    private final OrganizerRepository organizerRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    @PostMapping("/backfill")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a ledger for every existing grower, organizer, customer and employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> backfill() {
        UUID t = tenantContext.current();
        PageRequest all = PageRequest.of(0, 20000);
        int customers = 0, vendors = 0, growers = 0, organizers = 0, employees = 0;

        for (Customer c : customerRepository.findByTenantIdAndDeletedAtIsNull(t, all)) {
            partyLedgerService.ensureLedger(PartyType.CUSTOMER, c.getName(), c.getId(), c.getCode());
            customers++;
        }
        for (Vendor v : vendorRepository.findByTenantIdAndDeletedAtIsNull(t, all)) {
            partyLedgerService.ensureLedger(PartyType.VENDOR, v.getName(), v.getId(), v.getCode());
            vendors++;
        }
        for (Farmer f : farmerRepository.findByTenantIdAndDeletedAtIsNull(t, all)) {
            partyLedgerService.ensureLedger(PartyType.GROWER, f.getName(), f.getId(), f.getFarmerCode());
            growers++;
        }
        for (Organizer o : organizerRepository.findByTenantIdAndDeletedAtIsNull(t, all)) {
            partyLedgerService.ensureLedger(PartyType.ORGANIZER, o.getName(), o.getId(), o.getCode());
            organizers++;
        }
        for (Employee e : employeeRepository.findByTenantIdAndDeletedAtIsNull(t, all)) {
            String name = ((e.getFirstName() == null ? "" : e.getFirstName()) + " "
                    + (e.getLastName() == null ? "" : e.getLastName())).trim();
            partyLedgerService.ensureLedger(PartyType.EMPLOYEE, name, e.getId(), e.getEmployeeCode());
            employees++;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("customers", customers);
        out.put("vendors", vendors);
        out.put("growers", growers);
        out.put("organizers", organizers);
        out.put("employees", employees);
        return ResponseEntity.ok(ApiResponse.success(out, "Party ledgers backfilled"));
    }
}
