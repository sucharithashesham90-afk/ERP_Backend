package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plant_process_sequences",
       indexes = {@Index(name = "idx_pps_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PlantProcessSequence extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_category_id")
    private PlantCategory plantCategory;

    @Column(nullable = false)
    private int sequenceOrder;

    @Column(nullable = false, length = 150)
    private String processStepName;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String processType; // CLEANING, GRADING, TREATING, PACKAGING, TESTING

    /** Expected output percentage after this step */
    @Column
    private Double expectedOutputPercent;

    @Column(nullable = false)
    private boolean active = true;
}
