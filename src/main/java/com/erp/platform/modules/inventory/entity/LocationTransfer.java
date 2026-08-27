package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "location_transfers",
       indexes = {@Index(name = "idx_loc_transfer_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_loc_transfer_warehouse", columnList = "tenant_id, warehouse_id"),
                  @Index(name = "idx_loc_transfer_number", columnList = "tenant_id, transfer_number")})
@Getter
@Setter
public class LocationTransfer extends TenantEntity {

    @Column(name = "transfer_number", nullable = false, length = 30)
    private String transferNumber;

    @Column(name = "from_location_id", nullable = false)
    private UUID fromLocationId;

    @Column(name = "from_location_code", length = 50)
    private String fromLocationCode;

    @Column(name = "to_location_id", nullable = false)
    private UUID toLocationId;

    @Column(name = "to_location_code", length = 50)
    private String toLocationCode;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 300)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LTStatus status = LTStatus.PENDING;

    @Column(name = "performed_by", length = 150)
    private String performedBy;

    public enum LTStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
