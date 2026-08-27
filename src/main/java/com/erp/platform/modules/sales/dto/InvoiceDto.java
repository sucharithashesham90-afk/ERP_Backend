package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class InvoiceDto {
    private UUID id;
    private UUID tenantId;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private UUID salesOrderId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private BigDecimal freightCharges;
    private BigDecimal freightPaidAdvance;
    private BigDecimal packingForwarding;
    private BigDecimal tdsAmount;
    private BigDecimal surchargeAmount;
    private BigDecimal roundedValue;
    private BigDecimal balanceAfterSubmission;
    private String salesArea;
    private String fromLocation;
    private String lorryNumber;
    private String wayBillNumber;
    private String rrRlNumber;
    private String carrier;
    private String licenseNumber;
    private String dcComments;
    private String dispatchChallanNumber;
    private String invoiceComments;
    private String paymentTerms;
    private String subject;
    private String notes;
    private List<InvoiceItemDto> items;
    private LocalDateTime createdAt;
}
