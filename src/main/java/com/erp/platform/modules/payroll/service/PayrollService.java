package com.erp.platform.modules.payroll.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.repository.LeaveApplicationRepository;
import com.erp.platform.modules.payroll.entity.Payslip;
import com.erp.platform.modules.payroll.entity.Payslip.PayslipStatus;
import com.erp.platform.modules.payroll.entity.PayslipLine;
import com.erp.platform.modules.payroll.entity.SalaryComponent;
import com.erp.platform.modules.payroll.repository.PayslipRepository;
import com.erp.platform.modules.payroll.repository.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PayrollService {

    private final PayslipRepository payslipRepository;
    private final com.erp.platform.modules.hr.service.HrAccessService hrAccess;
    private final SalaryComponentRepository componentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveApplicationRepository leaveRepository;
    private final JournalEntryService journalEntryService;
    private final AccountRepository accountRepository;
    private final TenantContext tenantContext;

    /**
     * Payslips the signed-in user may see: everyone's for HR, their own for anybody else.
     *
     * <p>Restricted in the query, not after it. A payslip carries a salary, so another employee's
     * must never be loaded, counted in the page total, or returned by a later change to this method.
     */
    public PageResponse<Payslip> listPayslips(int month, int year, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        boolean filtered = month > 0 && year > 0;

        if (!hrAccess.isHr()) {
            UUID own = hrAccess.requireCurrentEmployeeId();
            return PageResponse.of(named(filtered
                    ? payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(tenantId, own, month, year, pageable)
                    : payslipRepository.findByTenantIdAndEmployeeIdAndDeletedAtIsNull(tenantId, own, pageable)));
        }
        if (filtered) {
            return PageResponse.of(named(payslipRepository
                    .findByTenantIdAndPayMonthAndPayYearAndDeletedAtIsNull(tenantId, month, year, pageable)));
        }
        return PageResponse.of(named(payslipRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)));
    }

    /** A page of payslips with every employee name resolved. */
    private org.springframework.data.domain.Page<Payslip> named(org.springframework.data.domain.Page<Payslip> page) {
        fillEmployeeNames(page.getContent());
        return page;
    }

    /**
     * Fill in the employee name on payslips that have none.
     *
     * <p>employee_name is stored on the payslip so it survives the employee record changing, but
     * payslips generated before that column was populated hold null, and the screens then fall back
     * to showing part of the employee's id — which reads as a code, not a name. Resolving it here
     * covers those rows without a migration. The service is read-only, so nothing is written back.
     *
     * <p>Names are fetched once for the whole page rather than per row.
     */
    private void fillEmployeeNames(List<Payslip> payslips) {
        UUID tenantId = tenantContext.current();
        var missing = payslips.stream()
                .filter(ps -> ps.getEmployeeName() == null || ps.getEmployeeName().isBlank()
                        || ps.getEmployeeName().contains("null"))
                .map(Payslip::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (missing.isEmpty()) return;
        var names = new java.util.HashMap<UUID, String>();
        for (Employee e : employeeRepository.findAllById(missing)) {
            if (tenantId.equals(e.getTenantId())) names.put(e.getId(), displayName(e));
        }
        for (Payslip ps : payslips) {
            String resolved = names.get(ps.getEmployeeId());
            if (resolved != null && !resolved.isBlank()) ps.setEmployeeName(resolved);
        }
    }

    /** First and last name, without the literal "null" a missing last name would otherwise leave. */
    private static String displayName(Employee e) {
        String first = e.getFirstName() == null ? "" : e.getFirstName().trim();
        String last = e.getLastName() == null ? "" : e.getLastName().trim();
        return (first + " " + last).trim();
    }

    /** One payslip — refused unless it is the caller's own, or the caller is HR. */
    public Payslip getPayslipById(UUID id) {
        Payslip p = findOrThrow(id);
        hrAccess.assertCanSee(p.getEmployeeId());
        fillEmployeeNames(List.of(p));
        return p;
    }

    @Transactional
    public Payslip generatePayslip(UUID employeeId, int month, int year) {
        UUID tenantId = tenantContext.current();
        Employee employee = employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, employeeId)
                .orElseThrow(() -> AppException.notFound("Employee not found: " + employeeId));

        // Check if payslip already exists
        payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                tenantId, employeeId, month, year).ifPresent(p -> {
            throw AppException.conflict("Payslip already exists for this employee and month");
        });

        YearMonth ym = YearMonth.of(year, month);
        int workingDays = ym.lengthOfMonth();

        BigDecimal basicSalary = employee.getBasicSalary();
        BigDecimal perDaySalary = basicSalary.divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP);

        // Deduct approved leave days for the month
        int leaveDays = 0;
        try {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd   = monthStart.withDayOfMonth(ym.lengthOfMonth());
            Long leaveSum = leaveRepository.sumApprovedLeaveForMonth(tenantId, employeeId, monthStart, monthEnd);
            leaveDays = (leaveSum != null) ? leaveSum.intValue() : 0;
        } catch (Exception e) {
            log.warn("Leave lookup failed for employee {} month {}/{}: {}", employeeId, month, year, e.getMessage());
        }
        BigDecimal leaveDeduction = perDaySalary.multiply(BigDecimal.valueOf(leaveDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal earnedBasic = basicSalary.subtract(leaveDeduction).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        List<SalaryComponent> components = componentRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<PayslipLine> lines = new ArrayList<>();
        BigDecimal grossEarnings = earnedBasic;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        // Basic salary line
        PayslipLine basicLine = new PayslipLine();
        basicLine.setComponentName("Basic Salary");
        basicLine.setComponentType("EARNING");
        basicLine.setAmount(earnedBasic);
        lines.add(basicLine);

        for (SalaryComponent comp : components) {
            BigDecimal amount;
            if ("PERCENTAGE".equals(comp.getCalculationType())) {
                BigDecimal base = "GROSS".equals(comp.getCalculationBase()) ? grossEarnings : earnedBasic;
                amount = base.multiply(BigDecimal.valueOf(comp.getValue()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                amount = BigDecimal.valueOf(comp.getValue());
            }

            PayslipLine line = new PayslipLine();
            line.setComponentId(comp.getId());
            line.setComponentName(comp.getName());
            line.setComponentType(comp.getType());
            line.setAmount(amount);
            lines.add(line);

            if ("EARNING".equals(comp.getType())) {
                grossEarnings = grossEarnings.add(amount);
            } else {
                totalDeductions = totalDeductions.add(amount);
            }
        }

        BigDecimal netSalary = grossEarnings.subtract(totalDeductions);

        Payslip payslip = new Payslip();
        payslip.setTenantId(tenantId);
        payslip.setPayslipNumber("PAY-" + year + String.format("%02d", month) + "-"
                + employeeId.toString().substring(0, 8).toUpperCase());
        payslip.setEmployeeId(employeeId);
        payslip.setEmployeeName(displayName(employee));
        payslip.setPayMonth(month);
        payslip.setPayYear(year);
        payslip.setPayDate(LocalDate.now());
        payslip.setBasicSalary(earnedBasic);
        payslip.setGrossEarnings(grossEarnings);
        payslip.setTotalDeductions(totalDeductions);
        payslip.setNetSalary(netSalary);
        payslip.setWorkingDays(workingDays);
        payslip.setPresentDays(workingDays - leaveDays);
        payslip.setLeaveDays(leaveDays);
        payslip.setStatus(PayslipStatus.DRAFT);
        payslip.setLines(lines);
        lines.forEach(l -> l.setPayslip(payslip));

        Payslip saved = payslipRepository.save(payslip);
        log.info("Payslip generated: id={}, employee={}, month={}/{}", saved.getId(), employeeId, month, year);
        return saved;
    }

    @Transactional
    public Payslip approve(UUID id) {
        Payslip payslip = findOrThrow(id);
        if (payslip.getStatus() != PayslipStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT payslips can be approved");
        }
        payslip.setStatus(PayslipStatus.APPROVED);
        return payslipRepository.save(payslip);
    }

    @Transactional
    public Payslip markPaid(UUID id, String paymentMethod) {
        Payslip payslip = findOrThrow(id);
        if (payslip.getStatus() != PayslipStatus.APPROVED) {
            throw AppException.badRequest("Payslip must be approved before marking as paid");
        }
        payslip.setStatus(PayslipStatus.PAID);
        payslip.setPaymentMethod(paymentMethod);
        Payslip saved = payslipRepository.save(payslip);
        // Post salary expense journal entry: DR Salary Expense / CR Bank/Cash
        createPayrollJournalEntry(saved);
        return saved;
    }

    @Transactional
    public Payslip reject(UUID id, String reason) {
        Payslip payslip = findOrThrow(id);
        if (payslip.getStatus() != PayslipStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT payslips can be rejected. Current status: " + payslip.getStatus());
        }
        payslip.setStatus(PayslipStatus.REJECTED);
        payslip.setRejectionReason(reason);
        Payslip saved = payslipRepository.save(payslip);
        log.info("Payslip {} rejected: {}", saved.getPayslipNumber(), reason);
        return saved;
    }

    @Transactional
    public Payslip cancel(UUID id) {
        Payslip payslip = findOrThrow(id);
        if (payslip.getStatus() == PayslipStatus.PAID) {
            throw AppException.badRequest("Cannot cancel a PAID payslip");
        }
        if (payslip.getStatus() == PayslipStatus.CANCELLED) {
            throw AppException.badRequest("Payslip is already cancelled");
        }
        payslip.setStatus(PayslipStatus.CANCELLED);
        Payslip saved = payslipRepository.save(payslip);
        log.info("Payslip {} cancelled", saved.getPayslipNumber());
        return saved;
    }

    private void createPayrollJournalEntry(Payslip payslip) {
        try {
            UUID tenantId = payslip.getTenantId();
            List<Account> salaryAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "SALARY_EXPENSE");
            List<Account> bankAccts   = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
            if (salaryAccts.isEmpty() || bankAccts.isEmpty()) {
                log.debug("Payroll voucher skipped for {}: SALARY_EXPENSE or BANK_ACCOUNT account not configured",
                        payslip.getPayslipNumber());
                return;
            }
            Account salaryAcct = salaryAccts.get(0);
            Account bankAcct   = bankAccts.get(0);
            BigDecimal netSalary = payslip.getNetSalary();

            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("PAYSLIP");
            je.setReferenceId(payslip.getId());
            je.setReferenceNumber(payslip.getPayslipNumber());
            je.setEntryDate(LocalDate.now());
            je.setDescription("Salary payment: " + payslip.getPayslipNumber());

            JournalEntryLine drLine = new JournalEntryLine();
            drLine.setAccountId(salaryAcct.getId());
            drLine.setAccountCode(salaryAcct.getCode());
            drLine.setAccountName(salaryAcct.getName());
            drLine.setDebitAmount(netSalary);
            drLine.setCreditAmount(BigDecimal.ZERO);
            drLine.setDescription("Salary expense — " + payslip.getPayslipNumber());

            JournalEntryLine crLine = new JournalEntryLine();
            crLine.setAccountId(bankAcct.getId());
            crLine.setAccountCode(bankAcct.getCode());
            crLine.setAccountName(bankAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO);
            crLine.setCreditAmount(netSalary);
            crLine.setDescription("Salary paid — " + payslip.getPayslipNumber());

            je.getLines().add(drLine);
            je.getLines().add(crLine);

            // Employer PF contribution: 12% of basic salary
            BigDecimal basicSalary = payslip.getBasicSalary() != null ? payslip.getBasicSalary() : BigDecimal.ZERO;
            if (basicSalary.compareTo(BigDecimal.ZERO) > 0) {
                List<Account> pfExpAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "PF_EXPENSE");
                List<Account> pfPayAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "PF_PAYABLE");
                if (!pfExpAccts.isEmpty() && !pfPayAccts.isEmpty()) {
                    BigDecimal pfAmount = basicSalary.multiply(new BigDecimal("0.12"))
                            .setScale(2, RoundingMode.HALF_UP);
                    JournalEntryLine pfDr = new JournalEntryLine();
                    pfDr.setAccountId(pfExpAccts.get(0).getId()); pfDr.setAccountCode(pfExpAccts.get(0).getCode());
                    pfDr.setAccountName(pfExpAccts.get(0).getName());
                    pfDr.setDebitAmount(pfAmount); pfDr.setCreditAmount(BigDecimal.ZERO);
                    pfDr.setDescription("Employer PF — " + payslip.getPayslipNumber());
                    JournalEntryLine pfCr = new JournalEntryLine();
                    pfCr.setAccountId(pfPayAccts.get(0).getId()); pfCr.setAccountCode(pfPayAccts.get(0).getCode());
                    pfCr.setAccountName(pfPayAccts.get(0).getName());
                    pfCr.setDebitAmount(BigDecimal.ZERO); pfCr.setCreditAmount(pfAmount);
                    pfCr.setDescription("Employer PF payable — " + payslip.getPayslipNumber());
                    je.getLines().add(pfDr);
                    je.getLines().add(pfCr);
                }
            }

            // Employer ESI contribution: 3.25% of gross earnings (applicable when gross <= 21000)
            BigDecimal grossEarnings = payslip.getGrossEarnings() != null ? payslip.getGrossEarnings() : BigDecimal.ZERO;
            if (grossEarnings.compareTo(BigDecimal.ZERO) > 0
                    && grossEarnings.compareTo(new BigDecimal("21000")) <= 0) {
                List<Account> esiExpAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ESI_EXPENSE");
                List<Account> esiPayAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ESI_PAYABLE");
                if (!esiExpAccts.isEmpty() && !esiPayAccts.isEmpty()) {
                    BigDecimal esiAmount = grossEarnings.multiply(new BigDecimal("0.0325"))
                            .setScale(2, RoundingMode.HALF_UP);
                    JournalEntryLine esiDr = new JournalEntryLine();
                    esiDr.setAccountId(esiExpAccts.get(0).getId()); esiDr.setAccountCode(esiExpAccts.get(0).getCode());
                    esiDr.setAccountName(esiExpAccts.get(0).getName());
                    esiDr.setDebitAmount(esiAmount); esiDr.setCreditAmount(BigDecimal.ZERO);
                    esiDr.setDescription("Employer ESI — " + payslip.getPayslipNumber());
                    JournalEntryLine esiCr = new JournalEntryLine();
                    esiCr.setAccountId(esiPayAccts.get(0).getId()); esiCr.setAccountCode(esiPayAccts.get(0).getCode());
                    esiCr.setAccountName(esiPayAccts.get(0).getName());
                    esiCr.setDebitAmount(BigDecimal.ZERO); esiCr.setCreditAmount(esiAmount);
                    esiCr.setDescription("Employer ESI payable — " + payslip.getPayslipNumber());
                    je.getLines().add(esiDr);
                    je.getLines().add(esiCr);
                }
            }

            journalEntryService.create(je);
            log.info("Payroll voucher DRAFT created for payslip {}", payslip.getPayslipNumber());
        } catch (Exception e) {
            log.warn("Payroll voucher creation failed for {}: {}", payslip.getPayslipNumber(), e.getMessage());
        }
    }

    // ---- Salary Components ----

    public PageResponse<SalaryComponent> listComponents(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(componentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public SalaryComponent createComponent(java.util.Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        SalaryComponent comp = new SalaryComponent();
        comp.setTenantId(tenantId);
        applyComponentBody(comp, body);
        return componentRepository.save(comp);
    }

    @Transactional
    public SalaryComponent updateComponent(UUID id, java.util.Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        SalaryComponent comp = componentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Salary component not found: " + id));
        applyComponentBody(comp, body);
        return componentRepository.save(comp);
    }

    @Transactional
    public void deleteComponent(UUID id) {
        UUID tenantId = tenantContext.current();
        SalaryComponent comp = componentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Salary component not found: " + id));
        comp.setDeletedAt(java.time.LocalDateTime.now());
        componentRepository.save(comp);
    }

    private void applyComponentBody(SalaryComponent comp, java.util.Map<String, Object> body) {
        if (body.containsKey("name") && body.get("name") != null) comp.setName(body.get("name").toString());
        if (body.containsKey("type") && body.get("type") != null) comp.setType(body.get("type").toString());
        if (body.containsKey("description") && body.get("description") != null) comp.setDescription(body.get("description").toString());
        if (body.containsKey("calculationType") && body.get("calculationType") != null) {
            comp.setCalculationType(body.get("calculationType").toString());
        }
        if (body.containsKey("value") && body.get("value") != null) {
            String valStr = body.get("value").toString().trim();
            if (!valStr.isEmpty()) comp.setValue(Double.parseDouble(valStr));
        }
        if (body.containsKey("active") && body.get("active") != null) {
            comp.setActive(Boolean.parseBoolean(body.get("active").toString()));
        }
    }

    @Transactional
    public java.util.Map<String, Object> runPayroll(int month, int year) {
        UUID tenantId = tenantContext.current();
        List<Employee> employees = employeeRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                tenantId, EmployeeStatus.ACTIVE);
        int generated = 0, skipped = 0;
        for (Employee emp : employees) {
            boolean exists = payslipRepository.findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
                    tenantId, emp.getId(), month, year).isPresent();
            if (exists) { skipped++; continue; }
            try {
                generatePayslip(emp.getId(), month, year);
                generated++;
            } catch (Exception e) {
                log.warn("Failed to generate payslip for employee {}: {}", emp.getId(), e.getMessage());
                skipped++;
            }
        }
        return java.util.Map.of("generated", generated, "skipped", skipped, "total", employees.size());
    }

    private Payslip findOrThrow(UUID id) {
        return payslipRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Payslip not found: " + id));
    }
}
