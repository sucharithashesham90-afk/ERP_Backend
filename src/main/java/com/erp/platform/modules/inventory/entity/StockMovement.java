package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_movements",
       indexes = {@Index(name = "idx_stock_mvt_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class StockMovement extends TenantEntity {

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private MovementType type;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 18, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "balance_before", precision = 18, scale = 4)
    private BigDecimal balanceBefore = BigDecimal.ZERO;

    @Column(name = "balance_after", precision = 18, scale = 4)
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    @Column(name = "movement_date")
    private LocalDate movementDate;

    @Column(length = 1000)
    private String notes;

    /** Production/storage lot this movement belongs to, when traceable to a specific lot. */
    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    /** Business purpose of the movement: INTAKE, PURCHASE_RECEIPT, PROCESSING, DISPATCH, SALES_RETURN, PURCHASE_RETURN, TRANSFER, SCRAP, ADJUSTMENT. */
    @Column(name = "purpose_type", length = 30)
    private String purposeType;

    /** Material processing state at time of movement: RAW, DRIED, GRADED, TREATED, PACKED. */
    @Column(name = "material_state", length = 30)
    private String materialState;

    /** Material quality/condition type at time of movement: GOOD, DAMAGED, EXPIRED, RETURNED, SMALLSEED. */
    @Column(name = "material_type", length = 30)
    private String materialType;

    public enum MovementType {
        RECEIPT, ISSUE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT
    }
}
