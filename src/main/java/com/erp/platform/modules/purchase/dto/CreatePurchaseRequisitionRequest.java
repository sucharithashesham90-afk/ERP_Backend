package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.PurchaseRequisition.Priority;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePurchaseRequisitionRequest {

    private String requestedBy;
    private UUID departmentId;
    private String departmentName;
    private LocalDate requiredByDate;
    private Priority priority;
    private String notes;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal estimatedUnitPrice;
        private UUID preferredVendorId;
        private String preferredVendorName;
        private String specifications;
        private String notes;
    }
}
