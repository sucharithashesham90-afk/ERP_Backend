package com.erp.platform.modules.dispatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "dispatch_items",
        indexes = {
                @Index(name = "idx_dispatch_item_dispatch", columnList = "dispatch_id")
        })
@Getter
@Setter
public class DispatchItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    @JsonIgnore
    private Dispatch dispatch;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    // Primitive, so the column must never hold NULL - Hibernate cannot read a NULL back into an
    // int, and one such row fails the whole page of the dispatch list rather than just itself.
    @Column(name = "package_count", nullable = false, columnDefinition = "integer not null default 1")
    private int packageCount = 1;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @Column(name = "pack_size_kg", precision = 10, scale = 3)
    private BigDecimal packSizeKg = BigDecimal.ZERO;

    @Column(name = "secondary_pack_type", length = 50)
    private String secondaryPackType;

    @Column(name = "secondary_pack_count", nullable = false, columnDefinition = "integer not null default 0")
    private int secondaryPackCount = 0;

    /**
     * Which bag the secondary packing uses, from the Bag Sizes master.
     *
     * <p>The screen offered a free-text box here, so the same bag was written a dozen ways and
     * nothing could be totalled by it. Kept separate from the count above: how many bags and which
     * bag are different facts.
     */
    @Column(name = "secondary_bag_size", length = 60)
    private String secondaryBagSize;

    @Column(name = "material_state", length = 50)
    private String materialState;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "gross_weight", precision = 18, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "net_weight", precision = 18, scale = 3)
    private BigDecimal netWeight;

    @Column(length = 500)
    private String remarks;
}
