package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * On-hand stock tracked per lot. This is the lot-based stock ledger that the seed industry
 * works with (lot + godown + net). It is written by Opening Stock (and, going forward, lot-wise
 * intake) and read back by the Physical Inventory screen.
 */
@Entity
@Table(name = "stock_lots",
       indexes = {@Index(name = "idx_stocklot_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_stocklot_godown", columnList = "godown_id"),
                  @Index(name = "idx_stocklot_product", columnList = "product_id")})
@Getter
@Setter
public class StockLot extends TenantEntity {

    @Column(name = "lot_no", nullable = false, length = 60)
    private String lotNo;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 250)
    private String productName;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "godown_id")
    private UUID godownId;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(name = "net_id")
    private UUID netId;

    @Column(name = "net_name", length = 200)
    private String netName;

    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "material_group_name", length = 200)
    private String materialGroupName;

    @Column(name = "material_item_name", length = 200)
    private String materialItemName;

    @Column(name = "material_type", length = 150)
    private String materialType;

    @Column(name = "material_state", length = 150)
    private String materialState;

    @Column(name = "no_of_bags")
    private Integer noOfBags;

    // Immutable "received" amount — set once at creation, never reduced on consumption.
    // Used as the fixed Receipt/audit value so Inventory = Receipts - Issues holds.
    @Column(name = "original_no_of_bags")
    private Integer originalNoOfBags;

    @Column(name = "original_quantity", precision = 18, scale = 3)
    private BigDecimal originalQuantity;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(length = 30)
    private String unit;

    /** OPENING | INTAKE — where the lot originated. */
    @Column(length = 20)
    private String source;

    /**
     * What one unit of this lot cost.
     *
     * <p>A lot is the natural place to hold cost: each receipt or production run has its own price,
     * so valuing stock lot by lot gives the same answer as FIFO without replaying every movement.
     * Null where the cost was never captured — valuation then falls back to the weighted average of
     * that product's receipts, and finally to its purchase price.
     */
    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;

    /**
     * When this lot stops being fit to dispatch.
     *
     * <p>Seed has a germination validity, treated chemicals have a shelf life, and packing materials
     * degrade. Without a date on the lot there was nothing to order a pick by and nothing to stop
     * expired stock shipping, so dispatch drew lots in whatever order the database returned them.
     * Null means no expiry applies — such lots are dispatchable, but they sort after dated ones so
     * perishable stock moves first.
     */
    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    /** When the lot was produced or received — the basis for a shelf life where one is configured. */
    @Column(name = "manufacture_date")
    private java.time.LocalDate manufactureDate;

    /** True once the expiry date has passed; kept as a column so expired stock can be filtered out cheaply. */
    @Column(name = "expired")
    private Boolean expired = Boolean.FALSE;
}
