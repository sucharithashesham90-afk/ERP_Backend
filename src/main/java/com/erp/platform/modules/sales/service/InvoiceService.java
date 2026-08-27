package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.sales.dto.CreateInvoiceRequest;
import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import com.erp.platform.modules.sales.dto.InvoiceDto;
import com.erp.platform.modules.sales.dto.InvoiceItemDto;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import com.erp.platform.modules.sales.entity.InvoiceItem;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.CustomerAdvanceRepository;
import com.erp.platform.modules.sales.entity.CustomerAdvance;
import com.erp.platform.modules.sales.entity.CustomerAdvance.AdvanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAdvanceRepository advanceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final JournalEntryRepository journalEntryRepository;
    private final SalesOrderRepository salesOrderRepository;
    /** A tax invoice has to show who issued it, not only who it is billed to. */
    private final com.erp.platform.modules.organization.service.CompanyLetterheadService letterhead;
    private final TenantContext tenantContext;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerRepository customerRepository,
                          CustomerAdvanceRepository advanceRepository,
                          AccountRepository accountRepository,
                          @Lazy JournalEntryService journalEntryService,
                          JournalEntryRepository journalEntryRepository,
                          SalesOrderRepository salesOrderRepository,
                          com.erp.platform.modules.organization.service.CompanyLetterheadService letterhead,
                          TenantContext tenantContext) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.advanceRepository = advanceRepository;
        this.accountRepository = accountRepository;
        this.journalEntryService = journalEntryService;
        this.journalEntryRepository = journalEntryRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.letterhead = letterhead;
        this.tenantContext = tenantContext;
    }

    public PageResponse<InvoiceDto> list(InvoiceStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? invoiceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public InvoiceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public InvoiceDto create(CreateInvoiceRequest request) {
        UUID tenantId = tenantContext.current();
        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setCustomerId(customer.getId());
        invoice.setCustomerName(customer.getName());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceDate(request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(30));
        invoice.setPaymentTerms(request.getPaymentTerms());
        invoice.setSubject(request.getSubject());
        invoice.setNotes(request.getNotes());
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO);
        invoice.setFreightPaidAdvance(request.getFreightPaidAdvance() != null ? request.getFreightPaidAdvance() : BigDecimal.ZERO);
        invoice.setPackingForwarding(request.getPackingForwarding() != null ? request.getPackingForwarding() : BigDecimal.ZERO);
        invoice.setTdsAmount(request.getTdsAmount() != null ? request.getTdsAmount() : BigDecimal.ZERO);
        invoice.setSurchargeAmount(request.getSurchargeAmount() != null ? request.getSurchargeAmount() : BigDecimal.ZERO);
        invoice.setRoundedValue(request.getRoundedValue() != null ? request.getRoundedValue() : BigDecimal.ZERO);
        invoice.setBalanceAfterSubmission(request.getBalanceAfterSubmission() != null ? request.getBalanceAfterSubmission() : BigDecimal.ZERO);
        invoice.setSalesArea(request.getSalesArea());
        invoice.setFromLocation(request.getFromLocation());
        invoice.setLorryNumber(request.getLorryNumber());
        invoice.setWayBillNumber(request.getWayBillNumber());
        invoice.setRrRlNumber(request.getRrRlNumber());
        invoice.setCarrier(request.getCarrier());
        invoice.setLicenseNumber(request.getLicenseNumber());
        invoice.setDcComments(request.getDcComments());
        invoice.setInvoiceComments(request.getInvoiceComments());
        invoice.setSalesOrderId(request.getSalesOrderId());

        if (request.getSalesOrderId() != null &&
                invoiceRepository.existsByTenantIdAndSalesOrderIdAndDeletedAtIsNull(tenantId, request.getSalesOrderId())) {
            throw AppException.badRequest("An invoice has already been created for this sales order");
        }

        List<InvoiceItem> items = buildInvoiceItems(invoice, request.getItems());
        invoice.setItems(items);
        calculateTotals(invoice);
        invoice.setBalanceDue(invoice.getTotalAmount());
        invoice.setPaidAmount(BigDecimal.ZERO);

        // Credit limit check — only when limit is configured (> 0)
        if (customer.getCreditLimit() != null && customer.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal outstanding = customer.getOutstandingBalance() != null ? customer.getOutstandingBalance() : BigDecimal.ZERO;
            if (outstanding.add(invoice.getTotalAmount()).compareTo(customer.getCreditLimit()) > 0) {
                throw AppException.badRequest(
                    "Credit limit exceeded for customer '" + customer.getName() + "': " +
                    "outstanding=" + outstanding + ", newInvoice=" + invoice.getTotalAmount() +
                    ", limit=" + customer.getCreditLimit());
            }
        }

        invoice = invoiceRepository.save(invoice);

        // Track outstanding balance on customer
        BigDecimal existing = customer.getOutstandingBalance() != null ? customer.getOutstandingBalance() : BigDecimal.ZERO;
        customer.setOutstandingBalance(existing.add(invoice.getTotalAmount()));
        customerRepository.save(customer);

        log.info("Invoice created: id={}, number={}", invoice.getId(), invoice.getInvoiceNumber());
        return toDto(invoice);
    }

    @Transactional
    public InvoiceDto update(UUID id, CreateInvoiceRequest request) {
        UUID tenantId = tenantContext.current();
        Invoice invoice = findOrThrow(id);

        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        invoice.setCustomerId(customer.getId());
        invoice.setCustomerName(customer.getName());
        if (request.getInvoiceDate() != null) invoice.setInvoiceDate(request.getInvoiceDate());
        if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
        invoice.setPaymentTerms(request.getPaymentTerms());
        invoice.setSubject(request.getSubject());
        invoice.setNotes(request.getNotes());
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO);
        invoice.setFreightPaidAdvance(request.getFreightPaidAdvance() != null ? request.getFreightPaidAdvance() : BigDecimal.ZERO);
        invoice.setPackingForwarding(request.getPackingForwarding() != null ? request.getPackingForwarding() : BigDecimal.ZERO);
        invoice.setTdsAmount(request.getTdsAmount() != null ? request.getTdsAmount() : BigDecimal.ZERO);
        invoice.setSurchargeAmount(request.getSurchargeAmount() != null ? request.getSurchargeAmount() : BigDecimal.ZERO);
        invoice.setRoundedValue(request.getRoundedValue() != null ? request.getRoundedValue() : BigDecimal.ZERO);
        invoice.setBalanceAfterSubmission(request.getBalanceAfterSubmission() != null ? request.getBalanceAfterSubmission() : BigDecimal.ZERO);
        if (request.getSalesArea() != null) invoice.setSalesArea(request.getSalesArea());
        if (request.getFromLocation() != null) invoice.setFromLocation(request.getFromLocation());
        if (request.getLorryNumber() != null) invoice.setLorryNumber(request.getLorryNumber());
        if (request.getWayBillNumber() != null) invoice.setWayBillNumber(request.getWayBillNumber());
        if (request.getRrRlNumber() != null) invoice.setRrRlNumber(request.getRrRlNumber());
        if (request.getCarrier() != null) invoice.setCarrier(request.getCarrier());
        if (request.getLicenseNumber() != null) invoice.setLicenseNumber(request.getLicenseNumber());
        if (request.getDcComments() != null) invoice.setDcComments(request.getDcComments());
        if (request.getInvoiceComments() != null) invoice.setInvoiceComments(request.getInvoiceComments());
        if (request.getSalesOrderId() != null) invoice.setSalesOrderId(request.getSalesOrderId());

        if (request.getItems() != null) {
            invoice.getItems().clear();
            invoice.getItems().addAll(buildInvoiceItems(invoice, request.getItems()));
            calculateTotals(invoice);
            invoice.setBalanceDue(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Invoice updated: id={}", invoice.getId());
        return toDto(invoice);
    }

    @Transactional
    public InvoiceDto recordPayment(UUID id, BigDecimal amount, String paymentMethod, String reference) {
        return recordPayment(id, amount, paymentMethod, reference, reference, null);
    }

    @Transactional
    public InvoiceDto recordPayment(UUID id, BigDecimal amount, String paymentMethod, String reference, String chequeNumber, String chequeDate) {
        Invoice invoice = findOrThrow(id);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw AppException.badRequest("Cannot record payment on cancelled invoice");
        }
        BigDecimal newPaid = invoice.getPaidAmount().add(amount);
        if (newPaid.compareTo(invoice.getTotalAmount()) > 0) {
            throw AppException.badRequest("Payment amount exceeds invoice total");
        }
        invoice.setPaidAmount(newPaid);
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(newPaid));
        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        Invoice saved = invoiceRepository.save(invoice);

        // Cash/Bank receipt journal entry (Voucher Entry): DR Bank/Cash / CR AR (for every payment, partial or full)
        try {
            createPaymentReceiptJournalEntry(tenantContext.current(), saved, amount, paymentMethod, reference, chequeNumber, chequeDate);
        } catch (Exception e) {
            log.warn("Payment receipt JE skipped for invoice {}: {}", saved.getInvoiceNumber(), e.getMessage());
        }

        // Reduce customer outstanding when payment fully clears
        if (saved.getStatus() == InvoiceStatus.PAID) {
            customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), saved.getCustomerId())
                .ifPresent(c -> {
                    BigDecimal newOutstanding = (c.getOutstandingBalance() != null ? c.getOutstandingBalance() : BigDecimal.ZERO)
                        .subtract(saved.getTotalAmount());
                    c.setOutstandingBalance(newOutstanding.max(BigDecimal.ZERO));
                    customerRepository.save(c);
                });
            // Mark linked SO as DELIVERED when invoice is fully paid
            if (saved.getSalesOrderId() != null) {
                salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), saved.getSalesOrderId())
                    .ifPresent(so -> {
                        so.setStatus(SalesOrder.SalesOrderStatus.DELIVERED);
                        salesOrderRepository.save(so);
                        log.info("Sales order {} marked DELIVERED after invoice {} paid", so.getOrderNumber(), saved.getInvoiceNumber());
                    });
            }
        }

        return toDto(saved);
    }

    private void createPaymentReceiptJournalEntry(UUID tenantId, Invoice invoice, BigDecimal amount, String paymentMethod, String reference, String chequeNumber, String chequeDate) {
        String subType = "CASH".equalsIgnoreCase(paymentMethod) ? "CASH_ACCOUNT" : "BANK_ACCOUNT";
        List<Account> bankAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, subType);
        if (bankAccounts.isEmpty()) {
            bankAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
        }
        List<Account> arAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_RECEIVABLE");
        if (bankAccounts.isEmpty() || arAccounts.isEmpty()) {
            log.debug("Payment receipt JE skipped for {}: accounts not configured", invoice.getInvoiceNumber());
            return;
        }
        Account bankAccount = bankAccounts.get(0);
        Account arAccount   = arAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("PAYMENT");
        je.setReferenceId(invoice.getId());
        je.setReferenceNumber(invoice.getInvoiceNumber());

        String chkStr = (chequeNumber != null && !chequeNumber.isBlank()) ? " (Cheque/Ref #: " + chequeNumber + ")" : "";
        je.setDescription("Payment received (" + (paymentMethod != null ? paymentMethod : "BANK") + chkStr + "): " + invoice.getInvoiceNumber() + " — " + invoice.getCustomerName());
        je.setEntryDate(LocalDate.now());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(bankAccount.getId());
        drLine.setAccountCode(bankAccount.getCode());
        drLine.setAccountName(bankAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("Payment received " + chkStr + " — " + invoice.getInvoiceNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(arAccount.getId());
        crLine.setAccountCode(arAccount.getCode());
        crLine.setAccountName(arAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("AR cleared — " + invoice.getInvoiceNumber());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("Payment receipt Voucher JE created for invoice {} with cheque # {}", invoice.getInvoiceNumber(), chequeNumber);
    }

    @Transactional
    public InvoiceDto updateStatus(UUID id, InvoiceStatus status) {
        UUID tenantId = tenantContext.current();
        Invoice invoice = findOrThrow(id);
        invoice.setStatus(status);
        Invoice saved = invoiceRepository.save(invoice);

        if (status == InvoiceStatus.SENT) {
            autoApplyAdvances(tenantId, saved);
            createSalesVoucher(tenantId, saved);
        }

        if (status == InvoiceStatus.CANCELLED) {
            // Reverse the outstanding balance that was added when invoice was created
            customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, saved.getCustomerId())
                .ifPresent(c -> {
                    BigDecimal outstanding = c.getOutstandingBalance() != null ? c.getOutstandingBalance() : BigDecimal.ZERO;
                    BigDecimal remaining = saved.getBalanceDue() != null ? saved.getBalanceDue() : saved.getTotalAmount();
                    c.setOutstandingBalance(outstanding.subtract(remaining).max(BigDecimal.ZERO));
                    customerRepository.save(c);
                });
            // If payments were already recorded, create a reversal JE to un-do the bank debit and AR credit
            BigDecimal paidSoFar = saved.getPaidAmount() != null ? saved.getPaidAmount() : BigDecimal.ZERO;
            if (paidSoFar.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    createPaymentReversalJournalEntry(tenantId, saved, paidSoFar);
                } catch (Exception e) {
                    log.warn("Payment reversal JE skipped for cancelled invoice {}: {}", saved.getInvoiceNumber(), e.getMessage());
                }
            }
            log.info("Invoice {} cancelled — customer outstanding reversed", saved.getInvoiceNumber());
        }
        return toDto(saved);
    }

    private void autoApplyAdvances(UUID tenantId, Invoice invoice) {
        if (invoice.getBalanceDue() == null || invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) return;
        List<CustomerAdvance> advances = advanceRepository.findByTenantIdAndCustomerIdAndStatusAndDeletedAtIsNull(
                tenantId, invoice.getCustomerId(), AdvanceStatus.AVAILABLE);
        advances.addAll(advanceRepository.findByTenantIdAndCustomerIdAndStatusAndDeletedAtIsNull(
                tenantId, invoice.getCustomerId(), AdvanceStatus.PARTIALLY_APPLIED));

        BigDecimal remaining = invoice.getBalanceDue();
        BigDecimal totalApplied = BigDecimal.ZERO;
        for (CustomerAdvance adv : advances) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal apply = adv.getAmountAvailable().min(remaining);
            adv.setAmountApplied(adv.getAmountApplied().add(apply));
            adv.setAmountAvailable(adv.getAmountAvailable().subtract(apply));
            adv.setStatus(adv.getAmountAvailable().compareTo(BigDecimal.ZERO) == 0
                    ? AdvanceStatus.FULLY_APPLIED : AdvanceStatus.PARTIALLY_APPLIED);
            advanceRepository.save(adv);
            remaining = remaining.subtract(apply);
            totalApplied = totalApplied.add(apply);
        }
        if (totalApplied.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setPaidAmount(invoice.getPaidAmount().add(totalApplied));
            invoice.setBalanceDue(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0)
                invoice.setStatus(InvoiceStatus.PAID);
            else
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            invoiceRepository.save(invoice);
            log.info("Auto-applied customer advance {} to invoice {}", totalApplied, invoice.getInvoiceNumber());
        }
    }

    private void createPaymentReceiptJournalEntry(UUID tenantId, Invoice invoice, BigDecimal amount) {
        List<Account> bankAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
        List<Account> arAccounts   = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_RECEIVABLE");
        if (bankAccounts.isEmpty() || arAccounts.isEmpty()) {
            log.debug("Payment receipt JE skipped for {}: accounts not configured", invoice.getInvoiceNumber());
            return;
        }
        Account bankAccount = bankAccounts.get(0);
        Account arAccount   = arAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("PAYMENT");
        je.setReferenceId(invoice.getId());
        je.setReferenceNumber(invoice.getInvoiceNumber());
        je.setDescription("Payment received: " + invoice.getInvoiceNumber() + " — " + invoice.getCustomerName());
        je.setEntryDate(LocalDate.now());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(bankAccount.getId());
        drLine.setAccountCode(bankAccount.getCode());
        drLine.setAccountName(bankAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("Payment received — " + invoice.getInvoiceNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(arAccount.getId());
        crLine.setAccountCode(arAccount.getCode());
        crLine.setAccountName(arAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("AR cleared — " + invoice.getInvoiceNumber());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("Payment receipt JE (DRAFT) created for invoice {}", invoice.getInvoiceNumber());
    }

    private void createPaymentReversalJournalEntry(UUID tenantId, Invoice invoice, BigDecimal amount) {
        List<Account> bankAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK_ACCOUNT");
        List<Account> arAccounts   = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_RECEIVABLE");
        if (bankAccounts.isEmpty() || arAccounts.isEmpty()) return;
        Account bankAccount = bankAccounts.get(0);
        Account arAccount   = arAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("PAYMENT_REVERSAL");
        je.setReferenceId(invoice.getId());
        je.setReferenceNumber(invoice.getInvoiceNumber());
        je.setDescription("Payment reversal on cancel: " + invoice.getInvoiceNumber() + " — " + invoice.getCustomerName());
        je.setEntryDate(LocalDate.now());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(arAccount.getId());
        drLine.setAccountCode(arAccount.getCode());
        drLine.setAccountName(arAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("AR reversal — " + invoice.getInvoiceNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(bankAccount.getId());
        crLine.setAccountCode(bankAccount.getCode());
        crLine.setAccountName(bankAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("Payment reversal — " + invoice.getInvoiceNumber());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("Payment reversal JE (DRAFT) created for cancelled invoice {}", invoice.getInvoiceNumber());
    }

    private void createSalesVoucher(UUID tenantId, Invoice invoice) {
        try {
            // Idempotent: exactly one sales voucher per invoice. Skip if it was already posted (e.g.
            // auto-posted on dispatch or completed earlier) or a sales voucher already exists in the
            // ledger — so repeated status changes or edits never create duplicate vouchers.
            if (invoice.isPosted()) return;
            if (journalEntryRepository.existsByTenantIdAndReferenceIdAndReferenceTypeInAndDeletedAtIsNull(
                    tenantId, invoice.getId(), java.util.List.of("SALES_INVOICE", "INVOICE")) ||
                (invoice.getInvoiceNumber() != null && journalEntryRepository.existsByTenantIdAndReferenceNumberAndReferenceTypeInAndDeletedAtIsNull(
                    tenantId, invoice.getInvoiceNumber(), java.util.List.of("SALES_INVOICE", "INVOICE")))) {
                invoice.setPosted(true);
                invoiceRepository.save(invoice);
                return;
            }

            List<Account> arAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_RECEIVABLE");
            List<Account> revenueAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "SALES_REVENUE");
            if (arAccounts.isEmpty() || revenueAccounts.isEmpty()) {
                log.debug("Auto-voucher skipped for invoice {}: AR or Revenue account not configured", invoice.getInvoiceNumber());
                return;
            }
            Account arAccount = arAccounts.get(0);
            Account revenueAccount = revenueAccounts.get(0);
            BigDecimal amount = invoice.getTotalAmount();

            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("INVOICE");
            je.setReferenceId(invoice.getId());
            je.setReferenceNumber(invoice.getInvoiceNumber());
            je.setDescription("Sales Invoice: " + invoice.getInvoiceNumber() + " — " + invoice.getCustomerName());
            je.setEntryDate(invoice.getInvoiceDate());

            JournalEntryLine drLine = new JournalEntryLine();
            drLine.setAccountId(arAccount.getId());
            drLine.setAccountCode(arAccount.getCode());
            drLine.setAccountName(arAccount.getName());
            drLine.setDebitAmount(amount);
            drLine.setCreditAmount(BigDecimal.ZERO);
            drLine.setDescription("Invoice " + invoice.getInvoiceNumber());

            JournalEntryLine crLine = new JournalEntryLine();
            crLine.setAccountId(revenueAccount.getId());
            crLine.setAccountCode(revenueAccount.getCode());
            crLine.setAccountName(revenueAccount.getName());
            crLine.setDebitAmount(BigDecimal.ZERO);
            crLine.setCreditAmount(amount);
            crLine.setDescription("Sales Revenue — " + invoice.getInvoiceNumber());

            je.getLines().add(drLine);
            je.getLines().add(crLine);
            JournalEntry saved = journalEntryService.create(je);
            invoice.setPosted(true);
            invoice.setJournalEntryId(saved.getId());
            invoice.setJournalEntryNumber(saved.getEntryNumber());
            invoiceRepository.save(invoice);
            log.info("Auto-voucher created for invoice {} (JE {})", invoice.getInvoiceNumber(), saved.getEntryNumber());
        } catch (Exception e) {
            log.warn("Auto-voucher creation failed for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
        }
    }

    @Transactional
    public void delete(UUID id) {
        Invoice invoice = findOrThrow(id);
        invoice.setDeletedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
        log.info("Invoice soft-deleted: id={}", id);
    }

    private List<InvoiceItem> buildInvoiceItems(Invoice invoice,
            List<CreateInvoiceRequest.CreateInvoiceItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setDescription(r.getDescription());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());
            item.setUnitPrice(r.getUnitPrice());
            item.setTaxPercent(r.getTaxPercent() != null ? r.getTaxPercent() : BigDecimal.ZERO);
            item.setPackType(r.getPackType());
            item.setPackSize(r.getPackSize() != null ? r.getPackSize() : BigDecimal.ZERO);
            item.setForwardingCharges(r.getForwardingCharges() != null ? r.getForwardingCharges() : BigDecimal.ZERO);
            item.setSurchargePercent(r.getSurchargePercent() != null ? r.getSurchargePercent() : BigDecimal.ZERO);
            // Resolve discountPercent from either PCT type or FIXED type
            if ("FIXED".equalsIgnoreCase(r.getDiscountType())) {
                item.setDiscountPercent(BigDecimal.ZERO);
                item.setDiscountAmount(r.getDiscountAmount() != null ? r.getDiscountAmount() : BigDecimal.ZERO);
            } else {
                item.setDiscountPercent(r.getDiscountPercent() != null ? r.getDiscountPercent() : BigDecimal.ZERO);
                item.setDiscountAmount(BigDecimal.ZERO);
            }
            return item;
        }).collect(Collectors.toList());
    }

    private void calculateTotals(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (InvoiceItem item : invoice.getItems()) {
            BigDecimal gross = item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discAmt;
            if (item.getDiscountAmount() != null && item.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discAmt = item.getDiscountAmount().min(gross);
            } else {
                BigDecimal pct = item.getDiscountPercent() != null ? item.getDiscountPercent() : BigDecimal.ZERO;
                discAmt = gross.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            item.setDiscountAmount(discAmt);
            BigDecimal net = gross.subtract(discAmt);
            BigDecimal tax = net.multiply(item.getTaxPercent() != null ? item.getTaxPercent() : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setTaxAmount(tax);
            BigDecimal surchargePct = item.getSurchargePercent() != null ? item.getSurchargePercent() : BigDecimal.ZERO;
            BigDecimal surchAmt = net.multiply(surchargePct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setSurchargeAmount(surchAmt);
            item.setTotalAmount(net.add(tax).add(surchAmt));
            subtotal = subtotal.add(net);
            taxTotal = taxTotal.add(tax);
        }
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxTotal);
        BigDecimal headerDisc = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal freight = invoice.getFreightCharges() != null ? invoice.getFreightCharges() : BigDecimal.ZERO;
        BigDecimal pf = invoice.getPackingForwarding() != null ? invoice.getPackingForwarding() : BigDecimal.ZERO;
        invoice.setTotalAmount(subtotal.add(taxTotal).subtract(headerDisc).add(freight).add(pf));
    }

    private InvoiceDto toDto(Invoice inv) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(inv.getId());
        dto.setTenantId(inv.getTenantId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setCustomerId(inv.getCustomerId());
        dto.setCustomerName(inv.getCustomerName());
        dto.setSalesOrderId(inv.getSalesOrderId());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setDueDate(inv.getDueDate());
        dto.setStatus(inv.getStatus());
        dto.setSubtotal(inv.getSubtotal());
        dto.setTaxAmount(inv.getTaxAmount());
        dto.setDiscountAmount(inv.getDiscountAmount());
        dto.setFreightCharges(inv.getFreightCharges());
        dto.setFreightPaidAdvance(inv.getFreightPaidAdvance());
        dto.setPackingForwarding(inv.getPackingForwarding());
        dto.setTdsAmount(inv.getTdsAmount());
        dto.setSurchargeAmount(inv.getSurchargeAmount());
        dto.setRoundedValue(inv.getRoundedValue());
        dto.setBalanceAfterSubmission(inv.getBalanceAfterSubmission());
        dto.setSalesArea(inv.getSalesArea());
        dto.setFromLocation(inv.getFromLocation());
        dto.setLorryNumber(inv.getLorryNumber());
        dto.setWayBillNumber(inv.getWayBillNumber());
        dto.setRrRlNumber(inv.getRrRlNumber());
        dto.setCarrier(inv.getCarrier());
        dto.setLicenseNumber(inv.getLicenseNumber());
        dto.setDispatchChallanNumber(inv.getDispatchChallanNumber());
        dto.setDcComments(inv.getDcComments());
        dto.setInvoiceComments(inv.getInvoiceComments());
        dto.setTotalAmount(inv.getTotalAmount());
        dto.setPaidAmount(inv.getPaidAmount());
        dto.setBalanceDue(inv.getBalanceDue());
        dto.setPaymentTerms(inv.getPaymentTerms());
        dto.setSubject(inv.getSubject());
        dto.setNotes(inv.getNotes());
        dto.setCreatedAt(inv.getCreatedAt());
        if (inv.getItems() != null) {
            dto.setItems(inv.getItems().stream().map(this::itemToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private InvoiceItemDto itemToDto(InvoiceItem item) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnit(item.getUnit());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountPercent(item.getDiscountPercent());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setTaxPercent(item.getTaxPercent());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setPackType(item.getPackType());
        dto.setPackSize(item.getPackSize());
        dto.setForwardingCharges(item.getForwardingCharges());
        dto.setSurchargePercent(item.getSurchargePercent());
        dto.setSurchargeAmount(item.getSurchargeAmount());
        return dto;
    }

    public String generateInvoiceHtml(UUID id) {
        UUID tenantId = tenantContext.current();
        Invoice inv = invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Invoice not found: " + id));
        return invoicePageHeader() + buildInvoiceBody(inv) + "</body></html>";
    }

    public String generateBatchHtml(List<UUID> ids) {
        UUID tenantId = tenantContext.current();
        StringBuilder out = new StringBuilder(invoicePageHeader());
        for (int i = 0; i < ids.size(); i++) {
            UUID invId = ids.get(i);
            Invoice inv = invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, invId)
                    .orElseThrow(() -> AppException.notFound("Invoice not found: " + invId));
            out.append(buildInvoiceBody(inv));
            if (i < ids.size() - 1) out.append("<div style='page-break-after:always'></div>");
        }
        out.append("</body></html>");
        return out.toString();
    }

    private String invoicePageHeader() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Invoice</title>" +
               "<style>" +
               "body{font-family:Arial,sans-serif;font-size:12px;color:#222;margin:0;padding:0}" +
               com.erp.platform.modules.organization.service.CompanyLetterheadService.CSS +
               ".inv{max-width:800px;margin:0 auto;padding:24px}" +
               ".inv-hdr{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;padding-bottom:12px;border-bottom:2px solid #1565c0}" +
               ".inv-title{font-size:22px;font-weight:bold;color:#1565c0}" +
               ".imeta{border-collapse:collapse}" +
               ".imeta td{padding:2px 8px 2px 0;vertical-align:top;font-size:12px}" +
               ".imeta td.lbl{font-weight:bold;color:#555;white-space:nowrap}" +
               ".sec{margin-top:12px}" +
               ".sec-title{font-weight:bold;background:#f0f4ff;padding:4px 8px;margin-bottom:4px;border-left:3px solid #1565c0}" +
               "table.items{width:100%;border-collapse:collapse;margin-top:8px}" +
               "table.items th{background:#1565c0;color:#fff;padding:5px 6px;text-align:left;font-size:11px}" +
               "table.items td{border:1px solid #ddd;padding:4px 6px;font-size:11px}" +
               // `table.items th` outranks a bare `.num`, so the numeric headings printed
               // left-aligned over right-aligned figures and nothing in the money columns lined up.
               // Matching the selector's weight puts the heading back above its own column.
               "table.items th.num,table.items td.num{text-align:right}" +
               "table.items td.num{white-space:nowrap;font-variant-numeric:tabular-nums}" +
               ".num{text-align:right}" +
               ".tots{margin-top:16px;display:flex;justify-content:flex-end}" +
               ".tots-t{width:300px;border-collapse:collapse}" +
               ".tots-t td{padding:3px 8px;font-size:12px}" +
               ".tots-t td.lbl{font-weight:bold;color:#555}" +
               ".tots-t td.val{text-align:right;font-weight:bold}" +
               ".grand td{background:#1565c0;color:#fff;font-size:13px;font-weight:bold;padding:5px 8px}" +
               ".inv-foot{margin-top:16px;padding-top:12px;border-top:1px solid #ddd;font-size:11px;color:#555}" +
               ".sig{margin-top:48px;display:flex;justify-content:space-between}" +
               "@media print{.inv{padding:0}}" +
               "</style></head><body>";
    }

    private String buildInvoiceBody(Invoice inv) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        sb.append("<div class='inv'>");

        // The seller's letterhead. A tax invoice showed the customer's address under "Bill To"
        // but never the supplier's own name, address or GSTIN, which an invoice is required to
        // carry — and without it the document could not be identified as ours at all.
        sb.append(letterhead.html());

        // Header
        sb.append("<div class='inv-hdr'>")
          .append("<div><span class='inv-title'>TAX INVOICE</span></div>")
          .append("<table class='imeta'>")
          .append("<tr><td class='lbl'>Invoice No</td><td>").append(ei(inv.getInvoiceNumber())).append("</td></tr>")
          .append("<tr><td class='lbl'>Date</td><td>").append(inv.getInvoiceDate() != null ? inv.getInvoiceDate().format(df) : "").append("</td></tr>")
          .append("<tr><td class='lbl'>Due Date</td><td>").append(inv.getDueDate() != null ? inv.getDueDate().format(df) : "").append("</td></tr>")
          .append("<tr><td class='lbl'>Status</td><td>").append(ei(inv.getStatus() != null ? inv.getStatus().name() : "")).append("</td></tr>");
        if (inv(inv.getPaymentTerms())) sb.append("<tr><td class='lbl'>Terms</td><td>").append(ei(inv.getPaymentTerms())).append("</td></tr>");
        sb.append("</table></div>");

        // Bill To
        sb.append("<div class='sec'><div class='sec-title'>Bill To</div>")
          .append("<div style='padding:4px 8px;font-weight:bold;font-size:13px'>").append(ei(inv.getCustomerName())).append("</div>")
          .append("</div>");

        // Transport
        boolean hasT = inv(inv.getLorryNumber()) || inv(inv.getWayBillNumber()) || inv(inv.getFromLocation()) || inv(inv.getCarrier());
        if (hasT) {
            sb.append("<div class='sec'><div class='sec-title'>Transport</div>")
              .append("<table class='imeta'><tr>");
            if (inv(inv.getFromLocation())) sb.append("<td class='lbl'>From</td><td>").append(ei(inv.getFromLocation())).append("&nbsp;&nbsp;</td>");
            if (inv(inv.getLorryNumber()))  sb.append("<td class='lbl'>Lorry</td><td>").append(ei(inv.getLorryNumber())).append("&nbsp;&nbsp;</td>");
            if (inv(inv.getWayBillNumber())) sb.append("<td class='lbl'>Way Bill</td><td>").append(ei(inv.getWayBillNumber())).append("&nbsp;&nbsp;</td>");
            if (inv(inv.getRrRlNumber()))   sb.append("<td class='lbl'>RR/RL</td><td>").append(ei(inv.getRrRlNumber())).append("&nbsp;&nbsp;</td>");
            if (inv(inv.getCarrier()))      sb.append("<td class='lbl'>Carrier</td><td>").append(ei(inv.getCarrier())).append("</td>");
            sb.append("</tr></table></div>");
        }

        // Items table
        sb.append("<div class='sec'><table class='items'><thead><tr>")
          .append("<th>#</th><th>Product</th><th>Description</th><th>Pack</th>")
          .append("<th class='num'>Qty</th><th>Unit</th><th class='num'>Rate</th>")
          .append("<th class='num'>Disc%</th><th class='num'>Tax%</th><th class='num'>Amount</th>")
          .append("</tr></thead><tbody>");
        if (inv.getItems() != null) {
            int idx = 1;
            for (InvoiceItem item : inv.getItems()) {
                String pack = item.getPackType() != null
                        ? item.getPackType() + (item.getPackSize() != null ? "×" + item.getPackSize().stripTrailingZeros().toPlainString() : "") : "";
                sb.append("<tr>")
                  .append("<td>").append(idx++).append("</td>")
                  .append("<td>").append(ei(item.getProductName())).append("</td>")
                  .append("<td>").append(ei(item.getDescription())).append("</td>")
                  .append("<td>").append(pack).append("</td>")
                  .append("<td class='num'>").append(nf(item.getQuantity())).append("</td>")
                  .append("<td>").append(ei(item.getUnit())).append("</td>")
                  .append("<td class='num'>").append(nf(item.getUnitPrice())).append("</td>")
                  .append("<td class='num'>").append(nf(item.getDiscountPercent())).append("</td>")
                  .append("<td class='num'>").append(nf(item.getTaxPercent())).append("</td>")
                  .append("<td class='num'>").append(nf(item.getTotalAmount())).append("</td>")
                  .append("</tr>");
            }
        }
        sb.append("</tbody></table></div>");

        // Totals
        sb.append("<div class='tots'><table class='tots-t'>")
          .append("<tr><td class='lbl'>Subtotal</td><td class='val'>").append(nf(inv.getSubtotal())).append("</td></tr>");
        if (pos(inv.getDiscountAmount())) sb.append("<tr><td class='lbl'>Discount</td><td class='val'>- ").append(nf(inv.getDiscountAmount())).append("</td></tr>");
        if (pos(inv.getTaxAmount()))      sb.append("<tr><td class='lbl'>Tax</td><td class='val'>").append(nf(inv.getTaxAmount())).append("</td></tr>");
        if (pos(inv.getFreightCharges())) sb.append("<tr><td class='lbl'>Freight</td><td class='val'>").append(nf(inv.getFreightCharges())).append("</td></tr>");
        if (pos(inv.getPackingForwarding())) sb.append("<tr><td class='lbl'>P&amp;F</td><td class='val'>").append(nf(inv.getPackingForwarding())).append("</td></tr>");
        if (pos(inv.getTdsAmount()))      sb.append("<tr><td class='lbl'>TDS</td><td class='val'>- ").append(nf(inv.getTdsAmount())).append("</td></tr>");
        if (pos(inv.getSurchargeAmount())) sb.append("<tr><td class='lbl'>Surcharge</td><td class='val'>").append(nf(inv.getSurchargeAmount())).append("</td></tr>");
        if (inv.getRoundedValue() != null && inv.getRoundedValue().compareTo(BigDecimal.ZERO) != 0)
            sb.append("<tr><td class='lbl'>Rounding</td><td class='val'>").append(nf(inv.getRoundedValue())).append("</td></tr>");
        sb.append("<tr class='grand'><td>TOTAL (&#8377;)</td><td class='val'>").append(nf(inv.getTotalAmount())).append("</td></tr>");
        if (pos(inv.getPaidAmount()))
            sb.append("<tr><td class='lbl'>Paid</td><td class='val' style='color:green'>").append(nf(inv.getPaidAmount())).append("</td></tr>")
              .append("<tr><td class='lbl' style='color:red'>Balance Due</td><td class='val' style='color:red'>").append(nf(inv.getBalanceDue())).append("</td></tr>");
        sb.append("</table></div>");

        // Footer
        if (inv(inv.getInvoiceComments()) || inv(inv.getNotes())) {
            sb.append("<div class='inv-foot'>");
            if (inv(inv.getInvoiceComments())) sb.append("<strong>Comments:</strong> ").append(ei(inv.getInvoiceComments())).append("<br>");
            if (inv(inv.getNotes())) sb.append("<strong>Notes:</strong> ").append(ei(inv.getNotes()));
            sb.append("</div>");
        }

        sb.append("<div class='sig'>")
          .append("<div><br><br><hr style='width:180px;margin:0'>Authorised Signatory</div>")
          .append("<div><br><br><hr style='width:180px;margin:0'>Received By / Stamp</div>")
          .append("</div></div>");
        return sb.toString();
    }

    private boolean inv(String s) { return s != null && !s.isBlank(); }
    private boolean pos(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) > 0; }
    private String nf(BigDecimal v) { return v != null ? String.format("%.2f", v) : "—"; }
    private String ei(String s) { return s != null ? s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : ""; }

    private Invoice findOrThrow(UUID id) {
        return invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Invoice not found: " + id));
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
