package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "agri_production_jobs",
        indexes = {@Index(name = "idx_prod_job_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AgriProductionJob extends TenantEntity {

    @Column(name = "job_number", length = 50, nullable = false)
    private String jobNumber;

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "plan_number", length = 50)
    private String planNumber;

    @Column(name = "plan_name", length = 200)
    private String planName;

    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 100)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "production_area_id")
    private UUID productionAreaId;

    @Column(name = "production_area_name", length = 200)
    private String productionAreaName;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    @Column(name = "notes", length = 500)
    private String notes;
}
