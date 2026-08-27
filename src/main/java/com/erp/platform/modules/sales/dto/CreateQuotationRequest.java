package com.erp.platform.modules.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateQuotationRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private LocalDate quotationDate;
    private LocalDate validUntil;
    private String terms;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String notes;

    @Valid
    private List<QuotationItemRequest> items;

    @Data
    public static class QuotationItemRequest {
        @NotNull
        private UUID productId;
        private String productName;
        @NotNull(message = "Crop group is required")
        private UUID cropGroupId;
        private String cropGroupName;
        @NotNull(message = "Crop is required")
        private UUID cropId;
        private String cropName;
        @NotNull(message = "Variety is required")
        private UUID varietyId;
        private String varietyName;
        @NotBlank(message = "Description is required")
        private String description;
        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;
        private String unit;
        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;
        private BigDecimal discountPercent = BigDecimal.ZERO;
        private BigDecimal taxPercent = BigDecimal.ZERO;
    }
}
