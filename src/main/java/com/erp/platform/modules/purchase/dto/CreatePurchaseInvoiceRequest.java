package com.erp.platform.modules.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CreatePurchaseInvoiceRequest {

    @NotNull(message = "Vendor ID is required")
    private UUID vendorId;

    private String vendorInvoiceNumber;
    private UUID purchaseOrderId;
    private UUID goodsReceiptId;

    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String paymentTerms;

    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal freightCharges = BigDecimal.ZERO;
    private BigDecimal exciseDuty = BigDecimal.ZERO;
    private BigDecimal pfCharge = BigDecimal.ZERO;

    // TDS fields
    private boolean tdsApplicable = false;
    private String tdsSection;
    private BigDecimal tdsPercent = BigDecimal.ZERO;

    private String notes;

    private List<Map<String, Object>> items;
}
