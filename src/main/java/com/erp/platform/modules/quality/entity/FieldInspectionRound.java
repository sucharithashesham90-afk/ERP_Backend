package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "field_inspection_rounds",
       indexes = {
           @Index(name = "idx_finspround_tenant",     columnList = "tenant_id"),
           @Index(name = "idx_finspround_inspection", columnList = "inspection_id")
       })
@Getter
@Setter
public class FieldInspectionRound extends TenantEntity {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private FieldInspection inspection;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "round_label", length = 80)
    private String roundLabel;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    /** GOOD | FAIR | POOR */
    @Column(name = "lot_condition", length = 30)
    private String lotCondition;

    @Column(name = "isolation_zone", precision = 10, scale = 2)
    private BigDecimal isolationZone;

    @Column(name = "rejected_area", precision = 14, scale = 4)
    private BigDecimal rejectedArea;

    @Column(name = "recommend_rejection")
    private boolean recommendRejection = false;

    @Column(name = "rejected_reasons", length = 1000)
    private String rejectedReasons;

    @Column(name = "sow_date_1")
    private LocalDate sowDate1;

    @Column(name = "sow_date_2")
    private LocalDate sowDate2;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "field_area_acres", precision = 12, scale = 4)
    private BigDecimal fieldAreaAcres;

    @Column(name = "crop_stage", length = 50)
    private String cropStage;

    @Column(name = "off_type_plant_count")
    private Integer offTypePlantCount;

    @Column(name = "contaminant_plant_count")
    private Integer contaminantPlantCount;

    @Column(name = "self_pollination_count")
    private Integer selfPollinationCount;

    @Column(name = "yield_estimated", precision = 14, scale = 4)
    private BigDecimal yieldEstimated;

    @Column(name = "yield_per_acre", precision = 14, scale = 4)
    private BigDecimal yieldPerAcre;

    @Column(name = "yield_1", precision = 14, scale = 4)
    private BigDecimal yield1;

    @Column(name = "yield_2", precision = 14, scale = 4)
    private BigDecimal yield2;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "inspected_by", length = 200)
    private String inspectedBy;

    // ── 1st Inspection specific ───────────────────────────────────────────────
    @Column(name = "plot_location", length = 200)
    private String plotLocation;

    @Column(name = "off_type_percent", precision = 8, scale = 4)
    private BigDecimal offTypePercent;

    @Column(name = "isolation_distance", precision = 10, scale = 2)
    private BigDecimal isolationDistance;

    @Column(name = "crop_condition", length = 100)
    private String cropCondition;

    @Column(name = "male_sow_date_1")
    private LocalDate maleSowDate1;

    @Column(name = "male_sow_date_2")
    private LocalDate maleSowDate2;

    @Column(name = "male_sow_date_3")
    private LocalDate maleSowDate3;

    @Column(name = "female_sow_date_1")
    private LocalDate femaleSowDate1;

    @Column(name = "female_sow_date_2")
    private LocalDate femaleSowDate2;

    @Column(name = "female_sow_date_3")
    private LocalDate femaleSowDate3;

    @Column(name = "male_area_allotted", precision = 12, scale = 4)
    private BigDecimal maleAreaAllotted;

    @Column(name = "female_area_allotted", precision = 12, scale = 4)
    private BigDecimal femaleAreaAllotted;

    @Column(name = "male_actual_area_sown", precision = 12, scale = 4)
    private BigDecimal maleActualAreaSown;

    @Column(name = "female_actual_area_sown", precision = 12, scale = 4)
    private BigDecimal femaleActualAreaSown;

    @Column(name = "male_lot_number", length = 100)
    private String maleLotNumber;

    @Column(name = "female_lot_number", length = 100)
    private String femaleLotNumber;

    @Column(name = "male_plant_distance", precision = 10, scale = 2)
    private BigDecimal malePlantDistance;

    @Column(name = "female_plant_distance", precision = 10, scale = 2)
    private BigDecimal femalePlantDistance;

    @Column(name = "male_row_distance", precision = 10, scale = 2)
    private BigDecimal maleRowDistance;

    @Column(name = "female_row_distance", precision = 10, scale = 2)
    private BigDecimal femaleRowDistance;

    @Column(name = "male_total_population")
    private Integer maleTotalPopulation;

    @Column(name = "female_total_population")
    private Integer femaleTotalPopulation;

    // ── 2nd / 3rd / 4th Inspection shared ────────────────────────────────────
    @Column(name = "date_of_crossing")
    private LocalDate dateOfCrossing;

    @Column(name = "selfed_bolls_percent", precision = 8, scale = 4)
    private BigDecimal selfedBollsPercent;

    @Column(name = "off_type_removed_count")
    private Integer offTypeRemovedCount;

    /** PROPER | IMPROPER */
    @Column(name = "emasculation", length = 20)
    private String emasculation;

    /** PROPER | IMPROPER */
    @Column(name = "hybridisation", length = 20)
    private String hybridisation;

    /** YES | NO */
    @Column(name = "meets_field_standards", length = 10)
    private String meetsFieldStandards;

    @Column(name = "meets_field_standards_comment", length = 500)
    private String meetsFieldStandardsComment;

    // ── 3rd Inspection specific ───────────────────────────────────────────────
    @Column(name = "effective_bolls_per_plant", precision = 10, scale = 2)
    private BigDecimal effectiveBollsPerPlant;

    /** YES | NO */
    @Column(name = "removed_selfed_bolls", length = 10)
    private String removedSelfedBolls;

    @Column(name = "crossed_bolls_per_plant", precision = 10, scale = 2)
    private BigDecimal crossedBollsPerPlant;

    // ── 4th Inspection specific ───────────────────────────────────────────────
    @Column(name = "date_of_crossing_stopped")
    private LocalDate dateOfCrossingStopped;

    @Column(name = "selfed_bolls_per_plant", precision = 10, scale = 2)
    private BigDecimal selfedBollsPerPlant;

    @Column(name = "bolls_per_plant", precision = 10, scale = 2)
    private BigDecimal bollsPerPlant;

    @Column(name = "already_picked_qty", precision = 14, scale = 4)
    private BigDecimal alreadyPickedQty;

    @Column(name = "yield_expected_qty", precision = 14, scale = 4)
    private BigDecimal yieldExpectedQty;

    @Column(name = "yield_expecting_date")
    private LocalDate yieldExpectingDate;

    // ── Where the inspection was actually taken ──────────────────────────────
    //
    // plotLocation is a name somebody typed; these are the coordinates the device recorded while
    // the inspector was standing in the field. Together they let an inspection be checked against
    // the plot it claims to be for — a report filed from the office looks the same as one filed
    // from the crop until you can see where it was captured.

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Metres of uncertainty the device reported. A fix good to 5 m means more than one good to 500. */
    @Column(name = "location_accuracy_m", precision = 8, scale = 2)
    private BigDecimal locationAccuracyM;

    /** When the device took the fix — not when the record reached the server, which for an offline
     *  inspection can be hours or days later. */
    @Column(name = "captured_at")
    private java.time.LocalDateTime capturedAt;

    // ── Hybrid seed production checks ────────────────────────────────────────
    //
    // What a field inspector is actually there to judge. Isolation and off-type counts were already
    // recorded; these are the operations that decide whether a hybrid lot stays certifiable —
    // rouging out the off-types, keeping the male and female rows in ratio, and detasseling the
    // female rows before they shed their own pollen.

    /** Metres to the nearest contaminating crop, against the standard for this class of seed. */
    @Column(name = "isolation_distance_required", precision = 10, scale = 2)
    private BigDecimal isolationDistanceRequired;

    /** Whether the measured distance met the requirement — recorded, not inferred, because the
     *  inspector may accept a shortfall with a barrier crop in between. */
    @Column(name = "isolation_adequate")
    private Boolean isolationAdequate;

    /** Off-types pulled out since the last visit, and what is left standing. */
    @Column(name = "rouging_done")
    private Boolean rougingDone;

    @Column(name = "rouged_plant_count")
    private Integer rougedPlantCount;

    @Column(name = "rouging_remarks", length = 500)
    private String rougingRemarks;

    /** Planting ratio, e.g. 4 female rows to 1 male. Held as the two counts rather than a string
     *  so it can be compared and reported on. */
    @Column(name = "male_rows")
    private Integer maleRows;

    @Column(name = "female_rows")
    private Integer femaleRows;

    /** Detasseling is time-critical: a female plant that sheds pollen self-pollinates and the lot
     *  is no longer a hybrid. Progress is tracked per visit, not just at the end. */
    @Column(name = "detasseling_percent", precision = 5, scale = 2)
    private BigDecimal detasselingPercent;

    @Column(name = "tassels_removed_count")
    private Integer tasselsRemovedCount;

    /** Female plants found already shedding — the number that puts the lot at risk. */
    @Column(name = "shedding_tassel_count")
    private Integer sheddingTasselCount;

    @Column(name = "detasseling_status", length = 40)
    private String detasselingStatus;
}
