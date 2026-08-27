package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class PurchaseInvoiceDto {
    private UUID id;
    private UUID tenantId;
    private String piNumber;
    private String vendorInvoiceNumber;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseOrderId;
    private UUID goodsReceiptId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private PIStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal freightCharges;
    private BigDecimal exciseDuty;
    private BigDecimal pfCharge;
    private boolean tdsApplicable;
    private String tdsSection;
    private BigDecimal tdsPercent;
    private BigDecimal tdsAmount;
    private BigDecimal totalAmount;
    private BigDecimal netPayable;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private String paymentTerms;
    private String notes;
    private List<Map<String, Object>> items;
    private LocalDateTime createdAt;
}
