package com.erp.platform.modules.hr;

import com.erp.platform.common.audit.AuditAwareImpl;
import com.erp.platform.modules.hr.entity.EmployeePayDetail;
import com.erp.platform.modules.hr.entity.TadaClaim;
import com.erp.platform.modules.hr.repository.EmployeePayDetailRepository;
import com.erp.platform.modules.hr.repository.TadaClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The self-service filter on the two HR tables that store the employee as text.
 *
 * <p>These exist because an earlier version of this filter declared the query parameter as a UUID
 * while {@code employee_id} is a varchar holding the id as a string. That compiled, and the
 * application started, and it would still have shown every employee an empty list. Only running the
 * query catches it, so it is run here.
 */
@DataJpaTest
@Import(HrSelfServiceScopingTest.AuditorConfig.class)
class HrSelfServiceScopingTest {

    /**
     * The JPA slice leaves the auditor out, and auditing asks for it by name, so it is registered
     * under the name {@code @EnableJpaAuditing} expects rather than the one {@code @Import} would give.
     */
    @TestConfiguration
    static class AuditorConfig {
        @Bean(name = "auditAwareImpl")
        AuditAwareImpl auditAwareImpl() {
            return new AuditAwareImpl();
        }
    }

    @Autowired
    private TadaClaimRepository tadaClaimRepository;

    @Autowired
    private EmployeePayDetailRepository payDetailRepository;

    private static final UUID TENANT = UUID.randomUUID();

    @Test
    @DisplayName("A TA/DA claim is found by the employee's id held as text")
    void tadaClaimFoundByIdAsText() {
        UUID mine = UUID.randomUUID();
        tadaClaimRepository.save(claim(mine.toString(), "Mine"));
        tadaClaimRepository.save(claim(UUID.randomUUID().toString(), "Somebody else's"));

        var page = tadaClaimRepository.findOwnedBy(TENANT, List.of(mine.toString()), PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(TadaClaim::getEmployeeName).containsExactly("Mine");
        assertThat(page.getTotalElements())
                .as("a colleague's row must not be counted in the total either")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A claim written against the employee code is found too")
    void tadaClaimFoundByEmployeeCode() {
        UUID mine = UUID.randomUUID();
        tadaClaimRepository.save(claim("EMP-2025-0007", "Written with a code"));

        var page = tadaClaimRepository.findOwnedBy(
                TENANT, List.of(mine.toString(), "EMP-2025-0007"), PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(TadaClaim::getEmployeeName).containsExactly("Written with a code");
    }

    @Test
    @DisplayName("A soft-deleted claim stays hidden")
    void softDeletedClaimExcluded() {
        UUID mine = UUID.randomUUID();
        TadaClaim deleted = claim(mine.toString(), "Deleted");
        deleted.setDeletedAt(java.time.LocalDateTime.now());
        tadaClaimRepository.save(deleted);

        var page = tadaClaimRepository.findOwnedBy(TENANT, List.of(mine.toString()), PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Another tenant's claim is never returned, even on a matching employee key")
    void otherTenantExcluded() {
        UUID mine = UUID.randomUUID();
        TadaClaim foreign = claim(mine.toString(), "Other tenant");
        foreign.setTenantId(UUID.randomUUID());
        tadaClaimRepository.save(foreign);

        var page = tadaClaimRepository.findOwnedBy(TENANT, List.of(mine.toString()), PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Pay details are scoped the same way — salary is the most sensitive of these")
    void payDetailScoped() {
        UUID mine = UUID.randomUUID();
        payDetailRepository.save(payDetail(mine.toString(), "Mine"));
        payDetailRepository.save(payDetail(UUID.randomUUID().toString(), "Somebody else's"));

        var page = payDetailRepository.findOwnedBy(TENANT, List.of(mine.toString()), PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(EmployeePayDetail::getEmployeeName).containsExactly("Mine");
    }

    private TadaClaim claim(String employeeId, String employeeName) {
        TadaClaim c = new TadaClaim();
        c.setTenantId(TENANT);
        c.setEmployeeId(employeeId);
        c.setEmployeeName(employeeName);
        return c;
    }

    private EmployeePayDetail payDetail(String employeeId, String employeeName) {
        EmployeePayDetail d = new EmployeePayDetail();
        d.setTenantId(TENANT);
        d.setEmployeeId(employeeId);
        d.setEmployeeName(employeeName);
        return d;
    }
}
