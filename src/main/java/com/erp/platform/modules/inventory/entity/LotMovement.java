package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lot_movements",
       indexes = {
           @Index(name = "idx_lot_mvt_tenant", columnList = "tenant_id"),
           @Index(name = "idx_lot_mvt_lot_number", columnList = "tenant_id, lot_number"),
           @Index(name = "idx_lot_mvt_product", columnList = "tenant_id, product_id")
       })
@Getter
@Setter
public class LotMovement extends TenantEntity {

    @Column(name = "lot_number", nullable = false, length = 100)
    private String lotNumber;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "movement_type", length = 20)
    @Enumerated(EnumType.STRING)
    private LotMovType movementType;

    @Column(name = "movement_date")
    private LocalDate movementDate;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "balance_before", precision = 18, scale = 4)
    private BigDecimal balanceBefore = BigDecimal.ZERO;

    @Column(name = "balance_after", precision = 18, scale = 4)
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 1000)
    private String notes;

    public enum LotMovType {
        RECEIPT, ISSUE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT, QUARANTINE, RELEASE
    }
}
