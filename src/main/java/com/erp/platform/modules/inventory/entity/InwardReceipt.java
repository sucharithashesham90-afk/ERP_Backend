package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inward_receipts", indexes = {@Index(name = "idx_ir_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class InwardReceipt extends TenantEntity {

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(length = 1000)
    private String notes;

    @Column(length = 20)
    private String status = "DRAFT";

    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(name = "lines_json", columnDefinition = "TEXT")
    private String linesJson;
}
