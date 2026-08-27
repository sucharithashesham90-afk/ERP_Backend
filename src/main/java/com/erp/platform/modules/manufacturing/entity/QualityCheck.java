package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quality_checks",
       indexes = {@Index(name = "idx_qc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class QualityCheck extends TenantEntity {

    @Column(name = "check_number", nullable = false, length = 50)
    private String checkNumber;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "check_type", length = 20)
    private String checkType; // INCOMING, IN_PROCESS, FINAL, OUTGOING

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "checked_by", length = 100)
    private String checkedBy;

    @Column(length = 10)
    private String result; // PASS, FAIL, CONDITIONAL

    @Column(length = 1000)
    private String parameters;

    @Column(name = "defects_found", length = 1000)
    private String defectsFound;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "corrective_action", length = 1000)
    private String correctiveAction;
}
