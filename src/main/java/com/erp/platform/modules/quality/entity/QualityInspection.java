package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quality_inspections",
       indexes = {
           @Index(name = "idx_qi_tenant", columnList = "tenant_id"),
           @Index(name = "idx_qi_type", columnList = "tenant_id, inspection_type")
       })
@Getter
@Setter
public class QualityInspection extends TenantEntity {

    @Column(name = "inspection_number", nullable = false, length = 50)
    private String inspectionNumber;

    @Column(name = "inspection_type", length = 30)
    private String inspectionType; // INCOMING, IN_PROCESS, OUTGOING

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "reference_id")
    private UUID referenceId; // GRN id or work order id

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "lot_id")
    private UUID lotId;

    @Column(name = "lot_number", length = 80)
    private String lotNumber;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "inspector_name", length = 150)
    private String inspectorName;

    @Column(length = 15)
    private String result; // PASS, FAIL, CONDITIONAL

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "defects_found")
    private Integer defectsFound;

    @Column(name = "acceptance_criteria", length = 500)
    private String acceptanceCriteria;

    @Column(length = 1000)
    private String remarks;
}
