package com.erp.platform.modules.supplier.dto;

import com.erp.platform.modules.supplier.entity.SupplierContract.ContractStatus;
import com.erp.platform.modules.supplier.entity.SupplierContract.ContractType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSupplierContractRequest {

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
    private Integer noticePeriodDays;
    private Boolean autoRenew;
    private Integer renewalTermMonths;
    private String notes;
    private List<ContractItemRequest> items;

    @Data
    public static class ContractItemRequest {
        private UUID productId;
        private String productName;
        private BigDecimal agreedPrice;
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private String unit;
        private Integer leadTimeDays;
        private String qualitySpec;
    }
}
