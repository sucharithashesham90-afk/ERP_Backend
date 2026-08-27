package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriLotProcessingDetail")
@Table(name = "agri_lot_processing_details", indexes = {@Index(name = "idx_lpd_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LotProcessingDetail extends TenantEntity {

    @Column(name = "job_number", length = 50, nullable = false)
    private String jobNumber;

    @Column(name = "processing_date")
    private LocalDate processingDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "input_lot_number", length = 100)
    private String inputLotNumber;

    @Column(name = "output_lot_number", length = 100)
    private String outputLotNumber;

    @Column(name = "input_quantity_kgs", precision = 15, scale = 3)
    private BigDecimal inputQuantityKgs;

    @Column(name = "output_quantity_kgs", precision = 15, scale = 3)
    private BigDecimal outputQuantityKgs;

    @Column(name = "process_type", length = 50)
    private String processType;

    @Column(name = "machine_name", length = 100)
    private String machineName;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "status", length = 20)
    private String status = "PENDING";
}
