package com.erp.platform.modules.supplier.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "supplier_nonconformances",
        indexes = {
                @Index(name = "idx_nc_tenant", columnList = "tenant_id"),
                @Index(name = "idx_nc_vendor", columnList = "tenant_id, vendor_id"),
                @Index(name = "idx_nc_status", columnList = "tenant_id, status")
        })
@Getter
@Setter
public class SupplierNonConformance extends TenantEntity {

    @Column(name = "nc_number", nullable = false, length = 50)
    private String ncNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "goods_receipt_id")
    private UUID goodsReceiptId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 20)
    private NCType issueType;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Severity severity;

    @Column(length = 1000)
    private String description;

    @Column(name = "root_cause", length = 1000)
    private String rootCause;

    @Column(name = "corrective_action", length = 1000)
    private String correctiveAction;

    @Column(name = "vendor_response", length = 1000)
    private String vendorResponse;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NCStatus status = NCStatus.OPEN;

    public enum NCType {
        QUALITY, DELIVERY, DOCUMENTATION, PRICING, PACKING
    }

    public enum Severity {
        MINOR, MAJOR, CRITICAL
    }

    public enum NCStatus {
        OPEN, VENDOR_RESPONDED, CLOSED, ESCALATED
    }
}
