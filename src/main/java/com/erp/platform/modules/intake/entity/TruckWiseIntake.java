package com.erp.platform.modules.intake.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriTruckWiseIntake")
@Table(name = "agri_truck_wise_intake",
        indexes = {@Index(name = "idx_truck_intake_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class TruckWiseIntake extends TenantEntity {

    @Column(length = 100)
    private String intakeSlipNumber;

    private LocalDate intakeDate;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String deliveryType;

    @Column(length = 50)
    private String vehicleNumber;

    @Column(length = 100)
    private String inwardGatePassNumber;

    @Column(length = 100)
    private String lrNumber;

    @Column(length = 200)
    private String transport;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalFreight;

    @Column(precision = 12, scale = 3)
    private BigDecimal weighBridgeQty;

    @Column(length = 100)
    private String stnNumber;

    private LocalDate stnDate;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    /** Line items (crop/variety/bags per line) persisted as JSON. */
    @Column(name = "lines_json", columnDefinition = "text")
    private String linesJson;
}
