package com.erp.platform.modules.purchase.dto;

import com.erp.platform.modules.purchase.entity.PurchaseRequisition.Priority;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition.ReqStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseRequisitionDto {

    private UUID id;
    private UUID tenantId;
    private String requisitionNumber;
    private String requestedBy;
    private UUID departmentId;
    private String departmentName;
    private LocalDate requiredByDate;
    private ReqStatus status;
    private Priority priority;
    private BigDecimal totalEstimatedValue;
    private String approvedBy;
    private LocalDate approvalDate;
    private String rejectionReason;
    private String notes;
    private LocalDateTime createdAt;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal estimatedUnitPrice;
        private BigDecimal estimatedTotalPrice;
        private UUID preferredVendorId;
        private String preferredVendorName;
        private String specifications;
        private String notes;
    }
}
