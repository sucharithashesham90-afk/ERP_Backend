package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.sales.dto.CreateSalesReturnRequest;
import com.erp.platform.modules.sales.dto.SalesReturnDto;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.entity.SalesReturn;
import com.erp.platform.modules.sales.entity.SalesReturnItem;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import com.erp.platform.modules.sales.repository.SalesReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;
    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StockService stockService;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final com.erp.platform.modules.accounting.repository.JournalEntryRepository journalEntryRepository;
    private final SalesLedgerService salesLedgerService;
    /** A credit note is a document in its own right, not only a ledger posting. */
    private final com.erp.platform.modules.accounting.repository.CreditNoteRepository creditNoteRepository;
    private final SalesStockService salesStockService;
    private final TenantContext tenantContext;

    public PageResponse<SalesReturnDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(salesReturnRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public SalesReturnDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SalesReturnDto create(CreateSalesReturnRequest request) {
        UUID tenantId = tenantContext.current();

        SalesReturn ret = new SalesReturn();
        ret.setTenantId(tenantId);
        ret.setReturnNumber(generateNumber());
        ret.setReturnDate(LocalDate.now());
        ret.setReason(request.getReason());
        ret.setNotes(request.getNotes());
        ret.setStatus("DRAFT");

        if (request.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getInvoiceId())
                    .orElseThrow(() -> AppException.notFound("Invoice not found: " + request.getInvoiceId()));
            ret.setInvoiceId(invoice.getId());
            ret.setSalesOrderId(invoice.getSalesOrderId());
            ret.setCustomerId(invoice.getCustomerId());
            ret.setCustomerName(invoice.getCustomerName());
        } else if (request.getSalesOrderId() != null) {
            SalesOrder order = salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getSalesOrderId())
                    .orElseThrow(() -> AppException.notFound("Sales order not found: " + request.getSalesOrderId()));
            ret.setSalesOrderId(order.getId());
            ret.setCustomerId(order.getCustomerId());
            ret.setCustomerName(order.getCustomerName());
        } else {
            ret.setCustomerId(request.getCustomerId());
            ret.setCustomerName(request.getCustomerName());
        }

        List<SalesReturnItem> items = buildItems(ret, tenantId, request.getItems());
        ret.setItems(items);

        BigDecimal total = items.stream()
                .map(SalesReturnItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ret.setTotalAmount(total);

        ret = salesReturnRepository.save(ret);
        log.info("Sales return created: id={}, number={}", ret.getId(), ret.getReturnNumber());
        // No credit note here. A return starts as a DRAFT claim that the goods are coming back;
        // nothing has been accepted yet, so there is nothing to credit the customer for. Raising
        // the note at this point put a POSTED entry in the ledger against a draft, and then
        // approving the same return raised a second one for the same goods.
        return toDto(ret);
    }

    @Transactional
    public SalesReturnDto approve(UUID id) {
        SalesReturn ret = findOrThrow(id);
        if (!"DRAFT".equals(ret.getStatus()) && !"PENDING_REVIEW".equals(ret.getStatus())) {
            throw AppException.badRequest("Only DRAFT returns can be approved");
        }

        List<SalesReturnItem> items = ret.getItems();
        if (items != null && !items.isEmpty()) {
            UUID tenantId = tenantContext.current();
            for (SalesReturnItem item : items) {
                BigDecimal qty = item.getQuantity();
                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) continue;

                String lotNo = (item.getLotNumber() != null && !item.getLotNumber().isBlank())
                        ? item.getLotNumber()
                        : "SR-" + (ret.getReturnNumber() != null ? ret.getReturnNumber() : "RET") + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

                String prodName = item.getProductName() != null && !item.getProductName().isBlank()
                        ? item.getProductName()
                        : "Returned Seed Product";

                try {
                    salesStockService.receiveToLot(tenantId, lotNo, qty, prodName, "SALES_RETURN");
                    log.info("Stock returned to lot {} for return {}: qty={}", lotNo, ret.getReturnNumber(), qty);
                } catch (Exception e) {
                    log.warn("Stock restore handled for lot {}: {}", lotNo, e.getMessage());
                }
            }
        }

        ret.setStatus("APPROVED");
        ret = salesReturnRepository.save(ret);
        log.info("Sales return approved: id={}, number={}", ret.getId(), ret.getReturnNumber());
        try {
            createCreditNoteJournalEntry(tenantContext.current(), ret);
        } catch (Exception e) {
            log.warn("Credit note JE skipped for {}: {}", ret.getReturnNumber(), e.getMessage());
        }
        return toDto(ret);
    }

    private void createCreditNoteJournalEntry(UUID tenantId, SalesReturn ret) {
        BigDecimal amount = ret.getTotalAmount() != null ? ret.getTotalAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;

        // One return, one credit to the customer. postEntry writes a new journal entry every time
        // it is called and has no notion of having posted this return already, so without this the
        // ledger gains a fresh 50,000 credit on every approval, retry or replay - and the customer
        // balance is wrong by that much each time, silently.
        if (journalEntryRepository.existsByTenantIdAndReferenceIdAndReferenceTypeInAndDeletedAtIsNull(
                tenantId, ret.getId(), java.util.List.of("SALES_RETURN"))) {
            log.info("Credit note already posted for sales return {} - not posting again",
                    ret.getReturnNumber());
            return;
        }

        try {
            Account salesReturns = salesLedgerService.resolveSalesReturnsLedger(tenantId);
            String custName = (ret.getCustomerName() != null && !ret.getCustomerName().isBlank()) ? ret.getCustomerName() : "General Customer";
            Account customerLedger = salesLedgerService.resolveCustomerLedger(tenantId, custName);
            salesLedgerService.postEntry(salesReturns, customerLedger, amount,
                    "SALES_RETURN", ret.getId(), ret.getReturnNumber(),
                    "Sales Return Credit Note: " + ret.getReturnNumber() + " — " + custName,
                    ret.getReturnDate());
            log.info("Credit note posted to customer ledger for sales return {}", ret.getReturnNumber());
        } catch (Exception e) {
            log.warn("Credit note posting skipped for {}: {}", ret.getReturnNumber(), e.getMessage());
        }

        // The posting alone was all that happened, so the screen said a credit note had been
        // created and Accounting > Credit Notes stayed empty — the register lists CreditNote
        // records, and none was ever written. The entity has carried a SALES_RETURN type and a
        // salesReturnId all along; nothing was filling them in.
        try {
            recordCreditNote(tenantId, ret, amount);
        } catch (Exception e) {
            log.warn("Credit note document not recorded for {}: {}", ret.getReturnNumber(), e.getMessage());
        }
    }

    /** Writes the credit note itself, once per return. */
    private void recordCreditNote(UUID tenantId, SalesReturn ret, BigDecimal amount) {
        if (creditNoteRepository.existsByTenantIdAndSalesReturnIdAndDeletedAtIsNull(tenantId, ret.getId())) {
            return;   // approving twice must not raise two notes for the same goods
        }
        var note = new com.erp.platform.modules.accounting.entity.CreditNote();
        note.setTenantId(tenantId);
        note.setCreditNoteNumber("CN-" + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        note.setNoteType("SALES_RETURN");
        note.setNoteDate(ret.getReturnDate() != null ? ret.getReturnDate() : java.time.LocalDate.now());
        note.setSalesReturnId(ret.getId());
        note.setBillNo(ret.getReturnNumber());
        note.setPartyName(ret.getCustomerName());
        note.setNarration("Credit note for sales return " + ret.getReturnNumber());
        note.setTotalAmount(amount);
        note.setStatus("POSTED");
        creditNoteRepository.save(note);
        log.info("Credit note {} recorded for sales return {}", note.getCreditNoteNumber(), ret.getReturnNumber());
    }

    @Transactional
    public void delete(UUID id) {
        SalesReturn ret = findOrThrow(id);
        if ("APPROVED".equals(ret.getStatus())) {
            throw AppException.badRequest("Approved returns cannot be deleted");
        }
        ret.setDeletedAt(LocalDateTime.now());
        salesReturnRepository.save(ret);
    }

    private List<SalesReturnItem> buildItems(SalesReturn ret, UUID tenantId,
            List<CreateSalesReturnRequest.ItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            SalesReturnItem item = new SalesReturnItem();
            item.setTenantId(tenantId);
            item.setSalesReturn(ret);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setCropId(r.getCropId());
            item.setCropName(r.getCropName());
            item.setVarietyId(r.getVarietyId());
            item.setVarietyName(r.getVarietyName());
            item.setBagSizeId(r.getBagSizeId());
            item.setBagSizeName(r.getBagSizeName());
            item.setBagTypeId(r.getBagTypeId());
            item.setBagTypeName(r.getBagTypeName());
            item.setLotNumber(r.getLotNumber());
            item.setQuantity(r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO);
            item.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : BigDecimal.ZERO);
            item.setUnit(r.getUnit());
            item.setWarehouseId(r.getWarehouseId());
            item.setWarehouseName(r.getWarehouseName());
            item.setRemarks(r.getRemarks());
            BigDecimal total = item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            item.setTotalAmount(total);
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public SalesReturnDto recordPayment(UUID id, BigDecimal amount, String paymentMethod, String chequeNumber, String chequeDate) {
        SalesReturn ret = findOrThrow(id);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Payment amount must be greater than zero");
        }
        ret.setPaymentStatus("PAID");
        ret.setPaidAmount(amount);
        ret.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        ret.setChequeNumber(chequeNumber);
        ret.setChequeDate(chequeDate);
        ret = salesReturnRepository.save(ret);

        try {
            UUID tenantId = tenantContext.current();
            String custName = (ret.getCustomerName() != null && !ret.getCustomerName().isBlank()) ? ret.getCustomerName() : "General Customer";
            Account customerLedger = salesLedgerService.resolveCustomerLedger(tenantId, custName);
            Account salesReturns = salesLedgerService.resolveSalesReturnsLedger(tenantId);
            salesLedgerService.postEntry(customerLedger, salesReturns, amount,
                    "CREDIT_NOTE", ret.getId(), "CN-" + ret.getReturnNumber(),
                    "Sales Return Credit Note Refund (" + paymentMethod + (chequeNumber != null ? " Cheque #" + chequeNumber : "") + ") — " + custName,
                    ret.getReturnDate());
            log.info("Credit note refund voucher entry generated for return {}", ret.getReturnNumber());
        } catch (Exception e) {
            log.warn("Credit note refund voucher entry skipped: {}", e.getMessage());
        }
        return toDto(ret);
    }

    private SalesReturnDto toDto(SalesReturn ret) {
        SalesReturnDto dto = new SalesReturnDto();
        dto.setId(ret.getId());
        dto.setTenantId(ret.getTenantId());
        dto.setReturnNumber(ret.getReturnNumber());
        dto.setInvoiceId(ret.getInvoiceId());
        dto.setSalesOrderId(ret.getSalesOrderId());
        dto.setCustomerId(ret.getCustomerId());
        dto.setCustomerName(ret.getCustomerName());
        dto.setReturnDate(ret.getReturnDate());
        dto.setReason(ret.getReason());
        dto.setNotes(ret.getNotes());
        dto.setTotalAmount(ret.getTotalAmount());
        dto.setStatus(ret.getStatus());
        dto.setPaymentStatus(ret.getPaymentStatus() != null ? ret.getPaymentStatus() : "UNPAID");
        dto.setPaidAmount(ret.getPaidAmount() != null ? ret.getPaidAmount() : BigDecimal.ZERO);
        dto.setPaymentMethod(ret.getPaymentMethod());
        dto.setChequeNumber(ret.getChequeNumber());
        dto.setChequeDate(ret.getChequeDate());
        dto.setCreatedAt(ret.getCreatedAt());
        if (ret.getItems() != null) {
            dto.setItems(ret.getItems().stream().map(item -> {
                SalesReturnDto.ItemDto idto = new SalesReturnDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setCropId(item.getCropId());
                idto.setCropName(item.getCropName());
                idto.setVarietyId(item.getVarietyId());
                idto.setVarietyName(item.getVarietyName());
                idto.setBagSizeId(item.getBagSizeId());
                idto.setBagSizeName(item.getBagSizeName());
                idto.setBagTypeId(item.getBagTypeId());
                idto.setBagTypeName(item.getBagTypeName());
                idto.setLotNumber(item.getLotNumber());
                idto.setQuantity(item.getQuantity());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setTotalAmount(item.getTotalAmount());
                idto.setUnit(item.getUnit());
                idto.setWarehouseId(item.getWarehouseId());
                idto.setWarehouseName(item.getWarehouseName());
                idto.setRemarks(item.getRemarks());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private SalesReturn findOrThrow(UUID id) {
        return salesReturnRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales return not found: " + id));
    }

    private String generateNumber() {
        return "SR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
