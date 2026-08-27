package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriProcessLineEfficiency")
@Table(name = "agri_process_line_efficiency",
        indexes = {@Index(name = "idx_agri_ple_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessLineEfficiency extends TenantEntity {

    private LocalDate recordDate;

    @Column(length = 200)
    private String location;

    /**
     * The godown within that location the line ran in.
     *
     * <p>Efficiency was recorded against a location alone, which is too coarse to act on: two
     * godowns on the same site run different lines at different rates, and averaging them hides
     * whichever one is underperforming.
     */
    @Column(length = 200)
    private String godown;

    @Column(length = 200)
    private String processingLineName;

    @Column(length = 100)
    private String processType;

    @Column(precision = 12, scale = 3)
    private BigDecimal plannedOutputKgs;

    @Column(precision = 12, scale = 3)
    private BigDecimal actualOutputKgs;

    @Column(precision = 6, scale = 2)
    private BigDecimal efficiencyPercent;

    @Column(length = 500)
    private String remarks;
}
