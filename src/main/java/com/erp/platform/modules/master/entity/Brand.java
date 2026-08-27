package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "brands",
       indexes = {
           @Index(name = "idx_brand_tenant", columnList = "tenant_id"),
           @Index(name = "idx_brand_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class Brand extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    // ── Crop hierarchy: a brand belongs to exactly one crop variety (1:1). ──
    @Column(name = "crop_group_id", length = 64)
    private String cropGroupId;
    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;
    @Column(name = "crop_id", length = 64)
    private String cropId;
    @Column(name = "crop_name", length = 200)
    private String cropName;
    @Column(name = "variety_id", length = 64)
    private String varietyId;
    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(length = 1000)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 100)
    private String country;

    /** ALL = all sales areas; SELECTED = only the areas listed in salesAreas. */
    @Column(name = "sales_scope", length = 20)
    private String salesScope = "ALL";

    /** CSV of selected sales-area names (when salesScope = SELECTED). */
    @Column(name = "sales_areas", length = 1000)
    private String salesAreas;

    // Boolean (not primitive) so pre-existing rows whose column is NULL read without a
    // PropertyAccessException. New rows default to false via the service.
    @Column(name = "use_sticker", columnDefinition = "boolean default false")
    private Boolean useSticker = false;

    /** Packing material used as the sticker (when useSticker = true). */
    @Column(name = "sticker_material", length = 200)
    private String stickerMaterial;

    /** Uploaded product image as a base64 data URL. */
    @Column(name = "image_data", columnDefinition = "text")
    private String imageData;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
