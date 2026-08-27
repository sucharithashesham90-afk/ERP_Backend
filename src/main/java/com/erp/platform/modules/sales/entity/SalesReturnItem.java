package com.erp.platform.modules.sales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_return_items",
       indexes = {
           @Index(name = "idx_sri_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sri_return", columnList = "return_id")
       })
@Getter
@Setter
public class SalesReturnItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    @JsonIgnore
    private SalesReturn salesReturn;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    // Crop / variety / packaging detail, so a return can be pulled into Sales Return
    // Intake with its material details intact.
    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "bag_size_id")
    private UUID bagSizeId;

    @Column(name = "bag_size_name", length = 100)
    private String bagSizeName;

    @Column(name = "bag_type_id")
    private UUID bagTypeId;

    @Column(name = "bag_type_name", length = 100)
    private String bagTypeName;

    @Column(name = "lot_number", length = 60)
    private String lotNumber;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(length = 500)
    private String remarks;
}
