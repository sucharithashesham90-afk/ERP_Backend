package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriMaterialStateLog")
@Table(name = "agri_material_state_logs", indexes = {
    @Index(name = "idx_material_state_log_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class MaterialStateLog extends TenantEntity {

    @Column(name = "log_number", length = 50, nullable = false)
    private String logNumber;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "from_state", length = 50)
    private String fromState;

    @Column(name = "to_state", length = 50)
    private String toState;

    @Column(name = "process_type", length = 100)
    private String processType;

    @Column(name = "quantity_kgs", precision = 15, scale = 3)
    private BigDecimal quantityKgs;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "operator_name", length = 200)
    private String operatorName;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
