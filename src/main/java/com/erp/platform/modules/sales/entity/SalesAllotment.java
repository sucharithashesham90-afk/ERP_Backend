package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sales_allotments",
       indexes = {
           @Index(name = "idx_sallot_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sallot_product", columnList = "tenant_id, product_id"),
           @Index(name = "idx_sallot_customer", columnList = "tenant_id, customer_id")
       })
@Getter
@Setter
public class SalesAllotment extends TenantEntity {

    public enum AllotmentStatus { PENDING, ACTIVE, RELEASED, CANCELLED }

    @Column(name = "allotment_number", nullable = false, length = 50)
    private String allotmentNumber;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "season_id")
    private UUID seasonId;

    @Column(name = "season_name", length = 100)
    private String seasonName;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "allotted_qty", precision = 18, scale = 4)
    private BigDecimal allottedQty = BigDecimal.ZERO;

    @Column(name = "dispatched_qty", precision = 18, scale = 4)
    private BigDecimal dispatchedQty = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "PCS";

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AllotmentStatus status = AllotmentStatus.PENDING;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(length = 1000)
    private String notes;
}
