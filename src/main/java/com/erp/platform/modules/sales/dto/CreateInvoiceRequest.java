package com.erp.platform.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateInvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID salesOrderId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String paymentTerms;

    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal freightCharges = BigDecimal.ZERO;
    private BigDecimal freightPaidAdvance = BigDecimal.ZERO;
    private BigDecimal packingForwarding = BigDecimal.ZERO;
    private BigDecimal tdsAmount = BigDecimal.ZERO;
    private BigDecimal surchargeAmount = BigDecimal.ZERO;
    private BigDecimal roundedValue = BigDecimal.ZERO;
    private BigDecimal balanceAfterSubmission = BigDecimal.ZERO;
    private String salesArea;
    private String fromLocation;
    private String lorryNumber;
    private String wayBillNumber;
    private String rrRlNumber;
    private String carrier;
    private String licenseNumber;
    private String dcComments;
    private String invoiceComments;
    private String subject;
    private String notes;

    @Valid
    private List<CreateInvoiceItemRequest> items;

    @Data
    public static class CreateInvoiceItemRequest {
        // Optional: dispatch-generated invoice lines reference an SKU/lot, not a master product,
        // so productId can be null; the line is identified by productName/description.
        private UUID productId;
        private String productName;
        private String description;
        @NotNull
        private BigDecimal quantity;
        private String unit;
        @NotNull
        private BigDecimal unitPrice;
        private String discountType = "PCT"; // PCT or FIXED
        private BigDecimal discountPercent = BigDecimal.ZERO;
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private BigDecimal taxPercent = BigDecimal.ZERO;
        private String packType;
        private BigDecimal packSize = BigDecimal.ZERO;
        private BigDecimal forwardingCharges = BigDecimal.ZERO;
        private BigDecimal surchargePercent = BigDecimal.ZERO;
    }
}
