package com.erp.platform.modules.supplier.dto;

import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCStatus;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCType;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.Severity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SupplierNonConformanceDto {

    private UUID id;
    private UUID tenantId;
    private String ncNumber;
    private UUID vendorId;
    private String vendorName;
    private UUID purchaseOrderId;
    private UUID goodsReceiptId;
    private LocalDate issueDate;
    private NCType issueType;
    private Severity severity;
    private String description;
    private String rootCause;
    private String correctiveAction;
    private String vendorResponse;
    private LocalDate closureDate;
    private NCStatus status;
    private LocalDateTime createdAt;
}
