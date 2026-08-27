package com.erp.platform.modules.payroll;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.repository.LeaveApplicationRepository;
import com.erp.platform.modules.payroll.entity.Payslip;
import com.erp.platform.modules.payroll.entity.Payslip.PayslipStatus;
import com.erp.platform.modules.payroll.entity.SalaryComponent;
import com.erp.platform.modules.payroll.repository.PayslipRepository;
import com.erp.platform.modules.payroll.repository.SalaryComponentRepository;
import com.erp.platform.modules.payroll.service.PayrollService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollService unit tests")
class PayrollServiceTest {

    @Mock private PayslipRepository payslipRepository;
    @Mock private SalaryComponentRepository componentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LeaveApplicationRepository leaveRepository;
    @Mock private JournalEntryService journalEntryService;
    @Mock private AccountRepository accountRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private PayrollService payrollService;

    private static final UUID TENANT_ID   = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID PAYSLIP_ID  = UUID.randomUUID();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── listPayslips ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("listPayslips() with month/year filters by period")
    void list_withMonthYear() {
        when(payslipRepository.findByTenantIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                eq(TENANT_ID), eq(5), eq(2026), any()))
                .thenReturn(new PageImpl<>(List.of()));
        payrollService.listPayslips(5, 2026, PageRequest.of(0, 20));
        verify(payslipRepository).findByTenantIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                eq(TENANT_ID), eq(5), eq(2026), any());
    }

    @Test
    @DisplayName("listPayslips() without month/year returns all")
    void list_noFilter() {
        when(payslipRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        payrollService.listPayslips(0, 0, PageRequest.of(0, 20));
        verify(payslipRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    // ─── getPayslipById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPayslipById() returns payslip when found")
    void getById_found() {
        Payslip p = buildPayslip(PayslipStatus.DRAFT);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        Payslip result = payrollService.getPayslipById(PAYSLIP_ID);
        assertThat(result.getId()).isEqualTo(PAYSLIP_ID);
    }

    @Test
    @DisplayName("getPayslipById() throws NOT_FOUND when missing")
    void getById_notFound() {
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> payrollService.getPayslipById(PAYSLIP_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── generatePayslip ──────────────────────────────────────────────────────

    @Test
    @DisplayName("generatePayslip() creates DRAFT payslip with correct net salary")
    void generate_createsDraftPayslip() {
        Employee emp = buildEmployee(BigDecimal.valueOf(30000));
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.of(emp));
        when(payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                TENANT_ID, EMPLOYEE_ID, 5, 2026)).thenReturn(Optional.empty());
        when(leaveRepository.sumApprovedLeaveForMonth(any(), any(), any(), any())).thenReturn(0L);
        when(componentRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(TENANT_ID))
                .thenReturn(List.of());
        when(payslipRepository.save(any())).thenAnswer(i -> {
            Payslip saved = i.getArgument(0);
            saved.setId(PAYSLIP_ID);
            return saved;
        });

        Payslip result = payrollService.generatePayslip(EMPLOYEE_ID, 5, 2026);

        assertThat(result.getStatus()).isEqualTo(PayslipStatus.DRAFT);
        assertThat(result.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        verify(payslipRepository).save(any());
    }

    @Test
    @DisplayName("generatePayslip() throws NOT_FOUND when employee missing")
    void generate_throwsEmployeeNotFound() {
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> payrollService.generatePayslip(EMPLOYEE_ID, 5, 2026));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        verify(payslipRepository, never()).save(any());
    }

    @Test
    @DisplayName("generatePayslip() throws CONFLICT when payslip already exists for month")
    void generate_throwsConflictForDuplicate() {
        Employee emp = buildEmployee(BigDecimal.valueOf(30000));
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.of(emp));
        when(payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                TENANT_ID, EMPLOYEE_ID, 5, 2026))
                .thenReturn(Optional.of(buildPayslip(PayslipStatus.DRAFT)));

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> payrollService.generatePayslip(EMPLOYEE_ID, 5, 2026));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("generatePayslip() applies percentage-based salary components")
    void generate_appliesComponents() {
        Employee emp = buildEmployee(BigDecimal.valueOf(20000));
        SalaryComponent hra = new SalaryComponent();
        hra.setId(UUID.randomUUID());
        hra.setName("HRA");
        hra.setType("EARNING");
        hra.setCalculationType("PERCENTAGE");
        hra.setValue(40.0);
        hra.setCalculationBase("BASIC");
        hra.setActive(true);

        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.of(emp));
        when(payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                TENANT_ID, EMPLOYEE_ID, 5, 2026)).thenReturn(Optional.empty());
        when(leaveRepository.sumApprovedLeaveForMonth(any(), any(), any(), any())).thenReturn(0L);
        when(componentRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(TENANT_ID))
                .thenReturn(List.of(hra));
        when(payslipRepository.save(any())).thenAnswer(i -> {
            Payslip saved = i.getArgument(0);
            saved.setId(PAYSLIP_ID);
            return saved;
        });

        Payslip result = payrollService.generatePayslip(EMPLOYEE_ID, 5, 2026);

        assertThat(result.getLines()).anyMatch(l -> "HRA".equals(l.getComponentName()));
    }

    // ─── approve ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("approve() transitions DRAFT payslip to APPROVED")
    void approve_success() {
        Payslip p = buildPayslip(PayslipStatus.DRAFT);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payslip result = payrollService.approve(PAYSLIP_ID);
        assertThat(result.getStatus()).isEqualTo(PayslipStatus.APPROVED);
    }

    @Test
    @DisplayName("approve() throws when payslip is not DRAFT")
    void approve_throwsForNonDraft() {
        Payslip p = buildPayslip(PayslipStatus.APPROVED);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        Assertions.assertThrows(AppException.class, () -> payrollService.approve(PAYSLIP_ID));
    }

    // ─── markPaid ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markPaid() transitions APPROVED to PAID and posts GL journal")
    void markPaid_postsGl() {
        Payslip p = buildPayslip(PayslipStatus.APPROVED);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "SALARY_EXPENSE"))
                .thenReturn(List.of(glAccount("5200")));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "BANK_ACCOUNT"))
                .thenReturn(List.of(glAccount("1122")));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "PF_EXPENSE"))
                .thenReturn(List.of());
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "ESI_EXPENSE"))
                .thenReturn(List.of());

        Payslip result = payrollService.markPaid(PAYSLIP_ID, "BANK");
        assertThat(result.getStatus()).isEqualTo(PayslipStatus.PAID);
        verify(journalEntryService).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("markPaid() skips GL when accounts not configured")
    void markPaid_skipsGlWhenAccountsMissing() {
        Payslip p = buildPayslip(PayslipStatus.APPROVED);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(List.of());

        Payslip result = payrollService.markPaid(PAYSLIP_ID, "CASH");
        assertThat(result.getStatus()).isEqualTo(PayslipStatus.PAID);
        verify(journalEntryService, never()).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("markPaid() throws when payslip is not APPROVED")
    void markPaid_throwsForNonApproved() {
        Payslip p = buildPayslip(PayslipStatus.DRAFT);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        Assertions.assertThrows(AppException.class, () -> payrollService.markPaid(PAYSLIP_ID, "BANK"));
    }

    // ─── reject ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reject() transitions DRAFT to REJECTED with reason")
    void reject_success() {
        Payslip p = buildPayslip(PayslipStatus.DRAFT);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payslip result = payrollService.reject(PAYSLIP_ID, "Incorrect salary");
        assertThat(result.getStatus()).isEqualTo(PayslipStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Incorrect salary");
    }

    // ─── cancel ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel() cancels DRAFT payslip")
    void cancel_fromDraft() {
        Payslip p = buildPayslip(PayslipStatus.DRAFT);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        when(payslipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payslip result = payrollService.cancel(PAYSLIP_ID);
        assertThat(result.getStatus()).isEqualTo(PayslipStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() throws when payslip is already PAID")
    void cancel_throwsForPaid() {
        Payslip p = buildPayslip(PayslipStatus.PAID);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        Assertions.assertThrows(AppException.class, () -> payrollService.cancel(PAYSLIP_ID));
    }

    @Test
    @DisplayName("cancel() throws when payslip is already CANCELLED")
    void cancel_throwsForAlreadyCancelled() {
        Payslip p = buildPayslip(PayslipStatus.CANCELLED);
        when(payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYSLIP_ID))
                .thenReturn(Optional.of(p));
        Assertions.assertThrows(AppException.class, () -> payrollService.cancel(PAYSLIP_ID));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Payslip buildPayslip(PayslipStatus status) {
        Payslip p = new Payslip();
        p.setId(PAYSLIP_ID);
        p.setTenantId(TENANT_ID);
        p.setPayslipNumber("PAY-202605-EMP001");
        p.setEmployeeId(EMPLOYEE_ID);
        p.setPayMonth(5);
        p.setPayYear(2026);
        p.setPayDate(LocalDate.now());
        p.setBasicSalary(BigDecimal.valueOf(30000));
        p.setGrossEarnings(BigDecimal.valueOf(35000));
        p.setTotalDeductions(BigDecimal.valueOf(4200));
        p.setNetSalary(BigDecimal.valueOf(30800));
        p.setStatus(status);
        p.setLines(new ArrayList<>());
        return p;
    }

    private Employee buildEmployee(BigDecimal basicSalary) {
        Employee e = new Employee();
        e.setId(EMPLOYEE_ID);
        e.setTenantId(TENANT_ID);
        e.setEmployeeCode("EMP001");
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setBasicSalary(basicSalary);
        e.setStatus(EmployeeStatus.ACTIVE);
        return e;
    }

    private Account glAccount(String code) {
        Account a = new Account();
        a.setId(UUID.randomUUID());
        a.setTenantId(TENANT_ID);
        a.setCode(code);
        a.setName("GL " + code);
        a.setBalance(BigDecimal.ZERO);
        return a;
    }
}
