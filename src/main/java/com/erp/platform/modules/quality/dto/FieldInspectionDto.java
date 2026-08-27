package com.erp.platform.modules.quality.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FieldInspectionDto {
    private UUID id;
    private String lotReference;
    private UUID productionJobId;
    private String jobReference;
    private String itemName;
    private String itemCode;
    private String locationName;
    private String contractReference;
    private String inspectorName;
    private String organizerName;
    private String status;
    private String notes;
    private List<RoundDto> rounds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class RoundDto {
        private UUID id;
        private int roundNumber;
        private String roundLabel;
        private LocalDate inspectionDate;
        private String lotCondition;
        private String cropStage;
        private BigDecimal fieldAreaAcres;
        private BigDecimal isolationZone;
        private BigDecimal rejectedArea;
        private Integer offTypePlantCount;
        private Integer contaminantPlantCount;
        private Integer selfPollinationCount;
        private boolean recommendRejection;
        private String rejectedReasons;
        private LocalDate sowDate1;
        private LocalDate sowDate2;
        private LocalDate harvestDate;
        private BigDecimal yieldEstimated;
        private BigDecimal yieldPerAcre;
        private BigDecimal yield1;
        private BigDecimal yield2;
        private String remarks;
        private String inspectedBy;

        // ── 1st Inspection specific ───────────────────────────────────────────
        private String plotLocation;
        private BigDecimal offTypePercent;
        private BigDecimal isolationDistance;
        private String cropCondition;
        private LocalDate maleSowDate1;
        private LocalDate maleSowDate2;
        private LocalDate maleSowDate3;
        private LocalDate femaleSowDate1;
        private LocalDate femaleSowDate2;
        private LocalDate femaleSowDate3;
        private BigDecimal maleAreaAllotted;
        private BigDecimal femaleAreaAllotted;
        private BigDecimal maleActualAreaSown;
        private BigDecimal femaleActualAreaSown;
        private String maleLotNumber;
        private String femaleLotNumber;
        private BigDecimal malePlantDistance;
        private BigDecimal femalePlantDistance;
        private BigDecimal maleRowDistance;
        private BigDecimal femaleRowDistance;
        private Integer maleTotalPopulation;
        private Integer femaleTotalPopulation;

        // ── 2nd / 3rd / 4th Inspection shared ────────────────────────────────
        private LocalDate dateOfCrossing;
        private BigDecimal selfedBollsPercent;
        private Integer offTypeRemovedCount;
        private String emasculation;
        private String hybridisation;
        private String meetsFieldStandards;
        private String meetsFieldStandardsComment;

        // ── 3rd Inspection specific ───────────────────────────────────────────
        private BigDecimal effectiveBollsPerPlant;
        private String removedSelfedBolls;
        private BigDecimal crossedBollsPerPlant;

        // ── 4th Inspection specific ───────────────────────────────────────────
        private LocalDate dateOfCrossingStopped;
        private BigDecimal selfedBollsPerPlant;
        private BigDecimal bollsPerPlant;
        private BigDecimal alreadyPickedQty;
        private BigDecimal yieldExpectedQty;
        private LocalDate yieldExpectingDate;

        /** Where the device recorded the inspection, and how good the fix was. */
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal locationAccuracyM;
        private java.time.LocalDateTime capturedAt;

        /** Hybrid seed production checks — isolation, rouging, row ratio, detasseling. */
        private BigDecimal isolationDistanceRequired;
        private Boolean isolationAdequate;
        private Boolean rougingDone;
        private Integer rougedPlantCount;
        private String rougingRemarks;
        private Integer maleRows;
        private Integer femaleRows;
        private BigDecimal detasselingPercent;
        private Integer tasselsRemovedCount;
        private Integer sheddingTasselCount;
        private String detasselingStatus;
    }
}
