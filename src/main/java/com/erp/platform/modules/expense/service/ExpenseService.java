package com.erp.platform.modules.expense.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.expense.entity.Expense;
import com.erp.platform.modules.expense.entity.Expense.ExpenseStatus;
import com.erp.platform.modules.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository repo;
    private final TenantContext tenantContext;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;

    public PageResponse<Expense> list(ExpenseStatus status, UUID employeeId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (employeeId != null) return PageResponse.of(repo.findByTenantIdAndEmployeeIdAndDeletedAtIsNull(tenantId, employeeId, pageable));
        if (status != null) return PageResponse.of(repo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public Expense getById(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
    }

    @Transactional
    public Expense create(Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        int seq = repo.maxExpenseSeq(tenantId) + 1;
        Expense e = new Expense();
        e.setTenantId(tenantId);
        e.setExpenseNumber(String.format("EXP-%04d", seq));
        e.setStatus(ExpenseStatus.DRAFT);
        apply(e, body);
        return repo.save(e);
    }

    @Transactional
    public Expense update(UUID id, Map<String, Object> body) {
        Expense e = findMutable(id);
        apply(e, body);
        return repo.save(e);
    }

    @Transactional
    public Expense submit(UUID id) {
        Expense e = findMutable(id);
        if (e.getStatus() != ExpenseStatus.DRAFT) throw AppException.badRequest("Only DRAFT expenses can be submitted");
        e.setStatus(ExpenseStatus.SUBMITTED);
        return repo.save(e);
    }

    @Transactional
    public Expense approve(UUID id) {
        Expense e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
        if (e.getStatus() != ExpenseStatus.SUBMITTED) throw AppException.badRequest("Only SUBMITTED expenses can be approved");
        e.setStatus(ExpenseStatus.APPROVED);
        return repo.save(e);
    }

    @Transactional
    public Expense reject(UUID id, String reason) {
        Expense e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
        if (e.getStatus() != ExpenseStatus.SUBMITTED) throw AppException.badRequest("Only SUBMITTED expenses can be rejected");
        e.setStatus(ExpenseStatus.REJECTED);
        e.setRejectionReason(reason);
        return repo.save(e);
    }

    @Transactional
    public Expense markPaid(UUID id, String paymentMethod) {
        Expense e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
        if (e.getStatus() != ExpenseStatus.APPROVED) throw AppException.badRequest("Only APPROVED expenses can be marked paid");
        e.setStatus(ExpenseStatus.PAID);
        e.setPaymentMethod(paymentMethod);
        e.setPaidDate(LocalDate.now());
        Expense saved = repo.save(e);
        postExpensePaymentJournal(saved);
        return saved;
    }

    private void postExpensePaymentJournal(Expense e) {
        UUID tenantId = e.getTenantId();
        List<Account> expAccts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "EMPLOYEE_EXPENSE");
        List<Account> bankAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
        if (expAccts.isEmpty() || bankAccts.isEmpty()) {
            log.warn("Expense GL skipped for {}: EMPLOYEE_EXPENSE or BANK_ACCOUNT account not configured", e.getExpenseNumber());
            return;
        }
        Account expAcct  = expAccts.get(0);
        Account bankAcct = bankAccts.get(0);
        BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;

        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccountId(expAcct.getId()); dr.setAccountCode(expAcct.getCode()); dr.setAccountName(expAcct.getName());
        dr.setDebitAmount(amt); dr.setCreditAmount(BigDecimal.ZERO);
        dr.setDescription("Expense payment: " + e.getExpenseNumber());

        JournalEntryLine cr = new JournalEntryLine();
        cr.setAccountId(bankAcct.getId()); cr.setAccountCode(bankAcct.getCode()); cr.setAccountName(bankAcct.getName());
        cr.setDebitAmount(BigDecimal.ZERO); cr.setCreditAmount(amt);
        cr.setDescription("Expense payment: " + e.getExpenseNumber());

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("EXPENSE");
        je.setReferenceId(e.getId());
        je.setReferenceNumber(e.getExpenseNumber());
        je.setEntryDate(e.getPaidDate());
        je.setDescription("Expense paid: " + e.getEmployeeName() + " — " + e.getCategory());
        je.getLines().add(dr);
        je.getLines().add(cr);
        journalEntryService.create(je);
        log.info("Expense GL posted for {}: DR {} CR {} amount={}", e.getExpenseNumber(), expAcct.getCode(), bankAcct.getCode(), amt);
    }

    @Transactional
    public void delete(UUID id) {
        Expense e = findMutable(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    private Expense findMutable(UUID id) {
        Expense e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Expense not found: " + id));
        if (e.getStatus() == ExpenseStatus.APPROVED || e.getStatus() == ExpenseStatus.PAID)
            throw AppException.badRequest("Cannot modify an approved or paid expense");
        return e;
    }

    private void apply(Expense e, Map<String, Object> body) {
        if (body.containsKey("employeeId") && body.get("employeeId") != null)
            e.setEmployeeId(UUID.fromString(body.get("employeeId").toString()));
        if (body.containsKey("employeeName")) e.setEmployeeName(s(body, "employeeName"));
        if (body.containsKey("category")) e.setCategory(s(body, "category"));
        if (body.containsKey("amount") && body.get("amount") != null)
            e.setAmount(new BigDecimal(body.get("amount").toString()));
        if (body.containsKey("currency")) e.setCurrency(s(body, "currency"));
        if (body.containsKey("description")) e.setDescription(s(body, "description"));
        if (body.containsKey("expenseDate") && body.get("expenseDate") != null)
            e.setExpenseDate(LocalDate.parse(body.get("expenseDate").toString()));
        if (body.containsKey("receiptReference")) e.setReceiptReference(s(body, "receiptReference"));
        if (body.containsKey("notes")) e.setNotes(s(body, "notes"));
    }

    private String s(Map<String, Object> body, String key) {
        Object v = body.get(key); return v != null ? v.toString() : null;
    }
}
