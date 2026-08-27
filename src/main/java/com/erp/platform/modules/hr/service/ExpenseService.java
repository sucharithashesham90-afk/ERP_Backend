package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.dto.CreateExpenseRequest;
import com.erp.platform.modules.hr.dto.ExpenseDto;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Expense;
import com.erp.platform.modules.hr.entity.Expense.ExpenseStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.repository.HrExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service("hrExpenseService")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseService {

    private final HrExpenseRepository expenseRepository;
    private final HrAccessService hrAccess;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    /**
     * Everyone's expenses for HR; your own for anybody else.
     *
     * <p>scopeToViewable refuses outright if a non-HR caller names somebody else's employee id,
     * rather than quietly substituting their own — a caller probing for another record is told no.
     */
    public PageResponse<ExpenseDto> list(ExpenseStatus status, UUID employeeId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        employeeId = hrAccess.scopeToViewable(employeeId);
        var page = employeeId != null
                ? expenseRepository.findByTenantIdAndEmployeeIdAndDeletedAtIsNull(tenantId, employeeId, pageable)
                : status != null
                    ? expenseRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                    : expenseRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public ExpenseDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ExpenseDto create(CreateExpenseRequest request) {
        UUID tenantId = tenantContext.current();
        Employee employee = employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getEmployeeId())
                .orElseThrow(() -> AppException.notFound("Employee not found: " + request.getEmployeeId()));

        Expense expense = new Expense();
        expense.setTenantId(tenantId);
        expense.setExpenseNumber(generateNumber());
        expense.setEmployeeId(employee.getId());
        expense.setEmployeeName(employee.getFirstName() + (employee.getLastName() != null ? " " + employee.getLastName() : ""));
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        expense.setReceiptReference(request.getReceiptReference());
        expense.setNotes(request.getNotes());
        expense.setStatus(ExpenseStatus.DRAFT);

        expense = expenseRepository.save(expense);
        log.info("Expense created: id={}, number={}", expense.getId(), expense.getExpenseNumber());
        return toDto(expense);
    }

    @Transactional
    public ExpenseDto updateStatus(UUID id, ExpenseStatus newStatus, String rejectionReason) {
        Expense expense = findOrThrow(id);
        validateStatusTransition(expense.getStatus(), newStatus);

        expense.setStatus(newStatus);
        if (newStatus == ExpenseStatus.APPROVED) {
            expense.setApprovedOn(LocalDate.now());
        } else if (newStatus == ExpenseStatus.REJECTED) {
            expense.setRejectionReason(rejectionReason);
        } else if (newStatus == ExpenseStatus.REIMBURSED) {
            expense.setReimbursedOn(LocalDate.now());
        }

        expense = expenseRepository.save(expense);
        log.info("Expense {} status updated to {}", expense.getExpenseNumber(), newStatus);
        return toDto(expense);
    }

    @Transactional
    public void delete(UUID id) {
        Expense expense = findOrThrow(id);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT expenses can be deleted");
        }
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(expense);
        log.info("Expense soft-deleted: id={}", id);
    }

    private void validateStatusTransition(ExpenseStatus current, ExpenseStatus next) {
        switch (current) {
            case DRAFT -> {
                if (next != ExpenseStatus.SUBMITTED)
                    throw AppException.businessRule("DRAFT expense can only be SUBMITTED");
            }
            case SUBMITTED -> {
                if (next != ExpenseStatus.APPROVED && next != ExpenseStatus.REJECTED)
                    throw AppException.businessRule("SUBMITTED expense can only be APPROVED or REJECTED");
            }
            case APPROVED -> {
                if (next != ExpenseStatus.REIMBURSED)
                    throw AppException.businessRule("APPROVED expense can only be REIMBURSED");
            }
            default -> throw AppException.businessRule("Cannot change status from " + current);
        }
    }

    private ExpenseDto toDto(Expense e) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(e.getId());
        dto.setTenantId(e.getTenantId());
        dto.setExpenseNumber(e.getExpenseNumber());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setEmployeeName(e.getEmployeeName());
        dto.setExpenseDate(e.getExpenseDate());
        dto.setCategory(e.getCategory());
        dto.setDescription(e.getDescription());
        dto.setAmount(e.getAmount());
        dto.setCurrency(e.getCurrency());
        dto.setReceiptReference(e.getReceiptReference());
        dto.setStatus(e.getStatus());
        dto.setApprovedBy(e.getApprovedBy());
        dto.setApprovedOn(e.getApprovedOn());
        dto.setRejectionReason(e.getRejectionReason());
        dto.setReimbursedOn(e.getReimbursedOn());
        dto.setNotes(e.getNotes());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private Expense findOrThrow(UUID id) {
        return expenseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
    }

    private String generateNumber() {
        return "EXP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
