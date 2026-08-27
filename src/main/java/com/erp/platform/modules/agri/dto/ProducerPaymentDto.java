package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProducerPaymentDto {
    private UUID id;
    private String paymentNumber;
    private LocalDate paymentDate;
    private UUID fieldProducerId;
    private String fieldProducerName;
    private UUID producerContractId;
    private String contractNumber;
    private BigDecimal deliveredQuantity;
    private BigDecimal qualifiedQuantity;
    private BigDecimal rejectedQuantity;
    private BigDecimal ratePerUnit;
    private BigDecimal grossAmount;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private List<DeductionItemDto> deductionItems;

    @Data
    public static class DeductionItemDto {
        private UUID id;
        private UUID possibleDeductionId;
        private String deductionName;
        private String deductionType;
        private BigDecimal rate;
        private BigDecimal quantity;
        private BigDecimal amount;
        private String remarks;
    }
}
