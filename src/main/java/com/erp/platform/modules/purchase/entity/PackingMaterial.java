package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Packing material master (Purchase Configuration → Generic Packing Materials
 * and Product Specific Packing Materials). The two documented sub-tabs share
 * this entity: a row with a null productId is a generic packing material, a row
 * with a productId is a product-specific one.
 */
@Entity
@Table(name = "packing_materials",
       indexes = {@Index(name = "idx_packmat_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackingMaterial extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "packing_type_id")
    private UUID packingTypeId;

    @Column(name = "packing_type_name", length = 200)
    private String packingTypeName;

    @Column(name = "technical_specification", length = 2000)
    private String technicalSpecification;

    /** Approximate unit weight in grams (free text to allow ranges). */
    @Column(name = "approx_unit_weight_gms", length = 50)
    private String approxUnitWeightGms;

    @Column(name = "approx_unit_cost", length = 50)
    private String approxUnitCost;

    @Column(name = "packets_per_kg", length = 50)
    private String packetsPerKg;

    /** STORAGE or PRODUCT_PACKING (the two radio buttons). */
    @Column(name = "usage_type", length = 30)
    private String usageType;

    /** Second "Type" drop-down (packing type). */
    @Column(name = "type_id")
    private UUID typeId;

    @Column(name = "type_name", length = 200)
    private String typeName;

    /** Packing size (drop-down from bag size). */
    @Column(name = "packing_size_id")
    private UUID packingSizeId;

    @Column(name = "packing_size_name", length = 200)
    private String packingSizeName;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    /** JSON array of pack capacities: [{"primaryPack":"...","packCapacity":"..."}]. */
    @Column(name = "pack_capacities_json", length = 4000)
    private String packCapacitiesJson;

    /** Non-null => product specific packing material. */
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(nullable = false)
    private boolean active = true;
}
