package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "delivery_notes",
       indexes = {@Index(name = "idx_dn_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DeliveryNote extends TenantEntity {

    @Column(name = "delivery_number", nullable = false, length = 50)
    private String deliveryNumber;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "carrier_name", length = 150)
    private String carrierName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(length = 20)
    private String status = "DRAFT";

    @Column(length = 1000)
    private String notes;
}
