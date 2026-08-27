package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_bom_lines",
       indexes = {
           @Index(name = "idx_prodbom_tenant",  columnList = "tenant_id"),
           @Index(name = "idx_prodbom_product", columnList = "tenant_id, product_id"),
           @Index(name = "idx_prodbom_brand",   columnList = "tenant_id, brand_id")
       })
@Getter
@Setter
public class ProductBomLine extends TenantEntity {

    /** Parent key. Kept non-null (mirrors brand_id for brand-scoped BOMs) for legacy queries + the NOT NULL constraint. */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_code", length = 50)
    private String productCode;

    /** A BOM is defined against a Brand. */
    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "material_group_id")
    private UUID materialGroupId;

    @Column(name = "material_group_name", length = 200)
    private String materialGroupName;

    @Column(name = "material_item_id")
    private UUID materialItemId;

    @Column(name = "material_item_name", length = 200)
    private String materialItemName;

    /** PACKING | TREATMENT | ACCESSORY | OTHER */
    @Column(name = "bom_type", length = 50)
    private String bomType = "PACKING";

    @Column(name = "qty_per_pack", precision = 18, scale = 4)
    private BigDecimal qtyPerPack = BigDecimal.ONE;

    @Column(length = 30)
    private String unit;

    @Column(precision = 18, scale = 4)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @Column(name = "sort_order")
    private int sortOrder = 0;
}
