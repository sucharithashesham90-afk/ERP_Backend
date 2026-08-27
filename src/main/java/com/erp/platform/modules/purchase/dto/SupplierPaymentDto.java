package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.SupplierPayment.PaymentMethod;
import com.erp.platform.modules.purchase.entity.SupplierPayment.PayStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SupplierPaymentDto {

    private UUID id;
    private String paymentNumber;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseInvoiceId;
    private String invoiceNumber;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
    private BigDecimal exchangeRate;
    private UUID bankAccountId;
    private String bankAccountName;
    private String referenceNumber;
    private BigDecimal tdsAmount;
    private BigDecimal tdsPercent;
    private BigDecimal netPayment;
    private PayStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime processedAt;
    private String notes;
    private List<AllocationDto> allocations;
    private LocalDateTime createdAt;

    @Data
    public static class AllocationDto {
        private UUID id;
        private UUID purchaseInvoiceId;
        private String invoiceNumber;
        private BigDecimal invoiceAmount;
        private BigDecimal allocatedAmount;
        private BigDecimal outstandingAfter;
    }
}
