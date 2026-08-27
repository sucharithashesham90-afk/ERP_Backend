package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lot_status_records", indexes = {@Index(name = "idx_lot_status_record_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LotStatusRecord extends TenantEntity {

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_name", length = 100)
    private String varietyName;

    @Column(name = "field_producer_name", length = 200)
    private String fieldProducerName;

    @Column(name = "current_status", length = 100)
    private String currentStatus;

    @Column(name = "quality_status", length = 100)
    private String qualityStatus;

    @Column(name = "inventory_kgs", precision = 18, scale = 4)
    private BigDecimal inventoryKgs;

    @Column(name = "processed_kgs", precision = 18, scale = 4)
    private BigDecimal processedKgs;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status_date")
    private LocalDate statusDate;
}
