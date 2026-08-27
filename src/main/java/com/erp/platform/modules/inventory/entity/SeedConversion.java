package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Seed conversion (reclassify/convert lot stock). Backs /api/v1/inventory/seed-conversions. */
@Entity
@Table(name = "seed_conversions",
       indexes = {@Index(name = "idx_seedconv_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SeedConversion extends TenantEntity {

    @Column(name = "crop_group_id")
    private UUID cropGroupId;
    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;
    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;
    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "material_type_id")
    private UUID materialTypeId;
    @Column(name = "material_state_id")
    private UUID materialStateId;

    @Column(length = 150)
    private String location;
    @Column(name = "godown_id")
    private UUID godownId;

    @Column(name = "conversion_date")
    private LocalDate conversionDate;

    @Column(name = "from_lot_no", length = 60)
    private String fromLotNo;
    @Column(name = "from_no_of_bags", length = 30)
    private String fromNoOfBags;
    @Column(name = "from_quantity", length = 30)
    private String fromQuantity;
    @Column(name = "from_uom_id")
    private UUID fromUomId;

    @Column(name = "to_no_of_bags", length = 30)
    private String toNoOfBags;
    @Column(name = "to_quantity", length = 30)
    private String toQuantity;
    @Column(name = "to_uom_id")
    private UUID toUomId;

    @Column(length = 1000)
    private String notes;

    /**
     * How this record came about: {@code MANUAL} for one entered on the Seed Conversion screen,
     * {@code PROCESS_JOB} for one written automatically when a job moved a lot between states.
     *
     * <p>Both are real conversions and both are worth keeping — a graded lot that changed state
     * without a trace would be worse. But they answer different questions, and the screen where
     * someone records a conversion is not the place to read back every stage transition the plant
     * made this week. Telling them apart by the wording of a note was never going to hold.
     *
     * <p>Null means manual, so every row written before this existed still appears where it did.
     */
    @Column(name = "source", length = 20)
    private String source;

    /** The job that produced this record, when it was not entered by hand. */
    @Column(name = "source_job_id")
    private UUID sourceJobId;

    @Column(name = "source_reference", length = 60)
    private String sourceReference;
}
