package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "process_steps",
       indexes = {
           @Index(name = "idx_ps_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ps_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class ProcessStep extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_type_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OperationType operationType;

    @Column(length = 1000)
    private String description;

    @Column(name = "standard_duration_hours", precision = 8, scale = 2)
    private BigDecimal standardDurationHours = BigDecimal.ZERO;

    @Column(name = "setup_time_hours", precision = 8, scale = 2)
    private BigDecimal setupTimeHours = BigDecimal.ZERO;

    @Column(name = "equipment_required", length = 500)
    private String equipmentRequired;

    @Column(name = "skill_required", length = 500)
    private String skillRequired;

    @Column(name = "quality_check_required", nullable = false)
    private boolean qualityCheckRequired = false;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "output_state", length = 100)
    private String outputState;

    @Column(name = "input_states", columnDefinition = "TEXT")
    private String inputStates;

    @Column(name = "has_process_loss", columnDefinition = "boolean not null default false")
    private boolean hasProcessLoss = false;

    @Column(name = "has_waste", columnDefinition = "boolean not null default false")
    private boolean hasWaste = false;

    @Column(name = "has_byproduct", columnDefinition = "boolean not null default false")
    private boolean hasByproduct = false;

    @Column(name = "allow_multiple_inputs", columnDefinition = "boolean not null default false")
    private boolean allowMultipleInputs = false;

    /**
     * Crops this step applies to, held as a comma-separated list of crop ids.
     *
     * <p>The screen has always sent a {@code crops} array and read one back, but there was no
     * column behind it, so Jackson dropped the field on the way in and the selection was gone by
     * the time the row was saved. Stored flat rather than as a join table because it is a filter on
     * a config row, not a relationship anything navigates.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "crops", columnDefinition = "TEXT")
    private String cropsCsv;

    /** The {@code crops} array the screen sends and expects back. */
    @jakarta.persistence.Transient
    public java.util.List<String> getCrops() {
        if (cropsCsv == null || cropsCsv.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(cropsCsv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public void setCrops(java.util.List<String> crops) {
        this.cropsCsv = (crops == null || crops.isEmpty())
                ? null
                : crops.stream().filter(java.util.Objects::nonNull)
                        .map(String::trim).filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.joining(","));
    }
}
