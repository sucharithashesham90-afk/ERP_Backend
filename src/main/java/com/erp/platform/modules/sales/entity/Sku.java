package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SKU (Product Configuration → SKUs): the packing details of a product/brand. Carries pricing and a
 * list of packing materials to use (stored as JSON).
 */
@Entity
@Table(name = "product_skus",
       indexes = {@Index(name = "idx_sku_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Sku extends TenantEntity {

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "packing_material", length = 200)
    private String packingMaterial;

    @Column(nullable = false, length = 200)
    private String name;

    // Crop / variety the SKU is packed for. Lets packing output be narrowed to the SKUs
    // that belong to the crop and variety being processed instead of listing everything.
    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "pack_info", length = 200)
    private String packInfo;

    @Column(name = "actual_pack_qty_kgs", precision = 15, scale = 3)
    private BigDecimal actualPackQtyKgs;

    @Column(name = "pack_mrp", precision = 18, scale = 2)
    private BigDecimal packMrp;

    @Column(length = 1000)
    private String description;

    @Column(name = "product_code", length = 60)
    private String productCode;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "use_sticker", nullable = false)
    private boolean useSticker = false;

    @Column(name = "sticker_material", length = 200)
    private String stickerMaterial;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Packing materials to use — list of {type, packingType, packingSize, name, imageUrl} as JSON. */
    @Column(name = "packing_materials_json", columnDefinition = "text")
    private String packingMaterialsJson;
}
