package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriWeighbridgeRecord")
@Table(name = "agri_weighbridge_records", indexes = {
    @Index(name = "idx_weighbridge_record_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class WeighbridgeRecord extends TenantEntity {

    @Column(name = "slip_number", length = 50, nullable = false)
    private String slipNumber;

    @Column(name = "weigh_date")
    private LocalDate weighDate;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "gross_weight", precision = 10, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "tare_weight", precision = 10, scale = 3)
    private BigDecimal tareWeight;

    @Column(name = "net_weight", precision = 10, scale = 3)
    private BigDecimal netWeight;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "linked_intake_slip", length = 100)
    private String linkedIntakeSlip;

    @Column(name = "driver_name", length = 200)
    private String driverName;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
