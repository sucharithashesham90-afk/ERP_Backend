package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.SupplierPayment.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSupplierPaymentRequest {

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
    private String notes;
    private List<AllocationRequest> allocations;

    @Data
    public static class AllocationRequest {
        private UUID invoiceId;
        private String invoiceNumber;
        private BigDecimal invoiceAmount;
        private BigDecimal allocatedAmount;
    }
}
