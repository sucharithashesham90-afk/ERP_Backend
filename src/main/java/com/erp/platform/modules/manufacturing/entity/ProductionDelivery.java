package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "production_deliveries",
       indexes = {
           @Index(name = "idx_pd_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pd_number", columnList = "tenant_id,delivery_number", unique = true)
       })
@Getter
@Setter
public class ProductionDelivery extends TenantEntity {

    @Column(name = "delivery_number", nullable = false, length = 50)
    private String deliveryNumber;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "destination_name", length = 200)
    private String destinationName;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "job_number", length = 50)
    private String jobNumber; // link to production job

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(precision = 20, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "KG";

    @Column(name = "pack_type", length = 50)
    private String packType;

    @Column(name = "pack_size", precision = 10, scale = 4)
    private BigDecimal packSize;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(length = 20)
    private String status = "DRAFT"; // DRAFT, DISPATCHED, DELIVERED

    @Column(length = 1000)
    private String notes;
}
