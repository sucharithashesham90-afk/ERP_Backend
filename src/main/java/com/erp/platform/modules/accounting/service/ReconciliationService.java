package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.ReconciliationDto;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReconciliationService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final TenantContext tenantContext;

    public ReconciliationDto.ArSummary getArSummary() {
        UUID tenantId = tenantContext.current();
        List<Invoice> open = invoiceRepository.findOutstandingInvoices(tenantId);

        Map<UUID, List<Invoice>> byCustomer = open.stream()
                .collect(Collectors.groupingBy(Invoice::getCustomerId));

        List<ReconciliationDto.PartyBalance> customers = byCustomer.entrySet().stream().map(e -> {
            ReconciliationDto.PartyBalance pb = new ReconciliationDto.PartyBalance();
            pb.setPartyId(e.getKey());
            pb.setPartyName(e.getValue().get(0).getCustomerName());
            pb.setTotalInvoiced(e.getValue().stream()
                    .map(i -> i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setTotalPaid(e.getValue().stream()
                    .map(i -> i.getPaidAmount() != null ? i.getPaidAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setBalanceDue(e.getValue().stream()
                    .map(i -> i.getBalanceDue() != null ? i.getBalanceDue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setOpenDocuments(e.getValue().size());
            return pb;
        }).sorted(Comparator.comparing(ReconciliationDto.PartyBalance::getBalanceDue).reversed())
          .collect(Collectors.toList());

        ReconciliationDto.ArSummary summary = new ReconciliationDto.ArSummary();
        summary.setCustomers(customers);
        summary.setTotalInvoiced(customers.stream().map(ReconciliationDto.PartyBalance::getTotalInvoiced)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalPaid(customers.stream().map(ReconciliationDto.PartyBalance::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalOutstanding(customers.stream().map(ReconciliationDto.PartyBalance::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return summary;
    }

    public ReconciliationDto.ArDetail getArDetail(UUID customerId) {
        UUID tenantId = tenantContext.current();
        List<Invoice> open = invoiceRepository.findOutstandingInvoices(tenantId).stream()
                .filter(i -> customerId.equals(i.getCustomerId()))
                .collect(Collectors.toList());

        ReconciliationDto.ArDetail detail = new ReconciliationDto.ArDetail();
        detail.setCustomerId(customerId);
        detail.setCustomerName(open.isEmpty() ? "" : open.get(0).getCustomerName());
        detail.setTotalInvoiced(open.stream().map(i -> safe(i.getTotalAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        detail.setTotalPaid(open.stream().map(i -> safe(i.getPaidAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        detail.setBalanceDue(open.stream().map(i -> safe(i.getBalanceDue())).reduce(BigDecimal.ZERO, BigDecimal::add));

        LocalDate today = LocalDate.now();
        detail.setInvoices(open.stream().map(inv -> {
            ReconciliationDto.InvoiceLine line = new ReconciliationDto.InvoiceLine();
            line.setId(inv.getId());
            line.setDocumentNumber(inv.getInvoiceNumber());
            line.setDocumentDate(inv.getInvoiceDate());
            line.setDueDate(inv.getDueDate());
            line.setStatus(inv.getStatus() != null ? inv.getStatus().name() : "");
            line.setTotalAmount(safe(inv.getTotalAmount()));
            line.setPaidAmount(safe(inv.getPaidAmount()));
            line.setBalanceDue(safe(inv.getBalanceDue()));
            line.setOverdue(inv.getDueDate() != null && inv.getDueDate().isBefore(today));
            return line;
        }).sorted(Comparator.comparing(ReconciliationDto.InvoiceLine::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()));

        return detail;
    }

    public ReconciliationDto.ApSummary getApSummary() {
        UUID tenantId = tenantContext.current();
        List<PurchaseInvoice> open = purchaseInvoiceRepository.findOutstandingPayables(tenantId);

        Map<UUID, List<PurchaseInvoice>> byVendor = open.stream()
                .collect(Collectors.groupingBy(PurchaseInvoice::getVendorId));

        List<ReconciliationDto.PartyBalance> vendors = byVendor.entrySet().stream().map(e -> {
            ReconciliationDto.PartyBalance pb = new ReconciliationDto.PartyBalance();
            pb.setPartyId(e.getKey());
            pb.setPartyName(e.getValue().get(0).getVendorName());
            pb.setTotalInvoiced(e.getValue().stream()
                    .map(i -> safe(i.getTotalAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setTotalPaid(e.getValue().stream()
                    .map(i -> safe(i.getPaidAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setBalanceDue(e.getValue().stream()
                    .map(i -> safe(i.getBalanceDue())).reduce(BigDecimal.ZERO, BigDecimal::add));
            pb.setOpenDocuments(e.getValue().size());
            return pb;
        }).sorted(Comparator.comparing(ReconciliationDto.PartyBalance::getBalanceDue).reversed())
          .collect(Collectors.toList());

        ReconciliationDto.ApSummary summary = new ReconciliationDto.ApSummary();
        summary.setVendors(vendors);
        summary.setTotalPayable(vendors.stream().map(ReconciliationDto.PartyBalance::getTotalInvoiced)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalPaid(vendors.stream().map(ReconciliationDto.PartyBalance::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalOutstanding(vendors.stream().map(ReconciliationDto.PartyBalance::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return summary;
    }

    public ReconciliationDto.ApDetail getApDetail(UUID vendorId) {
        UUID tenantId = tenantContext.current();
        List<PurchaseInvoice> open = purchaseInvoiceRepository.findOutstandingPayables(tenantId).stream()
                .filter(i -> vendorId.equals(i.getVendorId()))
                .collect(Collectors.toList());

        ReconciliationDto.ApDetail detail = new ReconciliationDto.ApDetail();
        detail.setVendorId(vendorId);
        detail.setVendorName(open.isEmpty() ? "" : open.get(0).getVendorName());
        detail.setTotalPayable(open.stream().map(i -> safe(i.getTotalAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        detail.setTotalPaid(open.stream().map(i -> safe(i.getPaidAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
        detail.setBalanceDue(open.stream().map(i -> safe(i.getBalanceDue())).reduce(BigDecimal.ZERO, BigDecimal::add));

        LocalDate today = LocalDate.now();
        detail.setInvoices(open.stream().map(inv -> {
            ReconciliationDto.InvoiceLine line = new ReconciliationDto.InvoiceLine();
            line.setId(inv.getId());
            line.setDocumentNumber(inv.getPiNumber());
            line.setDocumentDate(inv.getInvoiceDate());
            line.setDueDate(inv.getDueDate());
            line.setStatus(inv.getStatus() != null ? inv.getStatus().name() : "");
            line.setTotalAmount(safe(inv.getTotalAmount()));
            line.setPaidAmount(safe(inv.getPaidAmount()));
            line.setBalanceDue(safe(inv.getBalanceDue()));
            line.setOverdue(inv.getDueDate() != null && inv.getDueDate().isBefore(today));
            return line;
        }).sorted(Comparator.comparing(ReconciliationDto.InvoiceLine::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList()));

        return detail;
    }

    private BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
