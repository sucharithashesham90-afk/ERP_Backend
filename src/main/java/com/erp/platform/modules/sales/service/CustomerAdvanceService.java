package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.sales.dto.CreateCustomerAdvanceRequest;
import com.erp.platform.modules.sales.dto.CustomerAdvanceDto;
import com.erp.platform.modules.sales.entity.CustomerAdvance;
import com.erp.platform.modules.sales.entity.CustomerAdvance.AdvanceStatus;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.CustomerAdvanceRepository;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerAdvanceService {

    private final CustomerAdvanceRepository advanceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<CustomerAdvanceDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(
            advanceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public CustomerAdvanceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public BigDecimal getAvailableBalance(UUID customerId) {
        return advanceRepository.totalAvailableForCustomer(tenantContext.current(), customerId);
    }

    @Transactional
    public CustomerAdvanceDto create(CreateCustomerAdvanceRequest request) {
        UUID tenantId = tenantContext.current();
        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        CustomerAdvance advance = new CustomerAdvance();
        advance.setTenantId(tenantId);
        advance.setAdvanceNumber(generateAdvanceNumber());
        advance.setCustomerId(customer.getId());
        advance.setCustomerName(customer.getName());
        advance.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        advance.setPaymentMode(request.getPaymentMode());
        advance.setReferenceNumber(request.getReferenceNumber());
        advance.setNotes(request.getNotes());
        advance.setAmount(request.getAmount());
        advance.setAmountApplied(BigDecimal.ZERO);
        advance.setAmountAvailable(request.getAmount());
        advance.setStatus(AdvanceStatus.AVAILABLE);

        CustomerAdvance saved = advanceRepository.save(advance);
        log.info("Customer advance created: id={}, number={}", saved.getId(), saved.getAdvanceNumber());
        createAdvanceReceiptJournalEntry(saved);
        return toDto(saved);
    }

    /**
     * Apply available advances for a customer against an invoice's balance due.
     * Picks AVAILABLE/PARTIALLY_APPLIED advances oldest-first until invoice is settled
     * or advances are exhausted.
     *
     * @return total advance amount applied in this operation
     */
    @Transactional
    public BigDecimal applyAdvancesToInvoice(UUID invoiceId) {
        UUID tenantId = tenantContext.current();
        Invoice invoice = invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, invoiceId)
                .orElseThrow(() -> AppException.notFound("Invoice not found: " + invoiceId));

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        List<CustomerAdvance> advances = advanceRepository
                .findByTenantIdAndCustomerIdAndStatusAndDeletedAtIsNull(tenantId, invoice.getCustomerId(), AdvanceStatus.AVAILABLE);
        advances.addAll(advanceRepository
                .findByTenantIdAndCustomerIdAndStatusAndDeletedAtIsNull(tenantId, invoice.getCustomerId(), AdvanceStatus.PARTIALLY_APPLIED));

        BigDecimal remaining = invoice.getBalanceDue();
        BigDecimal totalApplied = BigDecimal.ZERO;

        for (CustomerAdvance advance : advances) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal apply = advance.getAmountAvailable().min(remaining);
            advance.setAmountApplied(advance.getAmountApplied().add(apply));
            advance.setAmountAvailable(advance.getAmountAvailable().subtract(apply));
            advance.setStatus(advance.getAmountAvailable().compareTo(BigDecimal.ZERO) == 0
                    ? AdvanceStatus.FULLY_APPLIED : AdvanceStatus.PARTIALLY_APPLIED);
            advanceRepository.save(advance);
            remaining = remaining.subtract(apply);
            totalApplied = totalApplied.add(apply);
        }

        if (totalApplied.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setPaidAmount(invoice.getPaidAmount().add(totalApplied));
            invoice.setBalanceDue(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(Invoice.InvoiceStatus.PAID);
            } else {
                invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
            }
            invoiceRepository.save(invoice);
            log.info("Applied advance {} to invoice {}", totalApplied, invoiceId);
        }
        return totalApplied;
    }

    @Transactional
    public CustomerAdvanceDto refund(UUID id) {
        CustomerAdvance advance = findOrThrow(id);
        if (advance.getAmountApplied().compareTo(BigDecimal.ZERO) > 0) {
            throw AppException.badRequest("Cannot refund a partially or fully applied advance");
        }
        advance.setStatus(AdvanceStatus.REFUNDED);
        advance.setAmountAvailable(BigDecimal.ZERO);
        CustomerAdvance saved = advanceRepository.save(advance);
        createAdvanceRefundJournalEntry(saved);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        CustomerAdvance advance = findOrThrow(id);
        if (advance.getAmountApplied().compareTo(BigDecimal.ZERO) > 0) {
            throw AppException.badRequest("Cannot delete an advance that has been partially or fully applied");
        }
        advance.setDeletedAt(LocalDateTime.now());
        advanceRepository.save(advance);
    }

    private void createAdvanceReceiptJournalEntry(CustomerAdvance advance) {
        try {
            UUID tenantId = advance.getTenantId();
            List<Account> bankAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
            List<Account> liabilityAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "CUSTOMER_ADVANCE_LIABILITY");
            if (bankAccts.isEmpty() || liabilityAccts.isEmpty()) {
                log.debug("Advance receipt voucher skipped for {}: BANK_ACCOUNT or CUSTOMER_ADVANCE_LIABILITY account not configured",
                        advance.getAdvanceNumber());
                return;
            }
            Account bankAcct      = bankAccts.get(0);
            Account liabilityAcct = liabilityAccts.get(0);
            BigDecimal amount     = advance.getAmount();

            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("CUSTOMER_ADVANCE");
            je.setReferenceId(advance.getId());
            je.setReferenceNumber(advance.getAdvanceNumber());
            je.setEntryDate(advance.getPaymentDate() != null ? advance.getPaymentDate() : LocalDate.now());
            je.setDescription("Customer advance received: " + advance.getAdvanceNumber());

            JournalEntryLine drLine = new JournalEntryLine();
            drLine.setAccountId(bankAcct.getId());
            drLine.setAccountCode(bankAcct.getCode());
            drLine.setAccountName(bankAcct.getName());
            drLine.setDebitAmount(amount);
            drLine.setCreditAmount(BigDecimal.ZERO);
            drLine.setDescription("Advance receipt — " + advance.getAdvanceNumber());

            JournalEntryLine crLine = new JournalEntryLine();
            crLine.setAccountId(liabilityAcct.getId());
            crLine.setAccountCode(liabilityAcct.getCode());
            crLine.setAccountName(liabilityAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO);
            crLine.setCreditAmount(amount);
            crLine.setDescription("Customer advance liability — " + advance.getCustomerName());

            je.getLines().add(drLine);
            je.getLines().add(crLine);
            journalEntryService.create(je);
            log.info("Advance receipt voucher DRAFT created for {}", advance.getAdvanceNumber());
        } catch (Exception e) {
            log.warn("Advance receipt voucher failed for {}: {}", advance.getAdvanceNumber(), e.getMessage());
        }
    }

    private void createAdvanceRefundJournalEntry(CustomerAdvance advance) {
        try {
            UUID tenantId = advance.getTenantId();
            List<Account> bankAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
            List<Account> liabilityAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "CUSTOMER_ADVANCE_LIABILITY");
            if (bankAccts.isEmpty() || liabilityAccts.isEmpty()) {
                log.debug("Advance refund voucher skipped for {}: accounts not configured", advance.getAdvanceNumber());
                return;
            }
            Account bankAcct      = bankAccts.get(0);
            Account liabilityAcct = liabilityAccts.get(0);
            BigDecimal amount     = advance.getAmount();

            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("CUSTOMER_ADVANCE_REFUND");
            je.setReferenceId(advance.getId());
            je.setReferenceNumber(advance.getAdvanceNumber());
            je.setEntryDate(LocalDate.now());
            je.setDescription("Customer advance refunded: " + advance.getAdvanceNumber());

            JournalEntryLine drLine = new JournalEntryLine();
            drLine.setAccountId(liabilityAcct.getId());
            drLine.setAccountCode(liabilityAcct.getCode());
            drLine.setAccountName(liabilityAcct.getName());
            drLine.setDebitAmount(amount);
            drLine.setCreditAmount(BigDecimal.ZERO);
            drLine.setDescription("Advance liability reversal — " + advance.getAdvanceNumber());

            JournalEntryLine crLine = new JournalEntryLine();
            crLine.setAccountId(bankAcct.getId());
            crLine.setAccountCode(bankAcct.getCode());
            crLine.setAccountName(bankAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO);
            crLine.setCreditAmount(amount);
            crLine.setDescription("Advance refund paid — " + advance.getCustomerName());

            je.getLines().add(drLine);
            je.getLines().add(crLine);
            journalEntryService.create(je);
            log.info("Advance refund voucher DRAFT created for {}", advance.getAdvanceNumber());
        } catch (Exception e) {
            log.warn("Advance refund voucher failed for {}: {}", advance.getAdvanceNumber(), e.getMessage());
        }
    }

    private CustomerAdvanceDto toDto(CustomerAdvance a) {
        CustomerAdvanceDto dto = new CustomerAdvanceDto();
        dto.setId(a.getId());
        dto.setTenantId(a.getTenantId());
        dto.setAdvanceNumber(a.getAdvanceNumber());
        dto.setCustomerId(a.getCustomerId());
        dto.setCustomerName(a.getCustomerName());
        dto.setPaymentDate(a.getPaymentDate());
        dto.setPaymentMode(a.getPaymentMode());
        dto.setReferenceNumber(a.getReferenceNumber());
        dto.setAmount(a.getAmount());
        dto.setAmountApplied(a.getAmountApplied());
        dto.setAmountAvailable(a.getAmountAvailable());
        dto.setStatus(a.getStatus());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }

    private CustomerAdvance findOrThrow(UUID id) {
        return advanceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Customer advance not found: " + id));
    }

    private String generateAdvanceNumber() {
        return "ADV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
