package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products",
       indexes = {
           @Index(name = "idx_product_tenant", columnList = "tenant_id"),
           @Index(name = "idx_product_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class Product extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 50)
    private String sku;

    @Column(length = 50)
    private String barcode;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(name = "material_group_id")
    private UUID materialGroupId;

    @Column(name = "material_group_name", length = 150)
    private String materialGroupName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "product_type", length = 30)
    @Enumerated(EnumType.STRING)
    private ProductType productType = ProductType.GOODS;

    @Column(length = 20)
    private String unit = "PCS";

    @Column(name = "purchase_price", precision = 18, scale = 4)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "sale_price", precision = 18, scale = 4)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4)
    private BigDecimal mrp = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_rate_id")
    private Tax taxRate;

    @Column(name = "reorder_level", precision = 18, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "current_stock", precision = 18, scale = 4)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "track_inventory")
    private boolean trackInventory = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String notes;

    public enum ProductType {
        GOODS, SERVICE, RAW_MATERIAL, CONSUMABLE, ASSET
    }
}
