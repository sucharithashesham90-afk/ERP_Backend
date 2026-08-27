package com.erp.platform.modules.supplier.dto;

import com.erp.platform.modules.supplier.entity.SupplierContract.ContractStatus;
import com.erp.platform.modules.supplier.entity.SupplierContract.ContractType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SupplierContractDto {

    private UUID id;
    private UUID tenantId;
    private String contractNumber;
    private UUID vendorId;
    private String vendorName;
    private ContractType contractType;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalContractValue;
    private String currency;
    private String paymentTerms;
    private String deliveryTerms;
    private String qualityTerms;
    private String penaltyClause;
    private int noticePeriodDays;
    private boolean autoRenew;
    private int renewalTermMonths;
    private String notes;
    private List<ItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal agreedPrice;
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private String unit;
        private int leadTimeDays;
        private String qualitySpec;
    }
}
