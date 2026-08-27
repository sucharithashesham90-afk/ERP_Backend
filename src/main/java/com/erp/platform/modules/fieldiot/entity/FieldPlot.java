package com.erp.platform.modules.fieldiot.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A mapped field: the unit that weather, soil and satellite readings hang off.
 *
 * The boundary is held as GeoJSON text rather than a spatial column so the module works on a
 * plain relational database with no PostGIS dependency — everything the dashboard needs is
 * served from the centroid and the stored area.
 */
@Entity(name = "IotFieldPlot")
@Table(name = "field_plots",
       indexes = {
           @Index(name = "idx_fieldplot_tenant", columnList = "tenant_id"),
           @Index(name = "idx_fieldplot_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class FieldPlot extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String code;

    /** Free-text village/region, kept denormalised so the dashboard needs no extra joins. */
    @Column(length = 150)
    private String location;

    @Column(name = "grower_id")
    private UUID growerId;

    @Column(name = "grower_name", length = 200)
    private String growerName;

    @Column(name = "crop_name", length = 150)
    private String cropName;

    @Column(name = "variety_name", length = 150)
    private String varietyName;

    @Column(name = "area_hectares", precision = 12, scale = 4)
    private BigDecimal areaHectares;

    @Column(name = "centroid_latitude", precision = 10, scale = 6)
    private BigDecimal centroidLatitude;

    @Column(name = "centroid_longitude", precision = 10, scale = 6)
    private BigDecimal centroidLongitude;

    /** GeoJSON polygon of the field boundary. Optional — the centroid alone drives the sync. */
    @Lob
    @Column(name = "boundary_geojson")
    private String boundaryGeojson;

    @Column(name = "sowing_date")
    private java.time.LocalDate sowingDate;

    @Column(name = "expected_harvest_date")
    private java.time.LocalDate expectedHarvestDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
