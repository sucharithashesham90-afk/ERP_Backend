package com.erp.platform.config;

import com.erp.platform.modules.hr.entity.LeaveType;
import com.erp.platform.modules.hr.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Idempotent, always-runs upgrade that guarantees a "Privilege Leave" (code PL) exists for every
 * tenant that already has any leave types, and frees the "PL" code from Paternity Leave (recoded to
 * "PTL"). Runs on existing databases too — the gated {@code TestDataSeeder} only fires on a fresh DB.
 */
@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class HrLeaveTypeUpgradeSeeder implements CommandLineRunner {

    private final LeaveTypeRepository leaveTypeRepo;

    @Override
    public void run(String... args) {
        List<LeaveType> all = leaveTypeRepo.findAll();
        if (all.isEmpty()) return;

        // 1) Free up the "PL" code from Paternity Leave so it can mean Privilege Leave.
        for (LeaveType lt : all) {
            if ("PL".equalsIgnoreCase(lt.getCode())
                    && lt.getName() != null && lt.getName().toLowerCase().contains("paternity")) {
                lt.setCode("PTL");
                leaveTypeRepo.save(lt);
                log.info("Recoded Paternity Leave from PL to PTL for tenant {}", lt.getTenantId());
            }
        }

        // 2) Ensure a Privilege Leave exists per tenant that already has leave types.
        Set<UUID> tenants = all.stream().map(LeaveType::getTenantId).collect(Collectors.toSet());
        for (UUID tenantId : tenants) {
            boolean hasPrivilege = all.stream().anyMatch(lt ->
                    tenantId.equals(lt.getTenantId())
                            && lt.getName() != null
                            && lt.getName().trim().equalsIgnoreCase("Privilege Leave"));
            if (!hasPrivilege) {
                LeaveType pl = new LeaveType();
                pl.setTenantId(tenantId);
                pl.setName("Privilege Leave");
                pl.setCode("PL");
                pl.setDaysAllowed(18);
                pl.setCarryForward(true);
                pl.setPaid(true);
                pl.setActive(true);
                leaveTypeRepo.save(pl);
                log.info("Seeded Privilege Leave (PL) for tenant {}", tenantId);
            }
        }
    }
}
