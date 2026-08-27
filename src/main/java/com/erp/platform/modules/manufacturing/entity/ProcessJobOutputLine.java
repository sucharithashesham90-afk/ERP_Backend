package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "process_job_output_lines",
       indexes = {
           @Index(name = "idx_pjol_job", columnList = "job_id"),
           @Index(name = "idx_pjol_tenant", columnList = "tenant_id")
       })
@Getter
@Setter
public class ProcessJobOutputLine extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private ProductionJob job;

    @Column(name = "output_type", length = 20)
    private String outputType; // PACKING or OTHER

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "output_lot_number", length = 100)
    private String outputLotNumber;

    @Column(name = "number_of_units")
    private Integer numberOfUnits; // packets for PACKING, bags for OTHER

    @Column(name = "mrp_value", precision = 18, scale = 2)
    private BigDecimal mrpValue;

    @Column(name = "pack_size", precision = 10, scale = 4)
    private BigDecimal packSize;

    /**
     * The unit {@link #packSize} is expressed in — "KG", "G", "QUINTAL".
     *
     * <p>Pack size used to be a bare number that everything downstream assumed was kilograms. A
     * 500&nbsp;gram pack was stored as 500 and multiplied out as 500&nbsp;kg, so ten packets of
     * half a kilo reported five thousand kilograms of output against five kilograms of input.
     * Carrying the unit with the number is the only way that arithmetic can be trusted.
     *
     * <p>Null means kilograms, so every line recorded before this existed still reads correctly.
     */
    @Column(name = "pack_size_uom", length = 20)
    private String packSizeUom;

    @Column(name = "pack_type", length = 50)
    private String packType;

    @Column(name = "storage_location_name", length = 200)
    private String storageLocationName;

    @Column(name = "net_compartment_name", length = 200)
    private String netCompartmentName;

    /** Non-packing (OTHER) output: variety (auto from input) + bag details. */
    @Column(name = "variety_name", length = 150)
    private String varietyName;

    @Column(name = "bag_size_name", length = 100)
    private String bagSizeName;

    @Column(name = "bag_type_name", length = 100)
    private String bagTypeName;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
