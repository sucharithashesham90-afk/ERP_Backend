package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ReconciliationDto {

    @Data
    public static class ArSummary {
        private BigDecimal totalInvoiced;
        private BigDecimal totalPaid;
        private BigDecimal totalOutstanding;
        private List<PartyBalance> customers;
    }

    @Data
    public static class ApSummary {
        private BigDecimal totalPayable;
        private BigDecimal totalPaid;
        private BigDecimal totalOutstanding;
        private List<PartyBalance> vendors;
    }

    @Data
    public static class PartyBalance {
        private UUID partyId;
        private String partyName;
        private BigDecimal totalInvoiced;
        private BigDecimal totalPaid;
        private BigDecimal balanceDue;
        private int openDocuments;
    }

    @Data
    public static class ArDetail {
        private UUID customerId;
        private String customerName;
        private BigDecimal totalInvoiced;
        private BigDecimal totalPaid;
        private BigDecimal balanceDue;
        private List<InvoiceLine> invoices;
    }

    @Data
    public static class ApDetail {
        private UUID vendorId;
        private String vendorName;
        private BigDecimal totalPayable;
        private BigDecimal totalPaid;
        private BigDecimal balanceDue;
        private List<InvoiceLine> invoices;
    }

    @Data
    public static class InvoiceLine {
        private UUID id;
        private String documentNumber;
        private LocalDate documentDate;
        private LocalDate dueDate;
        private String status;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal balanceDue;
        private boolean overdue;
    }
}
