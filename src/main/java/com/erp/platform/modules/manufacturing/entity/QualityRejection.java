package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quality_rejections",
       indexes = {
           @Index(name = "idx_qrej_tenant", columnList = "tenant_id"),
           @Index(name = "idx_qrej_status", columnList = "tenant_id, status"),
           @Index(name = "idx_qrej_product", columnList = "tenant_id, product_id")
       })
@Getter
@Setter
public class QualityRejection extends TenantEntity {

    @Column(name = "rejection_number", nullable = false, length = 50)
    private String rejectionNumber;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "rejection_date", nullable = false)
    private LocalDate rejectionDate;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit = "PCS";

    @Column(name = "rejection_reason", nullable = false, length = 1000)
    private String rejectionReason;

    @Column(name = "failed_parameters", columnDefinition = "TEXT")
    private String failedParameters;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Disposition disposition = Disposition.PENDING;

    @Column(name = "disposition_date")
    private LocalDate dispositionDate;

    @Column(name = "disposition_notes", length = 1000)
    private String dispositionNotes;

    @Column(name = "corrective_action", length = 2000)
    private String correctiveAction;

    @Column(name = "preventive_action", length = 2000)
    private String preventiveAction;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private RejectionStatus status = RejectionStatus.OPEN;

    @Column(name = "inspection_ref")
    private UUID inspectionRef;

    public enum Disposition {
        REWORK, SCRAP, RETURN_TO_SUPPLIER, QUARANTINE, PENDING
    }

    public enum RejectionStatus {
        OPEN, IN_REVIEW, RESOLVED, CLOSED
    }
}
