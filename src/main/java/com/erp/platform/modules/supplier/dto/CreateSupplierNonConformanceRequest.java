package com.erp.platform.modules.supplier.dto;

import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCType;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.Severity;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSupplierNonConformanceRequest {

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
}
