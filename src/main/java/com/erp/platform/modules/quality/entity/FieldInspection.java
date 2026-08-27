package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "field_inspections",
       indexes = {
           @Index(name = "idx_finsp_tenant", columnList = "tenant_id"),
           @Index(name = "idx_finsp_lot",    columnList = "tenant_id, lot_reference")
       })
@Getter
@Setter
public class FieldInspection extends TenantEntity {

    @Column(name = "lot_reference", length = 100)
    private String lotReference;

    @Column(name = "production_job_id")
    private UUID productionJobId;

    @Column(name = "job_reference", length = 100)
    private String jobReference;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Column(name = "location_name", length = 200)
    private String locationName;

    @Column(name = "contract_reference", length = 100)
    private String contractReference;

    @Column(name = "inspector_name", length = 200)
    private String inspectorName;

    @Column(name = "organizer_name", length = 200)
    private String organizerName;

    /** ACTIVE | COMPLETED | REJECTED */
    @Column(length = 30)
    private String status = "ACTIVE";

    @Column(length = 500)
    private String notes;

    @JsonManagedReference
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("roundNumber ASC")
    private List<FieldInspectionRound> rounds = new ArrayList<>();
}
