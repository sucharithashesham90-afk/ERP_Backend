package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * What kind of material a lot is — seed, consumable, packing material and so on.
 *
 * <p>Four screens (seed conversion, opening inventory, physical inventory and dispatch) have been
 * asking for this list since they were written, against an endpoint that did not exist. They each
 * swallowed the 404, so the dropdown was simply always empty with nothing saying why. Material state
 * had the same problem and turned out to be the seed-state master under another name; material type
 * had no master at all, which is what this is.
 */
@Entity(name = "AgriMaterialType")
@Table(name = "agri_material_types",
       indexes = {@Index(name = "idx_agri_mtype_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class MaterialType extends TenantEntity {

    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active = true;
}
