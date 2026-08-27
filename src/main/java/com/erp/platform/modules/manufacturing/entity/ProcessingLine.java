package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "processing_lines",
       indexes = {
           @Index(name = "idx_procline_tenant", columnList = "tenant_id"),
           @Index(name = "idx_procline_code",   columnList = "tenant_id, code", unique = true)
       })
@Getter
@Setter
public class ProcessingLine extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "line_type", length = 50)
    private String lineType; // PACKING, GRADING, PROCESSING, MIXING, TREATMENT, INSPECTION

    // The ids behind the two dropdowns. Only the names were stored, so a saved line came back with
    // no id for its plant or godown and both Autocompletes reopened empty - the values were on the
    // row all along, just not in a form the form could match.
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 150)
    private String warehouseName;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "location_name", length = 150)
    private String locationName;

    // Named "consumption" and "usageHoursPerDay" in the column, but the screen has always sent
    // powerConsumptionPerHr and maxUsageHoursPerDay. Jackson had nothing to bind them to, so both
    // silently vanished on save. Aliased rather than renamed so existing rows keep their data.
    @com.fasterxml.jackson.annotation.JsonProperty("powerConsumptionPerHr")
    @Column(name = "consumption", length = 200)
    private String consumption; // e.g. "15 kW/hr"

    @com.fasterxml.jackson.annotation.JsonProperty("maxUsageHoursPerDay")
    @Column(name = "usage_hours_per_day", precision = 5, scale = 2)
    private BigDecimal usageHoursPerDay = BigDecimal.ZERO;

    @Column(name = "capacity", precision = 18, scale = 4)
    private BigDecimal capacity = BigDecimal.ZERO;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit = "KG";

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "process_step_id")
    private UUID processStepId;

    @Column(name = "process_step_name", length = 200)
    private String processStepName;

    @Column(name = "ownership", length = 10)
    private String ownership = "OWN";
}
